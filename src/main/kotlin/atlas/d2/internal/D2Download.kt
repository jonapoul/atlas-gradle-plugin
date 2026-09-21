package atlas.d2.internal

import atlas.core.internal.ATLAS_CONFIGURATION_PREFIX
import atlas.core.internal.AtlasConfig
import atlas.d2.D2Spec
import atlas.d2.ExecutableSource
import atlas.d2.ExecutableSource.Download
import atlas.d2.ExecutableSource.Path
import java.io.File
import java.io.InputStream
import java.io.OutputStream
import java.util.zip.GZIPInputStream
import org.gradle.api.GradleException
import org.gradle.api.Project
import org.gradle.api.artifacts.dsl.RepositoryHandler
import org.gradle.api.artifacts.transform.InputArtifact
import org.gradle.api.artifacts.transform.TransformAction
import org.gradle.api.artifacts.transform.TransformOutputs
import org.gradle.api.artifacts.transform.TransformParameters
import org.gradle.api.artifacts.type.ArtifactTypeDefinition.ARTIFACT_TYPE_ATTRIBUTE
import org.gradle.api.file.FileSystemLocation
import org.gradle.api.file.RegularFile
import org.gradle.api.logging.Logger
import org.gradle.api.provider.Provider
import org.gradle.api.provider.ProviderFactory
import org.gradle.api.tasks.PathSensitive
import org.gradle.api.tasks.PathSensitivity.NONE
import org.gradle.work.DisableCachingByDefault

/*
 * D2 is downloaded from its GitHub releases as a regular Gradle dependency, so it gets Gradle's
 * caching, --offline handling, proxy settings and dependency verification. The releases page is
 * declared as an Ivy repository serving nothing but D2, and a transform unpacks the binary from its
 * tarball. Both results are cached in the Gradle user home, once per version and platform.
 */

private const val D2_GROUP = "d2lang"
private const val D2_MODULE = "d2"
private const val TARBALL_TYPE = "tar.gz"
private const val EXECUTABLE_TYPE = "d2-executable"
private const val CONFIGURATION_NAME = "${ATLAS_CONFIGURATION_PREFIX}D2Executable"

internal fun RepositoryHandler.d2Releases() {
  exclusiveContent { content ->
    content.forRepository {
      ivy { repo ->
        repo.name = "atlasD2Releases"
        repo.setUrl("https://github.com/$D2_GROUP/$D2_MODULE/releases/download")
        repo.patternLayout { it.artifact("v[revision]/[module]-v[revision]-[classifier].[ext]") }
        repo.metadataSources { it.artifact() }
      }
    }
    content.filter { it.includeGroup(D2_GROUP) }
  }
}

/**
 * Which D2 version to download, or null if d2 comes from somewhere else. Decided once from
 * settings, so that builds which never download get no repository or configuration for it.
 */
internal fun D2SpecImpl.downloadVersion(providers: ProviderFactory): String? =
  d2DownloadVersion(
    explicit = executable.isPresent || properties.executable.isPresent,
    source = executableSource.get(),
    pinned = version.orNull,
    onPath = { providers.d2OnPath().isPresent },
  )

/** [D2Spec.version] only picks what gets downloaded, so say when nothing will be. */
internal fun D2SpecImpl.warnIfVersionIgnored(logger: Logger) {
  val pinned = version.orNull ?: return
  val reason =
    when {
      executable.isPresent || properties.executable.isPresent -> "executable is also set"
      executableSource.get() == Path -> "executableSource is Path"
      else -> return
    }
  logger.warn(
    "Warning: version is set to $pinned, but $reason, so it's ignored and nothing is " +
      "downloaded. Remove version, or set executableSource to Auto or Download."
  )
}

/**
 * An explicit d2 always wins. Otherwise [ExecutableSource.Auto] prefers the PATH, unless a version
 * was set, since that means the user wants exactly that version.
 */
internal fun d2DownloadVersion(
  explicit: Boolean,
  source: ExecutableSource,
  pinned: String?,
  onPath: () -> Boolean,
): String? =
  when {
    explicit -> null
    source == Path -> null
    source == Download || pinned != null -> pinned ?: DEFAULT_D2_VERSION
    onPath() -> null
    else -> DEFAULT_D2_VERSION
  }

