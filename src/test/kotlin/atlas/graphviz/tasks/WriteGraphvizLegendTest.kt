package atlas.graphviz.tasks

import assertk.assertThat
import atlas.test.ScenarioTest
import atlas.test.resolve
import atlas.test.scenarios.GraphVizWithLinkTypes
import atlas.test.scenarios.GraphvizBasic
import blueprint.test.assertThatTask
import blueprint.test.buildsSuccessfully
import blueprint.test.contentContains
import blueprint.test.contentEquals
import blueprint.test.exists
import kotlin.test.Test

internal class WriteGraphvizLegendTest : ScenarioTest() {
  @Test
  fun `Generate dotfile legend from basic config`() =
    runScenario(GraphvizBasic) {
      // when
      assertThatTask("writeGraphvizLegend").buildsSuccessfully()

      // then the file was generated, with projects in declaration order
      assertThat(resolve("build/atlas/graphviz/legend.dot"))
        .exists()
        .contentContains(
          """
          digraph {
            node [shape="plaintext"]
            projects [label=<
            <TABLE BORDER="0" CELLBORDER="1" CELLSPACING="0" CELLPADDING="4">
              <TR><TD COLSPAN="2"><B>Project Types</B></TD></TR>
              <TR><TD>Kotlin JVM</TD><TD BGCOLOR="mediumorchid">&lt;project-name&gt;</TD></TR>
              <TR><TD>Java</TD><TD BGCOLOR="orange">&lt;project-name&gt;</TD></TR>
              <TR><TD>Custom</TD><TD BGCOLOR="#123456">&lt;project-name&gt;</TD></TR>
            </TABLE>
            >];
          }
          """
            .trimIndent()
        )
    }

  @Test
  fun `Show projects and links next to each other`() =
    runScenario(GraphVizWithLinkTypes) {
      // when
      assertThatTask("writeGraphvizLegend").buildsSuccessfully()

      // then the file was generated, overriding build script
      assertThat(resolve("build/atlas/graphviz/legend.dot"))
        .exists()
        .contentEquals(
          """
          digraph {
            node [shape="plaintext"]
            projects [label=<
            <TABLE BORDER="0" CELLBORDER="1" CELLSPACING="0" CELLPADDING="4">
              <TR><TD COLSPAN="2"><B>Project Types</B></TD></TR>
              <TR><TD>Kotlin JVM</TD><TD BGCOLOR="mediumorchid">&lt;project-name&gt;</TD></TR>
              <TR><TD>Java</TD><TD BGCOLOR="orange">&lt;project-name&gt;</TD></TR>
            </TABLE>
            >];
            links [label=<
            <TABLE BORDER="0" CELLBORDER="1" CELLSPACING="0" CELLPADDING="4">
              <TR><TD COLSPAN="2"><B>Link Types</B></TD></TR>
              <TR><TD>jvmMainImplementation</TD><TD BGCOLOR="orange">Bold</TD></TR>
              <TR><TD>api</TD><TD>Solid</TD></TR>
              <TR><TD>implementation</TD><TD>Dotted</TD></TR>
            </TABLE>
            >];
          }
          """
            .trimIndent()
        )
    }
}
