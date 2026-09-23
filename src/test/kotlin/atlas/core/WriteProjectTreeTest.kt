package atlas.core

import assertk.assertThat
import assertk.assertions.isEmpty
import atlas.core.internal.ProjectLink
import atlas.core.internal.readProjectLinks
import atlas.test.ScenarioTest
import atlas.test.isEqualToSet
import atlas.test.scenarios.DiamondGraph
import atlas.test.scenarios.DiamondGraphWithUpwardsTraversal
import atlas.test.scenarios.MultiplatformProjectsCustomConfigurations
import atlas.test.scenarios.ThreeProjectsWithBuiltInTypes
import atlas.test.scenarios.TriangleGraph
import atlas.test.scenarios.TriangleGraphWithUpwardsTraversal
import blueprint.test.allTasksSuccessful
import blueprint.test.assertThatTask
import blueprint.test.buildsSuccessfully
import kotlin.test.Test

internal class WriteProjectTreeTest : ScenarioTest() {
  @Test
  fun `Empty files for three projects with no dependencies`() = ThreeProjectsWithBuiltInTypes {
    assertThatTask("writeProjectTree").buildsSuccessfully().allTasksSuccessful()

    assertThat(projectTree("test-data")).isEmpty()
    assertThat(projectTree("test-domain")).isEmpty()
    assertThat(projectTree("test-ui")).isEmpty()
  }

  @Test
  fun `Filter links between multiplatform projects including custom configurations`() =
    MultiplatformProjectsCustomConfigurations {
      // when
      assertThatTask("writeProjectTree").buildsSuccessfully().allTasksSuccessful()

      // and the specified link type takes precedence over the others
      assertThat(projectTree(project = "a"))
        .isEqualToSet(
          ProjectLink(
            fromPath = ":a",
            toPath = ":b",
            configuration = "commonMainImplementation",
            type = LinkType(configuration = "commonMainImplementation", style = Solid),
          ),
          ProjectLink(
            fromPath = ":a",
            toPath = ":c",
            configuration = "commonMainApi",
            type = LinkType(configuration = "commonMainApi", style = Dotted),
          ),
        )
    }

  @Test
  fun `Single links for diamond`() = DiamondGraph {
    // when
    assertThatTask("writeProjectTree").buildsSuccessfully().allTasksSuccessful()

    // and the top project sees everything below it
    assertThat(projectTree("top"))
      .isEqualToSet(
        ProjectLink(fromPath = ":mid-a", toPath = ":bottom", configuration = "api", type = null),
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

    // and the mid only sees itself and bottom
    assertThat(projectTree("mid-a"))
      .isEqualToSet(
        ProjectLink(fromPath = ":mid-a", toPath = ":bottom", configuration = "api", type = null)
      )

    assertThat(projectTree("mid-b"))
      .isEqualToSet(
        ProjectLink(
          fromPath = ":mid-b",
          toPath = ":bottom",
          configuration = "implementation",
          type = null,
        )
      )

    // and the bottom sees nothing
    assertThat(projectTree("bottom")).isEmpty()
  }

  @Test
  fun `Single links for diamond with upwards traversal`() = DiamondGraphWithUpwardsTraversal {
    // when
    assertThatTask("writeProjectTree").buildsSuccessfully().allTasksSuccessful()

    // and the top project sees everything below it
    assertThat(projectTree("top"))
      .isEqualToSet(
        ProjectLink(fromPath = ":mid-a", toPath = ":bottom", configuration = "api", type = null),
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

    // and the mid sees itself, top and bottom
    assertThat(projectTree("mid-a"))
      .isEqualToSet(
        ProjectLink(fromPath = ":mid-a", toPath = ":bottom", configuration = "api", type = null),
        ProjectLink(fromPath = ":top", toPath = ":mid-a", configuration = "api", type = null),
      )

    assertThat(projectTree("mid-b"))
      .isEqualToSet(
        ProjectLink(
          fromPath = ":mid-b",
          toPath = ":bottom",
          configuration = "implementation",
          type = null,
        ),
        ProjectLink(
          fromPath = ":top",
          toPath = ":mid-b",
          configuration = "implementation",
          type = null,
        ),
      )

    // and the bottom sees everything above it
    assertThat(projectTree("bottom"))
      .isEqualToSet(
        ProjectLink(fromPath = ":mid-a", toPath = ":bottom", configuration = "api", type = null),
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
  }

  @Test
  fun `Multiple links for triangle`() = TriangleGraph {
    // when
    assertThatTask("writeProjectTree").buildsSuccessfully().allTasksSuccessful()

    // and the triangle links were detected, in a-z order
    assertThat(projectTree("a"))
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
    assertThat(projectTree("b1"))
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
    assertThat(projectTree("b2"))
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
    assertThat(projectTree("c1")).isEmpty()
    assertThat(projectTree("c2")).isEmpty()
    assertThat(projectTree("c3")).isEmpty()
  }

  @Test
  fun `Multiple links for triangle with upwards traversal`() = TriangleGraphWithUpwardsTraversal {
    // when
    assertThatTask("writeProjectTree").buildsSuccessfully().allTasksSuccessful()

    // and the triangle links were detected, in a-z order
    assertThat(projectTree("a"))
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
    assertThat(projectTree("b1"))
      .isEqualToSet(
        ProjectLink(
          fromPath = ":a",
          toPath = ":b1",
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
      )
    assertThat(projectTree("b2"))
      .isEqualToSet(
        ProjectLink(
          fromPath = ":a",
          toPath = ":b2",
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
    assertThat(projectTree("c1"))
      .isEqualToSet(
        ProjectLink(
          fromPath = ":a",
          toPath = ":b1",
          configuration = "implementation",
          type = null,
        ),
        ProjectLink(
          fromPath = ":b1",
          toPath = ":c1",
          configuration = "implementation",
          type = null,
        ),
      )
    assertThat(projectTree("c2"))
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
      )
    assertThat(projectTree("c3"))
      .isEqualToSet(
        ProjectLink(
          fromPath = ":a",
          toPath = ":b2",
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
  }

  private fun projectTree(project: String): Set<ProjectLink> =
    rootDir.resolve("$project/build/atlas/project-tree.json").let(::readProjectLinks)
}
