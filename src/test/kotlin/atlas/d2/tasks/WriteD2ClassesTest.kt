package atlas.d2.tasks

import assertk.assertThat
import atlas.test.ScenarioTest
import atlas.test.contains
import atlas.test.resolve
import atlas.test.scenarios.D2AllProjectTypes
import atlas.test.scenarios.D2ThemeOverrides
import blueprint.test.assertThatTask
import blueprint.test.buildsSuccessfully
import blueprint.test.contentEquals
import blueprint.test.taskSucceeded
import kotlin.test.Test

internal class WriteD2ClassesTest : ScenarioTest() {
  @Test
  fun `Generate classes from all default types`() =
    runScenario(D2AllProjectTypes) {
      // when
      assertThatTask("writeD2Classes").buildsSuccessfully().taskSucceeded(":writeD2Classes")

      // and the file was generated
      assertThat(resolve("build/atlas/classes-d2.d2"))
        .contentEquals(
          """
          classes: {
            project-AndroidApp {
              style.fill: "limegreen"
            }
            project-KotlinMultiplatform {
              style.fill: "mediumslateblue"
            }
            project-AndroidLibrary {
              style.fill: "lightgreen"
            }
            project-KotlinJVM {
              style.fill: "mediumorchid"
            }
            project-Java {
              style.fill: "orange"
            }
            project-Other {
              style.fill: "gainsboro"
            }
            container {
            }
            hidden {
              style.opacity: 0
            }
          }
          """
            .trimIndent()
        )
    }

  @Test
  fun `Write theme overrides`() =
    runScenario(D2ThemeOverrides) {
      // when
      assertThatTask("writeD2Classes").buildsSuccessfully().taskSucceeded(":writeD2Classes")

      // then
      assertThat(resolve("build/atlas/classes-d2.d2").readText())
        .contains("theme-id: 7")
        .contains(
          """
          theme-overrides: {
            B2: "orange"
            N1: "#123456"
          }
          """
            .trimIndent()
            .prependIndent("    ")
        )
        .contains(
          """
          dark-theme-overrides: {
            AA4: "#abcdef"
          }
          """
            .trimIndent()
            .prependIndent("    ")
        )
    }
}
