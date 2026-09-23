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
  fun `Generate charts for every configured framework`() = MultipleFrameworks {
    // when
    assertThatTask("atlasGenerate").buildsSuccessfully().noTasksFailed()

    // then each framework wrote to its own directory, so nothing was overwritten, and the shared
    // legends live in the root project
    assertThat(rootDir)
      .childExists("a/build/atlas/chart-d2.d2")
      .childExists("a/build/atlas/chart-graphviz.dot")
      .childExists("a/chart-mermaid.mmd")
      .childExists("build/atlas/classes-d2.d2")
      .childExists("build/atlas/legend-graphviz.dot")
      .childExists("atlas/legend-mermaid.md")
  }

  @Test
  fun `Write every framework's diagram into a single readme`() = MultipleFrameworks {
    // when
    assertThatTask("atlasGenerate").buildsSuccessfully()

    // then
    assertThat(resolve("a/README.md"))
      .contentContains("![chart](chart-d2.svg)")
      .contentContains("![chart](chart-graphviz.svg)")
      .contentContains("```mermaid")
  }
}
