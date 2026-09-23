package atlas.core

import assertk.assertThat
import assertk.assertions.isEmpty
import assertk.assertions.isEqualTo
import atlas.core.internal.ProjectLink
import atlas.core.internal.readProjectLinks
import atlas.test.ScenarioTest
import atlas.test.resolve
import atlas.test.scenarios.DiamondGraph
import atlas.test.scenarios.OverrideProjectLinksFile
import atlas.test.scenarios.ThreeProjectsWithBuiltInTypes
import atlas.test.scenarios.TriangleGraph
import blueprint.test.assertThatTask
import blueprint.test.buildsSuccessfully
import blueprint.test.doesNotExist
import java.io.File
import kotlin.test.Test

internal class CollateProjectLinksTest : ScenarioTest() {
  @Test
  fun `Empty file for three projects with no dependencies`() = ThreeProjectsWithBuiltInTypes {
    // when
    assertThatTask("collateProjectLinks").buildsSuccessfully()

    // and the links file is empty
    assertThat(projectLinks).isEmpty()
  }

  @Test
  fun `Single links for diamond`() = DiamondGraph {
    // when
    assertThatTask("collateProjectLinks").buildsSuccessfully()

    // and the links file contains the expected, in a-z order
    assertThat(projectLinks)
      .isEqualTo(
        setOf(
          ProjectLink(
            fromPath = ":mid-a",
            toPath = ":bottom",
            configuration = "api",
            type = null,
          ),
          ProjectLink(
            fromPath = ":mid-b",
            toPath = ":bottom",
            configuration = "implementation",
            type = null,
          ),
          ProjectLink(fromPath = ":top", toPath = ":mid-a", configuration = "api", type = null),
          ProjectLink(
            fromPath = ":top",
            toPath = ":mid-b",
            configuration = "implementation",
            type = null,
          ),
        )
      )
  }

  @Test
  fun `Multiple links for triangle`() = TriangleGraph {
    // when
    assertThatTask("collateProjectLinks").buildsSuccessfully()

    // and the triangle links were detected, in a-z order
    assertThat(projectLinks)
      .isEqualTo(
        setOf(
          ProjectLink(
            fromPath = ":a",
            toPath = ":b1",
            configuration = "implementation",
            type = null,
          ),
          ProjectLink(
            fromPath = ":a",
            toPath = ":b2",
            configuration = "implementation",
            type = null,
          ),
          ProjectLink(
            fromPath = ":b1",
            toPath = ":c1",
            configuration = "implementation",
            type = null,
          ),
          ProjectLink(
            fromPath = ":b1",
            toPath = ":c2",
            configuration = "implementation",
            type = null,
          ),
          ProjectLink(
            fromPath = ":b2",
            toPath = ":c2",
            configuration = "implementation",
            type = null,
          ),
          ProjectLink(
            fromPath = ":b2",
            toPath = ":c3",
            configuration = "implementation",
            type = null,
          ),
        )
      )
  }

  @Test
  fun `Can override task conventions from build script`() = OverrideProjectLinksFile {
    // when
    assertThatTask("collateProjectLinks").buildsSuccessfully()

    // then the default config file wasn't created
    assertThat(projectLinksFile).doesNotExist()

    // but the custom one does
    val customProjectLinksFileContents = readProjectLinks(resolve("custom-project-links-file.txt"))

    // and it contains the same from the diamond test a bit up from here
    assertThat(customProjectLinksFileContents)
      .isEqualTo(
        setOf(
          ProjectLink(
            fromPath = ":mid-a",
            toPath = ":bottom",
            configuration = "api",
            type = null,
          ),
          ProjectLink(
            fromPath = ":mid-b",
            toPath = ":bottom",
            configuration = "implementation",
            type = null,
          ),
          ProjectLink(fromPath = ":top", toPath = ":mid-a", configuration = "api", type = null),
          ProjectLink(
            fromPath = ":top",
            toPath = ":mid-b",
            configuration = "implementation",
            type = null,
          ),
        )
      )
  }

  private val projectLinksFile: File
    get() = rootDir.resolve("build/atlas/project-links.json")

  private val projectLinks: Set<ProjectLink>
    get() = readProjectLinks(projectLinksFile)
}
