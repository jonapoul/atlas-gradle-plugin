package atlas.d2.tasks

import assertk.assertThat
import atlas.test.ScenarioTest
import atlas.test.resolve
import atlas.test.scenarios.D2AllProjectTypes
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
      assertThat(resolve("build/atlas/d2/classes.d2"))
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
}
