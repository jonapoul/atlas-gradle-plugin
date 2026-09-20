package atlas.d2.tasks

import assertk.assertThat
import atlas.d2.RequiresD2
import atlas.test.ScenarioTest
import atlas.test.resolve
import atlas.test.scenarios.D2Basic
import atlas.test.scenarios.D2NestedProjects
import atlas.test.withIntermediatesInProjectDir
import blueprint.test.allTasksSuccessful
import blueprint.test.assertThatTask
import blueprint.test.buildsSuccessfully
import blueprint.test.childExists
import blueprint.test.contentEquals
import blueprint.test.exists
import blueprint.test.noTasksFailed
import blueprint.test.taskSucceeded
import kotlin.test.Test

internal class WriteD2ChartTest : ScenarioTest() {
  @Test
  fun `Generate charts from basic config`() =
    runScenario(D2Basic) {
      // when
      assertThatTask("writeD2Chart").buildsSuccessfully().allTasksSuccessful()

      // and the files were generated
      val d2FileA = resolve("a/build/atlas/d2/chart.d2")
      val d2FileB = resolve("b/build/atlas/d2/chart.d2")
      val d2FileC = resolve("c/build/atlas/d2/chart.d2")

      // and contain expected contents, with projects in declaration order
      assertThat(d2FileA)
        .contentEquals(
          """
          a: :a { class: project-KotlinJVM }
          b: :b { class: project-Java }
          c: :c { class: project-Java }
          a -> b
          a -> c
          vars: {
            d2-legend: {
              project-KotlinJVM: Kotlin JVM { class: project-KotlinJVM }
              project-Java: Java { class: project-Java }
            }
          }
          ...@../../../../build/atlas/d2/classes.d2
          """
            .trimIndent()
        )

      assertThat(d2FileB)
        .contentEquals(
          """
          b: :b { class: project-Java }
          vars: {
            d2-legend: {
              project-Java: Java { class: project-Java }
            }
          }
          ...@../../../../build/atlas/d2/classes.d2
          """
            .trimIndent()
        )

      assertThat(d2FileC)
        .contentEquals(
          """
          c: :c { class: project-Java }
          vars: {
            d2-legend: {
              project-Java: Java { class: project-Java }
            }
          }
          ...@../../../../build/atlas/d2/classes.d2
          """
            .trimIndent()
        )
    }

  @Test
  @RequiresD2
  fun `Write correct classes file path for nested projects`() =
    runScenario(D2NestedProjects) {
      // when
      assertThatTask("atlasGenerate").buildsSuccessfully().noTasksFailed()

      // and the files were generated
      assertThat(rootDir).childExists("build/atlas/d2/classes.d2")
      assertThat(resolve("path/to/my/project/build/atlas/d2/chart.d2"))
        .exists()
        .contentEquals(
          """
          path_to_my_project: :path:to:my:project
          ...@../../../../../../../build/atlas/d2/classes.d2
          """
            .trimIndent()
        )
    }

  @Test
  @RequiresD2
  fun `Check nested projects with intermediates in the project dir`() =
    runScenario(D2NestedProjects.withIntermediatesInProjectDir()) {
      // given
      assertThatTask("atlasGenerate").buildsSuccessfully().noTasksFailed()
      assertThat(resolve("path/to/my/project/atlas/d2/chart.d2"))
        .exists()
        .contentEquals(
          """
          path_to_my_project: :path:to:my:project
          ...@../../../../../../atlas/d2/classes.d2
          """
            .trimIndent()
        )

      // when we check, then the dummy chart matches despite living at a different depth
      assertThatTask("check").buildsSuccessfully().taskSucceeded(":path:to:my:project:checkD2Chart")
    }
}
