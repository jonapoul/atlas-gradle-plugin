package atlas

import assertk.assertThat
import atlas.test.ScenarioTest
import atlas.test.resolve
import atlas.test.scenarios.MultipleFrameworks
import blueprint.test.assertThatTask
import blueprint.test.buildsSuccessfully
import blueprint.test.childExists
import blueprint.test.contentContains
import blueprint.test.noTasksFailed
import kotlin.test.Test

internal class MultipleFrameworksTest : ScenarioTest() {
  @Test
  fun `Generate charts for every configured framework`() =
    runScenario(MultipleFrameworks) {
      // when
      assertThatTask("atlasGenerate").buildsSuccessfully().noTasksFailed()

      // then each framework wrote to its own directory, so nothing was overwritten, and the shared
      // legends live in the root project
      assertThat(rootDir)
        .childExists("a/build/atlas/d2/chart.d2")
        .childExists("a/build/atlas/graphviz/chart.dot")
        .childExists("a/atlas/mermaid/chart.mmd")
        .childExists("build/atlas/d2/classes.d2")
        .childExists("build/atlas/graphviz/legend.dot")
        .childExists("atlas/mermaid/legend.md")
    }

  @Test
  fun `Write every framework's diagram into a single readme`() =
    runScenario(MultipleFrameworks) {
      // when
      assertThatTask("atlasGenerate").buildsSuccessfully()

      // then
      assertThat(resolve("a/README.md"))
        .contentContains("![chart](atlas/d2/chart.svg)")
        .contentContains("![chart](atlas/graphviz/chart.svg)")
        .contentContains("```mermaid")
    }
}
