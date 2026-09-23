package atlas.graphviz.tasks

import assertk.assertThat
import atlas.test.ScenarioTest
import atlas.test.resolve
import atlas.test.scenarios.GraphVizChartCustomConfig
import atlas.test.scenarios.GraphVizChartWithCustomLinkTypes
import atlas.test.scenarios.GraphVizChartWithProperties
import atlas.test.scenarios.GraphVizChartWithReplacements
import atlas.test.scenarios.GraphvizBasic
import atlas.test.scenarios.GraphvizNestedProject
import blueprint.test.assertThatTask
import blueprint.test.buildsSuccessfully
import blueprint.test.contentEquals
import blueprint.test.exists
import kotlin.test.Test

internal class WriteGraphvizChartTest : ScenarioTest() {
  @Test
  fun `Generate dotfiles from basic config`() = GraphvizBasic {
    // when
    assertThatTask("writeGraphvizChart").buildsSuccessfully()

    // then the files were generated
    val dotFileA = resolve("a/build/atlas/chart-graphviz.dot")
    val dotFileB = resolve("b/build/atlas/chart-graphviz.dot")
    val dotFileC = resolve("c/build/atlas/chart-graphviz.dot")

    // and contain expected contents, with projects in declaration order
    assertThat(dotFileA)
      .contentEquals(
        """
        digraph {
          ":a" [fillcolor="mediumorchid"]
          ":b" [fillcolor="orange"]
          ":c" [fillcolor="orange"]
          ":a" -> ":b"
          ":a" -> ":c"
        }
        """
          .trimIndent()
      )

    assertThat(dotFileB)
      .contentEquals(
        """
        digraph {
          ":b" [fillcolor="orange"]
        }
        """
          .trimIndent()
      )
    assertThat(dotFileC)
      .contentEquals(
        """
        digraph {
          ":c" [fillcolor="orange"]
        }
        """
          .trimIndent()
      )
  }

  @Test
  fun `Customise dotfile from build script`() = GraphVizChartCustomConfig {
    // when
    assertThatTask("writeGraphvizChart").buildsSuccessfully()

    // then the file was generated, with projects in alphabetical order
    assertThat(resolve("a/build/atlas/chart-graphviz.dot"))
      .exists()
      .contentEquals(
        """
        digraph {
          edge [arrowhead="halfopen",arrowtail="open"]
          graph [layout="twopi",dpi="150"]
          node [shape="none"]
          ":a" [fillcolor="mediumorchid"]
          ":b" [fillcolor="orange"]
          ":c" [fillcolor="orange"]
          ":a" -> ":b"
          ":a" -> ":c"
        }
        """
          .trimIndent()
      )
  }

  @Test
  fun `Customise dotfile from gradle properties`() = GraphVizChartWithProperties {
    // when
    assertThatTask("writeGraphvizChart").buildsSuccessfully()

    // then the file was generated, with projects in alphabetical order
    assertThat(resolve("a/build/atlas/chart-graphviz.dot"))
      .exists()
      .contentEquals(
        """
        digraph {
          edge [arrowhead="halfopen"]
          graph [layout="neato",dpi="150"]
          node [shape="box"]
          ":a" [fillcolor="mediumorchid"]
          ":b" [fillcolor="orange"]
          ":c" [fillcolor="orange"]
          ":a" -> ":b"
          ":a" -> ":c"
        }
        """
          .trimIndent()
      )
  }

  @Test
  fun `Replace project names`() = GraphVizChartWithReplacements {
    // when
    assertThatTask("writeGraphvizChart").buildsSuccessfully()

    // then the file was generated, with colons removed from project prefixes and "b" -> "B"
    assertThat(resolve("a/build/atlas/chart-graphviz.dot"))
      .exists()
      .contentEquals(
        """
        digraph {
          "B" [fillcolor="orange"]
          "a" [fillcolor="mediumorchid"]
          "c" [fillcolor="orange"]
          "a" -> "B"
          "a" -> "c"
        }
        """
          .trimIndent()
      )
  }

  @Test
  fun `Handle custom link types`() = GraphVizChartWithCustomLinkTypes {
    // when
    assertThatTask("writeGraphvizChart").buildsSuccessfully()

    // then the file was generated, with the expected link styles
    assertThat(resolve("a/build/atlas/chart-graphviz.dot"))
      .exists()
      .contentEquals(
        """
        digraph {
          ":a" [fillcolor="mediumorchid"]
          ":b" [fillcolor="mediumorchid"]
          ":c" [fillcolor="orange"]
          ":d" [fillcolor="orange"]
          ":a" -> ":b" [style="bold"]
          ":a" -> ":c" [color="blue"]
          ":a" -> ":d" [style="dotted",color="#FF55FF"]
        }
        """
          .trimIndent()
      )
  }

  @Test
  fun `Handle nested projects`() = GraphvizNestedProject {
    // when
    assertThatTask("writeGraphvizChart").buildsSuccessfully()

    // then the file was generated, with the expected link styles
    assertThat(resolve("app/build/atlas/chart-graphviz.dot"))
      .exists()
      .contentEquals(
        """
        digraph {
          ":app" [fillcolor="mediumorchid"]
          ":data:a" [fillcolor="mediumorchid"]
          ":data:b" [fillcolor="mediumorchid"]
          ":domain:a" [fillcolor="mediumorchid"]
          ":domain:b" [fillcolor="mediumorchid"]
          ":ui:a" [fillcolor="mediumorchid"]
          ":ui:b" [fillcolor="mediumorchid"]
          ":ui:c" [fillcolor="mediumorchid"]
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
}
