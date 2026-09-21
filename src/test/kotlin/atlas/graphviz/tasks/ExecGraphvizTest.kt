package atlas.graphviz.tasks

import assertk.assertThat
import assertk.assertions.isEqualTo
import atlas.graphviz.RequiresGraphviz
import atlas.test.RequiresLn
import atlas.test.RequiresWhereis
import atlas.test.ScenarioTest
import atlas.test.resolve
import atlas.test.scenarios.GraphVizBasicWithPngOutput
import atlas.test.scenarios.GraphVizCustomDotExecutable
import atlas.test.scenarios.GraphVizCustomLayoutEngine
import atlas.test.scenarios.GraphvizBasic
import blueprint.test.allTasksSuccessful
import blueprint.test.assertThatTask
import blueprint.test.buildsSuccessfully
import blueprint.test.childDoesNotExist
import blueprint.test.childExists
import blueprint.test.exists
import blueprint.test.failsBuild
import blueprint.test.noTasksFailed
import blueprint.test.outputContains
import blueprint.test.outputContainsMatch
import blueprint.test.tasksSucceeded
import blueprint.test.withArgument
import kotlin.test.Test

internal class ExecGraphvizTest : ScenarioTest() {
  @Test
  fun `No extras are generated if no file formats have been declared`() = GraphvizBasic {
    // when, then no PNGs, SVGs, or anything else were generated besides the dotfile
    assertThatTask("atlasGenerate")
      .withArgument("--dry-run")
      .buildsSuccessfully()
      .outputContains(
        """
        :a:writeProjectType SKIPPED
        :b:writeProjectType SKIPPED
        :c:writeProjectType SKIPPED
        :collateProjectTypes SKIPPED
        :a:writeProjectLinks SKIPPED
        :b:writeProjectLinks SKIPPED
        :c:writeProjectLinks SKIPPED
        :collateProjectLinks SKIPPED
        :a:writeProjectTree SKIPPED
        :a:writeGraphvizChart SKIPPED
        :a:execGraphvizChart SKIPPED
        :writeGraphvizLegend SKIPPED
        :execGraphvizLegend SKIPPED
        :a:writeReadme SKIPPED
        :a:atlasGenerate SKIPPED
        :b:writeProjectTree SKIPPED
        :b:writeGraphvizChart SKIPPED
        :b:execGraphvizChart SKIPPED
        :b:writeReadme SKIPPED
        :b:atlasGenerate SKIPPED
        :c:writeProjectTree SKIPPED
        :c:writeGraphvizChart SKIPPED
        :c:execGraphvizChart SKIPPED
        :c:writeReadme SKIPPED
        :c:atlasGenerate SKIPPED

        BUILD SUCCESSFUL
        """
          .trimIndent()
      )
  }

  @Test
  @RequiresGraphviz
  fun `Generate png file`() = GraphVizBasicWithPngOutput {
    // when, then PNG, SVG and EPS tasks were run for each subproject
    assertThatTask("atlasGenerate")
      .buildsSuccessfully()
      .tasksSucceeded(
        ":a:execGraphvizChart",
        ":b:execGraphvizChart",
        ":c:execGraphvizChart",
      )

    // and the relevant files exist
    assertThat(rootDir)
      .childExists("a/chart-graphviz.png")
      .childExists("b/chart-graphviz.png")
      .childExists("c/chart-graphviz.png")
  }

  @Test
  @RequiresGraphviz
  fun `Choose custom layout engine`() = GraphVizCustomLayoutEngine {
    // when we specify the "neato" layout engine
    assertThatTask(":app:execGraphvizChart").buildsSuccessfully().allTasksSuccessful()
  }

  @Test
  @RequiresGraphviz
  @RequiresLn
  @RequiresWhereis
  fun `Use custom path to dot command`() = GraphVizCustomDotExecutable {
    // Given we've made a symbolic link to a dot executable
    val whereisProcess = ProcessBuilder("whereis", "dot").start()
    val pathToDot = whereisProcess.inputReader().readLine().split(" ")[1]
    assertThat(whereisProcess.waitFor()).isEqualTo(0)
    val customDotFile = resolve("path/to/custom/dot")
    customDotFile.parentFile.mkdirs()
    val lnProcess = ProcessBuilder("ln", "-s", pathToDot, customDotFile.absolutePath).start()
    assertThat(lnProcess.waitFor()).isEqualTo(0)
    assertThat(customDotFile).exists()

    // when
    assertThatTask("atlasGenerate").buildsSuccessfully().noTasksFailed()

    // if we don't add this we'll get a junit log warning
    customDotFile.delete()
  }

  @Test
  @RequiresGraphviz
  fun `Fail with nonexistent custom path to dot command`() = GraphVizCustomDotExecutable {
    // Given we've made a symbolic link to a dot executable which doesn't exist
    assertThat(rootDir).childDoesNotExist("path/to/custom/dot")

    // when, then it fails as expected
    assertThatTask("atlasGenerate")
      .failsBuild()
      .outputContainsMatch(
        """
        > A problem occurred starting process 'command '.*?/path/to/custom/dot''
        """
          .trimIndent()
          .toRegex()
      )
  }
}
