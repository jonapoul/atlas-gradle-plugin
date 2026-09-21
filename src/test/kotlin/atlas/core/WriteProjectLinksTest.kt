package atlas.core

import assertk.assertThat
import assertk.assertions.isEmpty
import assertk.assertions.isEqualTo
import atlas.core.internal.ProjectLink
import atlas.core.internal.readProjectLinks
import atlas.test.ScenarioTest
import atlas.test.isEqualToSet
import atlas.test.scenarios.CustomConfigurationExcluded
import atlas.test.scenarios.CustomConfigurations
import atlas.test.scenarios.DiamondGraph
import atlas.test.scenarios.OneKotlinJvmProject
import atlas.test.scenarios.ThreeProjectsWithBuiltInTypes
import atlas.test.scenarios.TriangleGraph
import blueprint.test.allTasksSuccessful
import blueprint.test.assertThatTask
import blueprint.test.buildsSuccessfully
import blueprint.test.taskSucceeded
import kotlin.test.Test

internal class WriteProjectLinksTest : ScenarioTest() {
  @Test
  fun `Empty file for single project with no dependencies`() = OneKotlinJvmProject {
    // when
    assertThatTask("writeProjectLinks")
      .buildsSuccessfully()
      .taskSucceeded(":test-jvm:writeProjectLinks")

    // and the links file is empty
    assertThat(projectLinks(project = "test-jvm")).isEmpty()
  }

  @Test
  fun `Empty files for three projects with no dependencies`() = ThreeProjectsWithBuiltInTypes {
    // when
    assertThatTask("writeProjectLinks")
      .buildsSuccessfully()

      // then the task was run
      .taskSucceeded(":test-data:writeProjectLinks")
      .taskSucceeded(":test-domain:writeProjectLinks")
      .taskSucceeded(":test-ui:writeProjectLinks")

    // and the links file is empty
    assertThat(projectLinks(project = "test-data")).isEmpty()
    assertThat(projectLinks(project = "test-domain")).isEmpty()
    assertThat(projectLinks(project = "test-ui")).isEmpty()
  }

  @Test
  fun `Single links for diamond`() = DiamondGraph {
    // when
    assertThatTask("writeProjectLinks")
      .buildsSuccessfully()

      // then the task was run
      .taskSucceeded(":top:writeProjectLinks")
      .taskSucceeded(":mid-a:writeProjectLinks")
      .taskSucceeded(":mid-b:writeProjectLinks")
      .taskSucceeded(":bottom:writeProjectLinks")

    // and the links file is empty
    assertThat(projectLinks(project = "top"))
      .isEqualToSet(
        ProjectLink(fromPath = ":top", toPath = ":mid-a", configuration = "api", type = null),
        ProjectLink(
          fromPath = ":top",
          toPath = ":mid-b",
          configuration = "implementation",
          type = null,
        ),
      )
    assertThat(projectLinks(project = "mid-a"))
      .isEqualToSet(
        ProjectLink(fromPath = ":mid-a", toPath = ":bottom", configuration = "api", type = null)
      )
    assertThat(projectLinks(project = "mid-b"))
      .isEqualToSet(
        ProjectLink(
          fromPath = ":mid-b",
          toPath = ":bottom",
          configuration = "implementation",
          type = null,
        )
      )
    assertThat(projectLinks(project = "bottom")).isEqualTo(emptySet())
  }

  @Test
  fun `Multiple links for triangle`() = TriangleGraph {
    // when
    assertThatTask("writeProjectLinks").buildsSuccessfully().allTasksSuccessful()

    // and the triangle links were detected as expected
    assertThat(projectLinks(project = "a"))
      .isEqualToSet(
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
      )
    assertThat(projectLinks(project = "b1"))
      .isEqualToSet(
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
      )
    assertThat(projectLinks(project = "b2"))
      .isEqualToSet(
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
    assertThat(projectLinks(project = "c1")).isEmpty()
    assertThat(projectLinks(project = "c2")).isEmpty()
    assertThat(projectLinks(project = "c3")).isEmpty()
  }

  @Test
  fun `Custom configuration is picked up if we dont exclude it`() = CustomConfigurations {
    // when
    assertThatTask("writeProjectLinks").buildsSuccessfully().allTasksSuccessful()

    // and the two custom configs were detected as links
    assertThat(projectLinks(project = "a"))
      .isEqualToSet(
        ProjectLink(fromPath = ":a", toPath = ":b", configuration = "abc", type = null),
        ProjectLink(fromPath = ":a", toPath = ":b", configuration = "xyz", type = null),
      )
    assertThat(projectLinks(project = "b")).isEmpty()
  }

  @Test
  fun `Custom configuration is excluded`() = CustomConfigurationExcluded {
    // when
    assertThatTask("writeProjectLinks").buildsSuccessfully().allTasksSuccessful()

    // and the two custom configs were detected as links, but the xyz config was excluded
    assertThat(projectLinks(project = "a"))
      .isEqualToSet(ProjectLink(fromPath = ":a", toPath = ":b", configuration = "abc", type = null))
    assertThat(projectLinks(project = "b")).isEmpty()
  }

  private fun projectLinks(project: String): Set<ProjectLink> =
    rootDir.resolve("$project/build/atlas/project-links.json").let(::readProjectLinks)
}
