package atlas.graphviz.internal

import assertk.assertThat
import atlas.graphviz.dotWriter
import atlas.test.OneLevelOfSubprojects
import atlas.test.ProjectWithNoLinks
import atlas.test.TwoLevelsOfSubprojects
import blueprint.test.isEqualToTrimmed
import kotlin.test.Test

internal class DotWriterTest {
  @Test
  fun `Base config with no project types`() {
    val writer =
      dotWriter(
        typedProjects = OneLevelOfSubprojects.projects,
        links = OneLevelOfSubprojects.links,
      )

    assertThat(writer())
      .isEqualToTrimmed(
        """
        digraph {
          ":app"
          ":data:a"
          ":data:b"
          ":domain:a"
          ":domain:b"
          ":ui:a"
          ":ui:b"
          ":ui:c"
          ":app" -> ":ui:a"
          ":app" -> ":ui:b"
          ":app" -> ":ui:c"
          ":domain:a" -> ":data:a"
          ":domain:b" -> ":data:a"
          ":domain:b" -> ":data:b"
          ":ui:a" -> ":domain:a"
          ":ui:b" -> ":domain:b"
          ":ui:c" -> ":domain:a"
          ":ui:c" -> ":domain:b"
        }
        """
          .trimIndent()
      )
  }

  @Test
  fun `Grouping projects`() {
    val writer =
      dotWriter(
        typedProjects = OneLevelOfSubprojects.projects,
        links = OneLevelOfSubprojects.links,
        groupProjects = true,
      )

    assertThat(writer())
      .isEqualToTrimmed(
        """
        digraph {
          ":app"
          subgraph cluster_data {
            label = ":data"
            ":data:a" [label=":a"]
            ":data:b" [label=":b"]
          }
          subgraph cluster_domain {
            label = ":domain"
            ":domain:a" [label=":a"]
            ":domain:b" [label=":b"]
          }
          subgraph cluster_ui {
            label = ":ui"
            ":ui:a" [label=":a"]
            ":ui:b" [label=":b"]
            ":ui:c" [label=":c"]
          }
          ":app" -> ":ui:a"
          ":app" -> ":ui:b"
          ":app" -> ":ui:c"
          ":domain:a" -> ":data:a"
          ":domain:b" -> ":data:a"
          ":domain:b" -> ":data:b"
          ":ui:a" -> ":domain:a"
          ":ui:b" -> ":domain:b"
          ":ui:c" -> ":domain:a"
          ":ui:c" -> ":domain:b"
        }
        """
          .trimIndent()
      )
  }

  @Test
  fun `Grouping projects with sub-subgraphs`() {
    val writer =
      dotWriter(
        typedProjects = TwoLevelsOfSubprojects.projects,
        links = TwoLevelsOfSubprojects.links,
        groupProjects = true,
      )

    assertThat(writer())
      .isEqualToTrimmed(
        """
        digraph {
          ":app"
          subgraph cluster_data {
            label = ":data"
            ":data:a" [label=":a"]
            ":data:b" [label=":b"]
            subgraph cluster_sub {
              label = ":sub"
              ":data:sub:sub1" [label=":sub1"]
              ":data:sub:sub2" [label=":sub2"]
            }
          }
          subgraph cluster_domain {
            label = ":domain"
            ":domain:a" [label=":a"]
            ":domain:b" [label=":b"]
          }
          subgraph cluster_ui {
            label = ":ui"
            ":ui:a" [label=":a"]
            ":ui:b" [label=":b"]
            ":ui:c" [label=":c"]
          }
          ":app" -> ":ui:a"
          ":app" -> ":ui:b"
          ":app" -> ":ui:c"
          ":domain:a" -> ":data:a"
          ":domain:a" -> ":data:sub:sub1"
          ":domain:a" -> ":data:sub:sub2"
          ":domain:b" -> ":data:a"
          ":domain:b" -> ":data:b"
          ":ui:a" -> ":domain:a"
          ":ui:b" -> ":domain:b"
          ":ui:c" -> ":domain:a"
          ":ui:c" -> ":domain:b"
        }
        """
          .trimIndent()
      )
  }

  @Test
  fun `Single project with no links`() {
    val writer =
      dotWriter(
        typedProjects = ProjectWithNoLinks.projects,
        links = ProjectWithNoLinks.links,
      )

    assertThat(writer())
      .isEqualToTrimmed(
        """
        digraph {
          ":app" [fillcolor="red"]
        }
        """
          .trimIndent()
      )
  }
}