internal fun ProviderFactory.d2OnPath(): Provider<File> =
  environmentVariable("PATH").flatMap { path ->
    provider {
      path
        .split(File.pathSeparator)
        .flatMap { dir -> listOf(File(dir, "d2"), File(dir, "d2.exe")) }
        .firstOrNull { it.isFile && it.canExecute() }
    }
  }

internal fun Project.downloadedD2(version: String, config: AtlasConfig): Provider<RegularFile> {
  val resolvable =
    if (CONFIGURATION_NAME in configurations.names) {
      configurations.named(CONFIGURATION_NAME)
    } else {
      addD2Releases(config)
      registerD2Configuration(version)
    }

  val files =
    resolvable.get().incoming.artifactView { view ->
      view.attributes.attribute(ARTIFACT_TYPE_ATTRIBUTE, EXECUTABLE_TYPE)
    }

  return layout.file(files.files.elements.map { it.single().asFile })
}

/**
 * Settings always get the repository. Under [AtlasConfig.preferProjectRepositories] a project with
 * repositories of its own ignores the settings ones, so it gets a copy too. Anywhere else, adding
 * one would either be ignored with a warning or fail the build.
 */
private fun Project.addD2Releases(config: AtlasConfig) {
  if (!config.preferProjectRepositories) return
  // Only this project's own repositories, so it stays within isolated projects' rules. It has to
  // wait for the build script, since that's where they're declared.
  @Suppress("AvoidAfterEvaluate")
  afterEvaluate { if (repositories.isNotEmpty()) repositories.d2Releases() }
}

@Suppress("UnstableApiUsage")
private fun Project.registerD2Configuration(version: String) = run {
  dependencies.registerTransform(UnpackD2::class.java) { spec ->
    spec.from.attribute(ARTIFACT_TYPE_ATTRIBUTE, TARBALL_TYPE)
    spec.to.attribute(ARTIFACT_TYPE_ATTRIBUTE, EXECUTABLE_TYPE)
  }

  val scope = configurations.dependencyScope("${CONFIGURATION_NAME}Dependencies")
  val platform = D2Platform.current()
  dependencies.add(
    scope.name,
    "$D2_GROUP:$D2_MODULE:${version.removePrefix("v")}:${platform.id}@$TARBALL_TYPE",
  )

  configurations.resolvable(CONFIGURATION_NAME) { it.extendsFrom(scope.get()) }
}

@DisableCachingByDefault(because = "Quicker to unpack again than to fetch from a cache")
internal abstract class UnpackD2 : TransformAction<TransformParameters.None> {
  @get:[InputArtifact PathSensitive(NONE)]
  abstract val tarball: Provider<FileSystemLocation>

  override fun transform(outputs: TransformOutputs) {
    val tarball = tarball.get().asFile
    GZIPInputStream(tarball.inputStream()).use { tar ->
      extractD2(tar) { name -> outputs.file(name) }.setExecutable(true)
    }
  }
}

/**
 * Copies the `bin/d2` (or `bin/d2.exe`) entry out of a tar stream, into the file [destination]
 * returns for its name. D2's archives are plain ustar, so this only reads what they use rather than
 * pulling in a tar library.
 */
internal fun extractD2(tar: InputStream, destination: (name: String) -> File): File {
  val header = ByteArray(TAR_BLOCK)
  while (tar.readNBytes(header, 0, TAR_BLOCK) == TAR_BLOCK && header.any { it != 0.toByte() }) {
    val name = header.string(offset = 0, length = 100)
    val prefix = header.string(offset = 345, length = 155)
    val path = if (prefix.isEmpty()) name else "$prefix/$name"
    val size = header.string(offset = 124, length = 12).trim().toLong(radix = 8)
    val binaryName = path.substringAfterLast('/')

    if (binaryName in BINARY_NAMES && path.removeSuffix(binaryName).endsWith("bin/")) {
      val file = destination(binaryName)
      file.outputStream().use { out -> copy(tar, out, size) }
      return file
    }

    tar.skipNBytes(size.roundUpTo(TAR_BLOCK))
  }
  throw GradleException("D2 archive has no bin/d2")
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

private const val TAR_BLOCK = 512
private val BINARY_NAMES = setOf("d2", "d2.exe")

internal data class D2Platform(val os: String, val arch: String) {
  val id: String
    get() = "$os-$arch"

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
          "atlas.d2.executableSource=path, or point atlas.d2.executable at it."
      )
  }
}
