package atlas.core.internal

import assertk.assertThat
import assertk.assertions.contains
import assertk.assertions.doesNotContain
import atlas.core.ProjectType
import atlas.core.Replacement
import atlas.d2.d2Writer
import atlas.graphviz.dotWriter
import atlas.mermaid.mermaidWriter
import atlas.test.ProjectLayout
import atlas.test.projectLink
import atlas.test.typedProject
import kotlin.test.Test

/**
 * Each path must be replaced exactly once. A replacement whose output it matches again, like `lib`
 * to `library`, shows when that slips: the node gets written as `:librarybrary` while its link
 * still points at `:library`, splitting it into two nodes.
 */
internal class PathReplacementTest {
  private val layout =
    object : ProjectLayout {
      private val type = ProjectType(name = "Lib", color = "red", properties = emptyMap())
      override val projects = setOf(typedProject(":app"), typedProject(":lib", type))
      override val links = setOf(projectLink(fromPath = ":app", toPath = ":lib"))
    }

  private val replacements = setOf(Replacement("lib".toRegex(), "library"))

  @Test
  fun `Graphviz replaces each path once`() {
    for (groupProjects in listOf(false, true)) {
      val chart =
        dotWriter(
          typedProjects = layout.projects,
          links = layout.links,
          replacements = replacements,
          groupProjects = groupProjects,
        )()

      assertThat(chart).contains("\":library\"")
      assertThat(chart).doesNotContain("librarybrary")
    }
  }

  @Test
  fun `D2 replaces each path once`() {
    for (groupProjects in listOf(false, true)) {
      val chart = d2Writer(layout, replacements = replacements, groupProjects = groupProjects)()

      assertThat(chart).contains("library")
      assertThat(chart).doesNotContain("librarybrary")
    }
  }

  @Test
  fun `Mermaid replaces each path once`() {
    for (groupProjects in listOf(false, true)) {
      val chart =
        mermaidWriter(
          typedProjects = layout.projects,
          links = layout.links,
          replacements = replacements,
          groupProjects = groupProjects,
        )()

      assertThat(chart).contains("_library")
      assertThat(chart).doesNotContain("librarybrary")
    }
  }
}
