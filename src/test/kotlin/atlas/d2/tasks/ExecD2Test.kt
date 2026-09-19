package atlas.d2.tasks

import assertk.assertThat
import assertk.assertions.doesNotContain
import atlas.d2.RequiresD2
import atlas.test.ScenarioTest
import atlas.test.contains
import atlas.test.resolve
import atlas.test.scenarios.D2Basic
import atlas.test.scenarios.D2CliFlags
import atlas.test.scenarios.D2CustomLayoutEngine
import blueprint.test.allTasksSuccessful
import blueprint.test.assertThatTask
import blueprint.test.buildsSuccessfully
import blueprint.test.childExists
import blueprint.test.outputContains
import blueprint.test.taskHadResult
import blueprint.test.withArgument
import kotlin.test.Test

internal class ExecD2Test : ScenarioTest() {
  @Test
  fun `No extras are generated if no file formats have been declared`() =
    runScenario(D2Basic) {
      // when, then no PNGs, SVGs, or anything else were generated besides the dotfile
      assertThatTask("atlasGenerate")
        .withArgument("--dry-run")
        .buildsSuccessfully()
        .outputContains(
          """
          :writeD2Classes SKIPPED
          :a:writeProjectType SKIPPED
          :b:writeProjectType SKIPPED
          :c:writeProjectType SKIPPED
          :collateProjectTypes SKIPPED
          :a:writeProjectLinks SKIPPED
          :b:writeProjectLinks SKIPPED
          :c:writeProjectLinks SKIPPED
          :collateProjectLinks SKIPPED
          :a:writeProjectTree SKIPPED
          :a:writeD2Chart SKIPPED
          :a:execD2Chart SKIPPED
          :a:writeReadme SKIPPED
          :a:atlasGenerate SKIPPED
          :b:writeProjectTree SKIPPED
          :b:writeD2Chart SKIPPED
          :b:execD2Chart SKIPPED
          :b:writeReadme SKIPPED
          :b:atlasGenerate SKIPPED
          :c:writeProjectTree SKIPPED
          :c:writeD2Chart SKIPPED
          :c:execD2Chart SKIPPED
          :c:writeReadme SKIPPED
          :c:atlasGenerate SKIPPED

          BUILD SUCCESSFUL
          """
            .trimIndent()
        )
    }

  @Test
  @RequiresD2
  fun `Choose custom layout engine`() =
    runScenario(D2CustomLayoutEngine) {
      // when we specify the "neato" layout engine
      assertThatTask("execD2Chart").buildsSuccessfully().allTasksSuccessful()

      // then
      assertThat(rootDir)
        .childExists("a/atlas/d2/chart.svg")
        .childExists("b/atlas/d2/chart.svg")
        .childExists("c/atlas/d2/chart.svg")
    }

  @Test
  @RequiresD2
  fun `Rerun when changing properties in the classes file`() =
    runScenario(D2CustomLayoutEngine) {
      // First run - all tasks run
      assertThatTask(":a:execD2Chart")
        .buildsSuccessfully()
        .taskHadResult(":writeD2Classes", SUCCESS)
        .taskHadResult(":a:writeD2Chart", SUCCESS)
        .taskHadResult(":a:execD2Chart", SUCCESS)

      // Second run with no changes - skipped
      assertThatTask(":a:execD2Chart")
        .buildsSuccessfully()
        .taskHadResult(":writeD2Classes", UP_TO_DATE)
        .taskHadResult(":a:writeD2Chart", UP_TO_DATE)
        .taskHadResult(":a:execD2Chart", UP_TO_DATE)

      // Third run setting a property to change the classes file - classes are written, chart is not
      // but the output file is regenerated
      assertThatTask(":a:execD2Chart")
        .withArgument("-Patlas.d2.theme=7")
        .buildsSuccessfully()
        .taskHadResult(":writeD2Classes", SUCCESS)
        .taskHadResult(":a:writeD2Chart", UP_TO_DATE)
        .taskHadResult(":a:execD2Chart", SUCCESS)
    }

  @Test
  fun `Pass extra CLI flags to D2`() =
    runScenario(D2CliFlags) {
      rootDir.resolve("font.ttf").writeText("")

      // when
      assertThatTask(":a:execD2Chart").buildsSuccessfully().taskHadResult(":a:execD2Chart", SUCCESS)

      // then the flags reached the command, which is echo'd here
      val args = resolve("a/atlas/d2/chart.txt").readText()
      val font = rootDir.resolve("font.ttf").absolutePath
      assertThat(args)
        .contains("--ascii-mode=standard")
        .contains("--omit-version=true")
        .contains("--timeout=300")
        .contains("--font-regular=$font")
        .contains("--font-mono-bold=$font")
        .contains("--elk-nodeSelfLoop=50")
        // only used for SVGs
        .doesNotContain("--no-xml-tag")
    }
}
