package atlas.core

import assertk.assertThat
import atlas.test.ScenarioTest
import atlas.test.resolve
import atlas.test.scenarios.CheckExplicitlyDisabled
import atlas.test.scenarios.CheckExplicitlyEnabled
import atlas.test.scenarios.D2Basic
import atlas.test.scenarios.GraphVizBasicWithPngOutput
import atlas.test.scenarios.GraphvizBasic
import atlas.test.withIntermediatesInProjectDir
import blueprint.test.assertThatTask
import blueprint.test.buildsSuccessfully
import blueprint.test.contentContains
import blueprint.test.failsBuild
import blueprint.test.outputContains
import blueprint.test.outputContainsLine
import blueprint.test.outputDoesNotContain
import blueprint.test.taskHadResult
import blueprint.test.taskSucceeded
import blueprint.test.withArgument
import org.junit.jupiter.api.Test

internal class CheckFileDiffTest : ScenarioTest() {
  @Test
  fun `Write doesn't run as a dependency of check for graphviz`() =
    runScenario(GraphvizBasic.withIntermediatesInProjectDir()) {
      // when
      assertThatTask(":a:checkGraphvizChart")
        .withArgument("--dry-run")
        .buildsSuccessfully()

        // then the chart wasn't written but the dummy and check tasks were run
        .outputDoesNotContain(":a:writeGraphvizChart")
        .outputContains(":a:writeDummyGraphvizChart")
        .outputContains(":a:checkGraphvizChart")
    }

  @Test
  fun `Write doesn't run as a dependency of check for D2`() =
    runScenario(D2Basic.withIntermediatesInProjectDir()) {
      // when
      assertThatTask(":a:checkD2Chart")
        .withArgument("--dry-run")
        .buildsSuccessfully()

        // then the chart wasn't written but the dummy and check tasks were run
        .outputDoesNotContain(":a:writeD2Chart")
        .outputContains(":a:writeDummyD2Chart")
        .outputContains(":a:checkD2Chart")
    }

  @Test
  fun `Fail if the expected file hasn't been generated yet`() =
    runScenario(GraphvizBasic.withIntermediatesInProjectDir()) {
      assertThatTask(":a:checkGraphvizChart")
        .failsBuild()
        .outputContains(
          """
          * What went wrong:
          Execution failed for task ':a:checkGraphvizChart' (registered by plugin 'dev.jonpoulton.atlas').
          > java.io.FileNotFoundException
          """
            .trimIndent()
        )

      assertThat(resolve("build/reports/problems/problems-report.html"))
        .contentContains("Run `gradle :a:writeGraphvizChart` to generate the file.")
    }

  @Test
  fun `Verify projects of a basic project`() =
    runScenario(GraphVizBasicWithPngOutput.withIntermediatesInProjectDir()) {
      // given initial dotfile is generated
      assertThatTask(":a:writeGraphvizChart").buildsSuccessfully()

      // when we check it with no changes
      assertThatTask(":a:checkGraphvizChart")
        .buildsSuccessfully()
        .taskHadResult(":a:checkGraphvizChart", SUCCESS)

      // given we set a custom property set to adjust the output
      resolve("gradle.properties").writeText("atlas.graphviz.layoutEngine=circo")

      // when we run a check again, then it fails and spits out the expected diff
      assertThatTask(":a:checkGraphvizChart")
        .failsBuild()
        .taskHadResult(":a:checkGraphvizChart", FAILED)
        .outputContains(
          """
          |          digraph {
          |      ---   graph [layout="circo"]
          |            ":a" [fillcolor="mediumorchid"]
          |            ":b" [fillcolor="orange"]
          |            ":c" [fillcolor="orange"]
          |            ":a" -> ":b"
          |            ":a" -> ":c"
          |          }
          """
            .trimMargin()
        )
    }

  @Test
  fun `Verify legend of a basic project`() =
    runScenario(GraphvizBasic.withIntermediatesInProjectDir()) {
      // given initial dotfile is generated
      assertThatTask("writeGraphvizLegend").buildsSuccessfully()

      // when we check it with no changes
      assertThatTask("checkGraphvizLegend")
        .buildsSuccessfully()
        .taskHadResult(":checkGraphvizLegend", SUCCESS)

      // given we manually adjust the generated file
      val legendFile = resolve("atlas/legend-graphviz.dot")
      val editedLegend = legendFile.readText().replace("CELLBORDER=\"1\"", "CELLBORDER=\"100\"")
      legendFile.writeText(editedLegend)

      // when we run a check again, then it fails and spits out the expected diff
      assertThatTask("checkGraphvizLegend")
        .failsBuild()
        .taskHadResult(":checkGraphvizLegend", FAILED)
        .outputContains(
          """
          |          digraph {
          |            node [shape="plaintext"]
          |            projects [label=<
          |      ---   <TABLE BORDER="0" CELLBORDER="1" CELLSPACING="0" CELLPADDING="4">
          |      +++   <TABLE BORDER="0" CELLBORDER="100" CELLSPACING="0" CELLPADDING="4">
          |              <TR><TD COLSPAN="2"><B>Project Types</B></TD></TR>
          |              <TR><TD>Kotlin JVM</TD><TD BGCOLOR="mediumorchid">&lt;project-name&gt;</TD></TR>
          |              <TR><TD>Java</TD><TD BGCOLOR="orange">&lt;project-name&gt;</TD></TR>
          |              <TR><TD>Custom</TD><TD BGCOLOR="#123456">&lt;project-name&gt;</TD></TR>
          |            </TABLE>
          |            >];
          |          }
          """
            .trimMargin()
        )
    }

  @Test
  fun `Register check tasks when checkOutputs is true`() =
    runScenario(CheckExplicitlyEnabled.withIntermediatesInProjectDir()) {
      assertThatTask("check")
        .withArgument("--dry-run")
        .buildsSuccessfully()
        .outputContainsLine(":check SKIPPED")
        .outputContainsLine(":checkGraphvizLegend SKIPPED")
        .outputContainsLine(":a:checkGraphvizChart SKIPPED")
        .outputContainsLine(":b:checkGraphvizChart SKIPPED")
        .outputContainsLine(":c:checkGraphvizChart SKIPPED")
    }

  @Test
  fun `Don't register check tasks when checkOutputs is false`() =
    runScenario(CheckExplicitlyDisabled.withIntermediatesInProjectDir()) {
      assertThatTask("check")
        .withArgument("--dry-run")
        .buildsSuccessfully()
        .outputDoesNotContain("checkGraphvizLegend")
    }

  @Test
  fun `Don't register check tasks when intermediates are in the build dir`() =
    CheckExplicitlyEnabled {
      assertThatTask("check")
        .withArgument("--dry-run")
        .buildsSuccessfully()
        .outputDoesNotContain("checkGraphvizLegend")
        .outputDoesNotContain("checkGraphvizChart")
    }

  @Test
  fun `Run aggregated check task`() =
    runScenario(GraphvizBasic.withIntermediatesInProjectDir()) {
      assertThatTask("atlasGenerate").buildsSuccessfully()

      assertThatTask("atlasCheck")
        .buildsSuccessfully()
        .taskSucceeded(":checkGraphvizLegend")
        .taskSucceeded(":a:checkGraphvizChart")
        .taskSucceeded(":b:checkGraphvizChart")
        .taskSucceeded(":c:checkGraphvizChart")
        .taskSucceeded(":atlasCheck")
    }
}
