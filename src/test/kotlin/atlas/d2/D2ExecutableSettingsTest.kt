package atlas.d2

import atlas.test.ScenarioTest
import atlas.test.scenarios.D2PinnedVersion
import atlas.test.scenarios.D2PrintingRepositories
import blueprint.test.assertThatTask
import blueprint.test.buildsSuccessfully
import blueprint.test.outputContains
import blueprint.test.outputDoesNotContain
import blueprint.test.withGradleProperty
import kotlin.test.Test

internal class D2ExecutableSettingsTest : ScenarioTest() {
  @Test
  fun `No repository is added when nothing downloads`() =
    runScenario(D2PrintingRepositories) {
      assertThatTask("help")
        .withGradleProperty("atlas.d2.executableSource", "path")
        .buildsSuccessfully()
        .outputContains("Settings repositories: [Google, MavenRepo]")
        .outputDoesNotContain("atlasD2Releases")
    }

  @Test
  fun `Warn when d2Version can't be used`() =
    runScenario(D2PinnedVersion) {
      assertThatTask("help")
        .withGradleProperty("atlas.d2.executableSource", "path")
        .buildsSuccessfully()
        .outputContains(
          "Warning: d2Version is set to 0.9.0, but executableSource is Path, so it's ignored"
        )
    }

  @Test
  fun `No warning when d2Version is used`() =
    runScenario(D2PinnedVersion) {
      assertThatTask("help").buildsSuccessfully().outputDoesNotContain("Warning")
    }

  @Test
  fun `The repository is added when D2 downloads`() =
    runScenario(D2PrintingRepositories) {
      assertThatTask("help")
        .withGradleProperty("atlas.d2.executableSource", "download")
        .buildsSuccessfully()
        .outputContains("Settings repositories: [Google, MavenRepo, atlasD2Releases]")
    }
}
