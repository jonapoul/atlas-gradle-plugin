package atlas.d2.internal

import java.io.File
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream
import java.net.ProxySelector
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import java.nio.file.Files
import java.nio.file.StandardCopyOption.ATOMIC_MOVE
import java.nio.file.StandardCopyOption.REPLACE_EXISTING
import java.security.MessageDigest
import java.time.Duration
import java.util.HexFormat
import java.util.zip.GZIPInputStream
import org.gradle.api.GradleException
import org.gradle.api.logging.Logging

/**
 * Downloads D2 release binaries from GitHub into a cache directory, one per version and platform.
 * Each binary is only ever downloaded once per machine, since the cache lives in the Gradle user
 * home rather than any one build.
 */
internal object D2Downloader {
  private val logger = Logging.getLogger(D2Downloader::class.java)

  private val client by lazy {
    // An HttpClient ignores proxies unless told otherwise, and this picks up Gradle's systemProp
    // ones
    HttpClient.newBuilder()
      .followRedirects(HttpClient.Redirect.NORMAL)
      .proxy(ProxySelector.getDefault())
      .connectTimeout(Duration.ofSeconds(CONNECT_TIMEOUT_SECONDS))
      .build()
  }

  // Parallel tasks in one build share this, so only the first one downloads
  @Synchronized
  fun executable(requestedVersion: String, cacheDir: File, offline: Boolean): File {
    val version = requestedVersion.removePrefix("v")
    val platform = D2Platform.current()
    val binary = cacheDir.resolve("v$version/${platform.id}/${platform.binaryName}")
    if (binary.isFile) return binary

    if (offline) {
      throw GradleException(
        "D2 $version isn't cached yet, and Gradle is running offline. Run once without --offline, " +
          "or set atlas.d2.executableSource=system to use d2 from the PATH."
      )
    }

    val archiveName = "d2-v$version-${platform.id}.tar.gz"
    val url = "$RELEASES_URL/v$version/$archiveName"
    logger.lifecycle("Downloading D2 $version from $url")

    val archive = download(url)
    verifyChecksum(version, archiveName, archive)

    // Written alongside then moved into place, so another Gradle process never sees half a file
    binary.parentFile.mkdirs()
    val temp = File.createTempFile("d2-", ".tmp", binary.parentFile)
    try {
      GZIPInputStream(archive.inputStream()).use { extract(it, platform.binaryName, temp) }
      temp.setExecutable(true)
      Files.move(temp.toPath(), binary.toPath(), ATOMIC_MOVE, REPLACE_EXISTING)
    } finally {
      temp.delete()
    }
    return binary
  }

  private fun download(url: String): ByteArray =
    get(url, HttpResponse.BodyHandlers.ofByteArray()).requireOk(url).body()

  private fun <T> HttpResponse<T>.requireOk(url: String): HttpResponse<T> {
    if (statusCode() != HTTP_OK)
      throw GradleException("Failed downloading $url: HTTP ${statusCode()}")
    return this
  }

  private fun <T> get(url: String, handler: HttpResponse.BodyHandler<T>): HttpResponse<T> =
    try {
      client.send(HttpRequest.newBuilder(URI(url)).GET().build(), handler)
    } catch (e: IOException) {
      throw GradleException("Failed downloading $url", e)
    }

  /**
   * D2 only publishes a SHA256SUMS file from 0.9.0 onwards. Older versions are still allowed, just
   * unverified.
   */
  private fun verifyChecksum(version: String, archiveName: String, archive: ByteArray) {
    val url = "$RELEASES_URL/v$version/SHA256SUMS"
    val response = get(url, HttpResponse.BodyHandlers.ofString())
    if (response.statusCode() == HTTP_NOT_FOUND) {
      logger.warn("D2 $version publishes no checksums, so its download can't be verified")
      return
    }
    val expected =
      response
        .requireOk(url)
        .body()
        .lineSequence()
        .map { it.trim().split(Regex("\\s+")) }
        .firstOrNull { it.size == 2 && it[1] == archiveName }
        ?.first() ?: throw GradleException("$url has no checksum for $archiveName")

    val actual = MessageDigest.getInstance("SHA-256").digest(archive).let(HexFormat.of()::formatHex)
    if (!actual.equals(expected, ignoreCase = true)) {
      throw GradleException("Checksum mismatch for $archiveName: expected $expected, got $actual")
    }
  }

  /**
   * Copies the `bin/<binaryName>` entry out of a tar stream into [destination]. D2's archives are
   * plain ustar, so this only reads what they use rather than pulling in a tar library.
   */
  internal fun extract(tar: InputStream, binaryName: String, destination: File) {
    val header = ByteArray(TAR_BLOCK)
    while (tar.readNBytes(header, 0, TAR_BLOCK) == TAR_BLOCK && header.any { it != 0.toByte() }) {
      val name = header.string(offset = 0, length = 100)
      val prefix = header.string(offset = 345, length = 155)
      val path = if (prefix.isEmpty()) name else "$prefix/$name"
      val size = header.string(offset = 124, length = 12).trim().toLong(radix = 8)

      if (path.endsWith("bin/$binaryName")) {
        destination.outputStream().use { out -> copy(tar, out, size) }
        return
      }

      tar.skipNBytes(size.roundUpTo(TAR_BLOCK))
    }
    throw GradleException("D2 archive has no bin/$binaryName")
  }

  private fun copy(input: InputStream, output: OutputStream, size: Long) {
    val buffer = ByteArray(DEFAULT_BUFFER_SIZE)
    var remaining = size
    while (remaining > 0) {
      val read = input.read(buffer, 0, minOf(buffer.size.toLong(), remaining).toInt())
      if (read < 0) throw GradleException("D2 archive ended early")
      output.write(buffer, 0, read)
      remaining -= read
    }
  }

  private fun ByteArray.string(offset: Int, length: Int): String =
    String(this, offset, length, Charsets.US_ASCII).substringBefore('\u0000')

  private fun Long.roundUpTo(block: Int): Long = (this + block - 1) / block * block

  private const val RELEASES_URL = "https://github.com/terrastruct/d2/releases/download"
  private const val CONNECT_TIMEOUT_SECONDS = 30L
  private const val HTTP_OK = 200
  private const val HTTP_NOT_FOUND = 404
  private const val TAR_BLOCK = 512
}

internal data class D2Platform(val os: String, val arch: String) {
  val id: String
    get() = "$os-$arch"

  val binaryName: String
    get() = if (os == "windows") "d2.exe" else "d2"

  companion object {
    fun current(): D2Platform = of(System.getProperty("os.name"), System.getProperty("os.arch"))

    fun of(osName: String, osArch: String): D2Platform {
      val os =
        when {
          osName.startsWith("Windows", ignoreCase = true) -> "windows"
          osName.startsWith("Mac", ignoreCase = true) -> "macos"
          osName.startsWith("Linux", ignoreCase = true) -> "linux"
          else -> unsupported(osName, osArch)
        }
      val arch =
        when (osArch.lowercase()) {
          "amd64",
          "x86_64" -> "amd64"
          "aarch64",
          "arm64" -> "arm64"
          else -> unsupported(osName, osArch)
        }
      return D2Platform(os, arch)
    }

    private fun unsupported(osName: String, osArch: String): Nothing =
      throw GradleException(
        "D2 publishes no binary for $osName/$osArch. Install d2 yourself and set " +
          "atlas.d2.executableSource=system, or point atlas.d2.d2Executable at it."
      )
  }
}
