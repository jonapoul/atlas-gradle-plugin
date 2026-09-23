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
import atlas.test.scenarios.ThreeProjectsOnlyMatchingOther
import blueprint.test.Scenario
import blueprint.test.assertThatTask
import blueprint.test.buildsSuccessfully
import blueprint.test.taskHadResult
import blueprint.test.taskSucceeded
import kotlin.test.Test

internal class WriteProjectTypeTest : ScenarioTest() {
  @Test
  fun `No project types declared`() = NoProjectTypesDeclared {
    // when
    assertThatTask("writeProjectType")
      .buildsSuccessfully()
      .taskHadResult(":test-jvm:writeProjectType", SUCCESS)

    // then
    assertThat(projectType("test-jvm")).isEqualTo(TypedProject(":test-jvm", type = null))
  }

  @Test
  fun `Project types declared but none match`() = ProjectTypesDeclaredButNoneMatch {
    // when
    assertThatTask("writeProjectType")
      .buildsSuccessfully()
      .taskHadResult(":test-jvm:writeProjectType", SUCCESS)

    // then
    assertThat(projectType("test-jvm")).isEqualTo(TypedProject(":test-jvm", type = null))
  }

  @Test
  fun `Write file if built-in type matches`() = OneKotlinJvmProject {
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
  fun `Fall back to other if no types match`() = ThreeProjectsOnlyMatchingOther {
    assertThatTask("writeProjectType").buildsSuccessfully()

    assertThat(projectType("a"))
      .isEqualTo(TypedProject(":a", type = ProjectType("Other", color = "gainsboro")))
    assertThat(projectType("b"))
      .isEqualTo(TypedProject(":b", type = ProjectType("Other", color = "gainsboro")))
    assertThat(projectType("c"))
      .isEqualTo(TypedProject(":c", type = ProjectType("Other", color = "gainsboro")))
  }

  private fun Scenario.projectType(path: String) =
    resolve("$path/build/atlas/project-type.json").let(::readProjectType)
}
