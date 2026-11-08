package atlas.d2.tasks

import assertk.assertThat
import assertk.assertions.containsExactly
import assertk.assertions.isEmpty
import atlas.d2.AsciiMode
import atlas.d2.FileFormat
import atlas.d2.tasks.ExecD2.Companion.cliFlags
import java.io.File
import kotlin.test.Test

internal class D2CliFlagsTest {
  @Test
  fun `Nothing configured passes no flags`() {
    assertThat(cliFlags(format = FileFormat.Svg)).isEmpty()
  }

  @Test
  fun `Layout engine options come first, then the other flags`() {
    val flags =
      cliFlags(
        format = FileFormat.Svg,
        cliArguments = mapOf("layout" to "elk", "elk-nodeSelfLoop" to "50"),
        omitVersion = true,
        scale = 1.5f,
        timeoutSeconds = 300,
      )

    assertThat(flags)
      .containsExactly(
        "--layout=elk",
        "--elk-nodeSelfLoop=50",
        "--omit-version=true",
        "--scale=1.5",
        "--timeout=300",
      )
  }

  @Test
  fun `Animate interval is only passed for gifs`() {
    assertThat(cliFlags(format = FileFormat.Gif, animateInterval = 500))
      .containsExactly("--animate-interval=500")
    assertThat(cliFlags(format = FileFormat.Svg, animateInterval = 500)).isEmpty()
  }

  @Test
  fun `Ascii mode is only passed for text output`() {
    assertThat(cliFlags(format = FileFormat.Ascii, asciiMode = AsciiMode.Standard))
      .containsExactly("--ascii-mode=standard")
    assertThat(cliFlags(format = FileFormat.Png, asciiMode = AsciiMode.Standard)).isEmpty()
  }

  @Test
  fun `No XML tag is only passed for SVGs`() {
    assertThat(cliFlags(format = FileFormat.Svg, noXmlTag = true))
      .containsExactly("--no-xml-tag=true")
    assertThat(cliFlags(format = FileFormat.Ascii, noXmlTag = true)).isEmpty()
  }

  @Test
  fun `Fonts are passed as absolute paths`() {
    val font = File("font.ttf")

    assertThat(
        cliFlags(
          format = FileFormat.Svg,
          fonts = mapOf("font-regular" to font, "font-mono-bold" to font),
        ),
      )
      .containsExactly(
        "--font-regular=${font.absolutePath}",
        "--font-mono-bold=${font.absolutePath}",
      )
  }

  @Test
  fun `An explicit flag overrides the same key from the layout engine`() {
    assertThat(
        cliFlags(
          format = FileFormat.Svg,
          cliArguments = mapOf("timeout" to "10"),
          timeoutSeconds = 60,
        ),
      )
      .containsExactly("--timeout=60")
  }
}
