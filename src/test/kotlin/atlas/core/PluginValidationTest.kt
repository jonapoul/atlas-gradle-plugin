package atlas.core

import atlas.test.ScenarioTest
import atlas.test.scenarios.ProjectTypeCreated
import atlas.test.scenarios.ProjectTypeRegistered
import atlas.test.scenarios.ProjectTypeWithNoIdentifiers
import blueprint.test.assertThatTask
import blueprint.test.buildsSuccessfully
import blueprint.test.outputContains
import blueprint.test.outputDoesNotContain
import kotlin.test.Test

internal class PluginValidationTest : ScenarioTest() {
  @Test
  fun `Warn if project type is declared with no identifiers`() = ProjectTypeWithNoIdentifiers {
    // when we're not running any of our tasks, then we get a log warning
    assertThatTask("help")
      .buildsSuccessfully()
      .outputContains(
        "Warning: Project type 'custom' will be ignored - you need to set one of " +
          "pathContains, pathMatches or hasPluginId."
      )
  }

  @Test
  fun `Don't warn if project type is created`() = ProjectTypeCreated {
    // when we're not running any of our tasks
    assertThatTask("help").buildsSuccessfully().outputDoesNotContain("Warning")
  }

  @Test
  fun `Don't warn if project type is registered`() = ProjectTypeRegistered {
    // when we're not running any of our tasks
    assertThatTask("help").buildsSuccessfully().outputDoesNotContain("Warning")
  }
}
