package atlas.core.internal

import assertk.assertThat
import assertk.assertions.containsExactly
import atlas.core.LinkType
import java.io.File
import kotlin.test.Test
import org.junit.jupiter.api.io.TempDir

internal class LinkTypeMatchingTest {
  @TempDir lateinit var dir: File

  @Test
  fun `Exact configuration name matches`() {
    val api = LinkType(configuration = "api")

    assertThat(typesFor(listOf("api"), setOf(api))).containsExactly(api)
  }

  @Test
  fun `Pattern matches ignoring case`() {
    val implementation = LinkType(configuration = ".*implementation")

    assertThat(typesFor(listOf("commonMainImplementation"), setOf(implementation)))
      .containsExactly(implementation)
  }

  @Test
  fun `First matching type wins`() {
    val anyApi = LinkType(configuration = ".*api")
    val api = LinkType(configuration = "api")

    assertThat(typesFor(listOf("api"), setOf(anyApi, api))).containsExactly(anyApi)
  }

  @Test
  fun `Unmatched configuration has no type`() {
    val api = LinkType(configuration = "api")

    assertThat(typesFor(listOf("compileOnly"), setOf(api))).containsExactly(null)
  }

  @Test
  fun `Exact name that isn't a valid pattern still matches`() {
    val weird = LinkType(configuration = "weird[")

    assertThat(typesFor(listOf("weird["), setOf(weird))).containsExactly(weird)
  }

  private fun typesFor(configurations: List<String>, linkTypes: Set<LinkType>) =
    writeProjectLinks(
        outputFile = dir.resolve("links.json"),
        fromPath = ":a",
        projectLinks = mapOf(":b" to configurations),
        linkTypes = linkTypes,
      )
      .map { it.type }
}
