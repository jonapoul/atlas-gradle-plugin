package atlas.mermaid

import assertk.assertThat
import atlas.test.ScenarioTest
import atlas.test.resolve
import atlas.test.scenarios.MermaidWithGroupsNested
import atlas.test.scenarios.MermaidWithGroupsNotNested
import atlas.test.scenarios.MermaidWithoutGroups
import blueprint.test.allTasksSuccessful
import blueprint.test.assertThatTask
import blueprint.test.buildsSuccessfully
import blueprint.test.contentEquals
import blueprint.test.exists
import kotlin.test.Test

internal class WriteMermaidChartTest : ScenarioTest() {
  @Test
  fun `Write chart without groups`() =
    runScenario(MermaidWithoutGroups) {
      // when
      assertThatTask(":a:writeMermaidChart").buildsSuccessfully().allTasksSuccessful()

      // then
      assertThat(resolve("a/chart-mermaid.mmd"))
        .exists()
        .contentEquals(
          """
          graph TD
            _a[":a"]
            _b[":b"]
            _c[":c"]
            _a --> _b
            _a --> _c
          """
            .trimIndent()
        )
    }

  @Test
  fun `Write chart with groups enabled but no nested projects`() =
    runScenario(MermaidWithGroupsNotNested) {
      // when
      assertThatTask(":a:writeMermaidChart").buildsSuccessfully().allTasksSuccessful()

      // then
      assertThat(resolve("a/chart-mermaid.mmd"))
        .exists()
        .contentEquals(
          """
          graph TD
            _a[":a"]
            _b[":b"]
            _c[":c"]
            _a --> _b
            _a --> _c
          """
            .trimIndent()
        )
    }

  @Test
  fun `Write chart with groups enabled and projects nested`() =
    runScenario(MermaidWithGroupsNested) {
      // when
      assertThatTask(":a:writeMermaidChart").buildsSuccessfully().allTasksSuccessful()

      // then
      assertThat(resolve("a/chart-mermaid.mmd"))
        .exists()
        .contentEquals(
          """
          graph TD
            _a[":a"]
            subgraph b[":b"]
              _b_b1[":b:b1"]
              _b_b2[":b:b2"]
            end
            subgraph c[":c"]
              _c_c3[":c:c3"]
              subgraph inner[":inner"]
                _c_inner_c1[":c:inner:c1"]
                _c_inner_c2[":c:inner:c2"]
              end
            end
            _a --> _b_b1
            _a --> _b_b2
            _b_b1 --> _c_inner_c1
            _b_b1 --> _c_inner_c2
            _b_b2 --> _c_c3
            _b_b2 --> _c_inner_c2
          """
            .trimIndent()
        )
    }
}
