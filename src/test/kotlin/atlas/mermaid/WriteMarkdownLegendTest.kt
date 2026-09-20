package atlas.mermaid

import assertk.assertThat
import atlas.test.ScenarioTest
import atlas.test.resolve
import atlas.test.scenarios.MermaidWithLinkTypes
import atlas.test.scenarios.MermaidWithProjectTypes
import blueprint.test.allTasksSuccessful
import blueprint.test.assertThatTask
import blueprint.test.buildsSuccessfully
import blueprint.test.contentContains
import blueprint.test.contentEquals
import blueprint.test.exists
import kotlin.test.Test

internal class WriteMarkdownLegendTest : ScenarioTest() {
  @Test
  fun `Write markdown legend with no link types`() =
    runScenario(MermaidWithProjectTypes) {
      // when
      assertThatTask(":writeMermaidLegend").buildsSuccessfully().allTasksSuccessful()

      // then
      assertThat(resolve("atlas/legend-mermaid.md"))
        .exists()
        .contentContains(
          """
          | Project Types | Color |
          |:--:|:--:|
          | Kotlin JVM | <img src="https://img.shields.io/badge/-%20-mediumorchid?style=flat-square" height="30" width="100"> |
          | Java | <img src="https://img.shields.io/badge/-%20-orange?style=flat-square" height="30" width="100"> |
          """
            .trimIndent()
        )
    }

  @Test
  fun `Write markdown legend with no project types`() =
    runScenario(MermaidWithLinkTypes) {
      // when
      assertThatTask(":writeMermaidLegend").buildsSuccessfully().allTasksSuccessful()

      // then
      assertThat(resolve("atlas/legend-mermaid.md"))
        .exists()
        .contentEquals(
          """
          | Link Types | Style |
          |:--:|:--:|
          | api | Green |
          | implementation | #5555FF |
          | compileOnly | Yellow Dashed |
          """
            .trimIndent()
        )
    }
}
