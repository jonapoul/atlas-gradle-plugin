package atlas.core

import assertk.assertThat
import assertk.assertions.isEqualTo
import atlas.core.internal.TypedProject
import atlas.core.internal.readProjectType
import atlas.test.ScenarioTest
import atlas.test.resolve
import atlas.test.scenarios.NoProjectTypesDeclared
import atlas.test.scenarios.OneKotlinJvmProject
import atlas.test.scenarios.ProjectTypesDeclaredButNoneMatch
import atlas.test.scenarios.ThreeProjectWithCustomTypes
import atlas.test.scenarios.ThreeProjectsNoMatchingType
import atlas.test.scenarios.ThreeProjectsOnlyMatchingOther
import blueprint.test.Scenario
import blueprint.test.assertThatTask
import blueprint.test.buildsSuccessfully
import blueprint.test.taskHadResult
import blueprint.test.taskSucceeded
import kotlin.test.Test

internal class WriteProjectTypeTest : ScenarioTest() {
  @Test
  fun `No project types declared`() =
    runScenario(NoProjectTypesDeclared) {
      // when
      assertThatTask("writeProjectType")
        .buildsSuccessfully()
        .taskHadResult(":test-jvm:writeProjectType", SUCCESS)

      // then
      assertThat(projectType("test-jvm")).isEqualTo(TypedProject(":test-jvm", type = null))
    }

  @Test
  fun `Project types declared but none match`() =
    runScenario(ProjectTypesDeclaredButNoneMatch) {
      // when
      assertThatTask("writeProjectType")
        .buildsSuccessfully()
        .taskHadResult(":test-jvm:writeProjectType", SUCCESS)

      // then
      assertThat(projectType("test-jvm")).isEqualTo(TypedProject(":test-jvm", type = null))
    }

  @Test
  fun `Write file if built-in type matches`() =
    runScenario(OneKotlinJvmProject) {
      // when
      assertThatTask("writeProjectType")
        .buildsSuccessfully()
        .taskSucceeded(":test-jvm:writeProjectType")

      // then
      assertThat(projectType("test-jvm"))
        .isEqualTo(
          TypedProject(":test-jvm", type = ProjectType("Kotlin JVM", color = "mediumorchid"))
        )

      // when running again, then it's cached
      assertThatTask("writeProjectType")
        .buildsSuccessfully()
        .taskHadResult(":test-jvm:writeProjectType", UP_TO_DATE)
    }

  @Test
  fun `Write files if custom types match`() =
    runScenario(ThreeProjectWithCustomTypes) {
      assertThatTask("writeProjectType")
        .buildsSuccessfully()
        .taskSucceeded(":test-data:writeProjectType")
        .taskSucceeded(":test-domain:writeProjectType")
        .taskSucceeded(":test-ui:writeProjectType")

      assertThat(projectType("test-data"))
        .isEqualTo(TypedProject(":test-data", type = ProjectType("Data", color = "#ABC123")))
      assertThat(projectType("test-domain"))
        .isEqualTo(TypedProject(":test-domain", type = ProjectType("Domain", color = "#123ABC")))
      assertThat(projectType("test-ui"))
        .isEqualTo(TypedProject(":test-ui", type = ProjectType("Android", color = "#A1B2C3")))
    }

  @Test
  fun `Fall back to other if no types match`() =
    runScenario(ThreeProjectsOnlyMatchingOther) {
      assertThatTask("writeProjectType").buildsSuccessfully()

      assertThat(projectType("a"))
        .isEqualTo(TypedProject(":a", type = ProjectType("Other", color = "gainsboro")))
      assertThat(projectType("b"))
        .isEqualTo(TypedProject(":b", type = ProjectType("Other", color = "gainsboro")))
      assertThat(projectType("c"))
        .isEqualTo(TypedProject(":c", type = ProjectType("Other", color = "gainsboro")))
    }

  @Test
  fun `No types match`() =
    runScenario(ThreeProjectsNoMatchingType) {
      assertThatTask("a:writeProjectType")
        .buildsSuccessfully()
        .taskHadResult(":a:writeProjectType", SUCCESS)

      assertThat(projectType("a")).isEqualTo(TypedProject(":a", type = null))
    }

  private fun Scenario.projectType(path: String) =
    resolve("$path/build/atlas/project-type.json").let(::readProjectType)
}
