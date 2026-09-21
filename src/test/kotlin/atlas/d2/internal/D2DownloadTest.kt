package atlas.d2.internal

import assertk.assertFailure
import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.assertions.isFalse
import assertk.assertions.isInstanceOf
import assertk.assertions.isNull
import atlas.d2.ExecutableSource
import atlas.d2.ExecutableSource.Auto
import atlas.d2.ExecutableSource.Download
import atlas.d2.ExecutableSource.Path
import java.io.ByteArrayOutputStream
import java.io.File
import kotlin.io.path.createTempDirectory
import kotlin.test.Test
import org.gradle.api.GradleException

internal class D2DownloadTest {
  @Test
  fun `An explicit executable never downloads`() {
    ExecutableSource.entries.forEach { source ->
      assertThat(version(explicit = true, source = source, pinned = "0.7.1", onPath = false))
        .isNull()
    }
  }

  @Test
  fun `Path never downloads`() {
    assertThat(version(source = Path, pinned = "0.7.1", onPath = false)).isNull()
    assertThat(version(source = Path, onPath = false)).isNull()
  }

  @Test
  fun `Download always downloads, preferring the pinned version`() {
    assertThat(version(source = Download, onPath = true)).isEqualTo(DEFAULT_D2_VERSION)
    assertThat(version(source = Download, pinned = "0.7.1", onPath = true)).isEqualTo("0.7.1")
  }

  @Test
  fun `Auto uses the PATH unless a version is pinned`() {
    assertThat(version(source = Auto, onPath = true)).isNull()
    assertThat(version(source = Auto, onPath = false)).isEqualTo(DEFAULT_D2_VERSION)
    assertThat(version(source = Auto, pinned = "0.7.1", onPath = true)).isEqualTo("0.7.1")
  }

  @Test
  fun `Only searches the PATH when it could matter`() {
    var searched = false
    d2DownloadVersion(explicit = false, source = Download, pinned = null) {
      true.also { searched = true }
    }
    assertThat(searched).isFalse()
  }

  private fun version(
    explicit: Boolean = false,
    source: ExecutableSource,
    pinned: String? = null,
    onPath: Boolean,
  ) = d2DownloadVersion(explicit = explicit, source = source, pinned = pinned, onPath = { onPath })

  @Test
  fun `Maps JVM platform names to D2 archive names`() {
    assertThat(D2Platform.of("Linux", "amd64").id).isEqualTo("linux-amd64")
    assertThat(D2Platform.of("Mac OS X", "aarch64").id).isEqualTo("macos-arm64")
    assertThat(D2Platform.of("Mac OS X", "x86_64").id).isEqualTo("macos-amd64")
    assertThat(D2Platform.of("Windows 11", "amd64").id).isEqualTo("windows-amd64")
  }

  @Test
  fun `Fails on platforms D2 doesn't publish for`() {
    assertFailure { D2Platform.of("FreeBSD", "amd64") }.isInstanceOf<GradleException>()
    assertFailure { D2Platform.of("Linux", "riscv64") }.isInstanceOf<GradleException>()
  }

  @Test
  fun `Extracts the binary from a tarball`() {
    val tar =
      tarOf(
        "d2-v0.9.0/README.md" to "readme",
        "d2-v0.9.0/bin/d2" to "the binary",
        "d2-v0.9.0/man/d2.1" to "manual",
      )
    val dir = createTempDirectory().toFile()

    val binary = extractD2(tar.inputStream()) { name -> File(dir, name) }

    assertThat(binary.name).isEqualTo("d2")
    assertThat(binary.readText()).isEqualTo("the binary")
  }

  @Test
  fun `Fails if the tarball has no binary`() {
    val tar = tarOf("d2-v0.9.0/README.md" to "readme")
    val dir = createTempDirectory().toFile()

    assertFailure { extractD2(tar.inputStream()) { name -> File(dir, name) } }
      .isInstanceOf<GradleException>()
  }

  private fun tarOf(vararg entries: Pair<String, String>): ByteArray {
    val out = ByteArrayOutputStream()
    entries.forEach { (path, content) ->
      val header = ByteArray(BLOCK)
      path.toByteArray().copyInto(header, destinationOffset = 0)
      content.length
        .toString(radix = 8)
        .padStart(11, '0')
        .toByteArray()
        .copyInto(header, destinationOffset = 124)
      header[156] = '0'.code.toByte()
      "ustar".toByteArray().copyInto(header, destinationOffset = 257)
      out.write(header)

      val data = content.toByteArray()
      out.write(data)
      out.write(ByteArray((BLOCK - data.size % BLOCK) % BLOCK))
    }
    out.write(ByteArray(BLOCK * 2))
    return out.toByteArray()
  }

  private companion object {
    const val BLOCK = 512
  }
}
