package atlas.d2.internal

import assertk.assertFailure
import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.assertions.isInstanceOf
import java.io.ByteArrayOutputStream
import java.io.File
import kotlin.io.path.createTempDirectory
import kotlin.test.Test
import org.gradle.api.GradleException

internal class D2DownloadTest {
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
