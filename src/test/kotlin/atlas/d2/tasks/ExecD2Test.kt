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
import atlas.test.scenarios.D2FailOnProjectRepos
import atlas.test.scenarios.D2PinnedVersion
import atlas.test.scenarios.D2WithProjectRepositories
import atlas.test.scenarios.GroovyD2PreferSettings
import blueprint.test.Scenario as RunningScenario
import blueprint.test.allTasksSuccessful
import blueprint.test.assertThatTask
import blueprint.test.buildsSuccessfully
import blueprint.test.childExists
import blueprint.test.outputContains
import blueprint.test.outputDoesNotContain
import blueprint.test.taskHadResult
import blueprint.test.withArgument
import blueprint.test.withGradleProperty
import java.io.File
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
        .childExists("a/chart-d2.svg")
        .childExists("b/chart-d2.svg")
        .childExists("c/chart-d2.svg")
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
        .withGradleProperty("atlas.d2.theme", 7)
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
      val args = resolve("a/chart-d2.txt").readText()
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

  @Test
  fun `Download d2 instead of using the PATH`() = runScenario(D2Basic) { assertD2Downloaded() }

  @Test
  fun `Download d2 when its version is set`() =
    runScenario(D2PinnedVersion) { assertD2Downloaded(source = "auto") }

  @Test
  fun `Download d2 when settings repositories are preferred`() =
    runScenario(GroovyD2PreferSettings) { assertD2Downloaded() }

  @Test
  @RequiresD2
  fun `Use d2 from the PATH by default`() =
    runScenario(D2Basic) {
      assertThatTask(":a:execD2Chart")
        .withArgument("--info")
        .buildsSuccessfully()
        .outputContains("Starting d2: '[${d2OnPath()}")
        .outputDoesNotContain("/transformed/d2")
    }

  private fun d2OnPath(): String =
    System.getenv("PATH")
      .split(File.pathSeparator)
      .map { File(it, "d2") }
      .first { it.isFile && it.canExecute() }
      .absolutePath

  @Test
  fun `Download d2 when projects declare their own repositories`() =
    runScenario(D2WithProjectRepositories) { assertD2Downloaded() }

  @Test
  fun `Download d2 when project repositories are forbidden`() =
    runScenario(D2FailOnProjectRepos) { assertD2Downloaded() }

  private fun RunningScenario.assertD2Downloaded(source: String = "download") {
    // when
    assertThatTask(":a:execD2Chart")
      .withGradleProperty("atlas.d2.executableSource", source)
      .withArgument("--info")
      .buildsSuccessfully()
      .taskHadResult(":a:execD2Chart", SUCCESS)
      // then the binary unpacked from the download ran, not whatever's on the PATH
      .outputContains("/transformed/d2")

    assertThat(rootDir).childExists("a/chart-d2.svg")

    // and a second run is up to date
    assertThatTask(":a:execD2Chart")
      .withGradleProperty("atlas.d2.executableSource", source)
      .buildsSuccessfully()
      .taskHadResult(":a:execD2Chart", UP_TO_DATE)
  }
}
