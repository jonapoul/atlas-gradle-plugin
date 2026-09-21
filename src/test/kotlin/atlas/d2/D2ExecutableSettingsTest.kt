package atlas.d2

import atlas.test.ScenarioTest
import atlas.test.scenarios.D2DownloadPinnedVersion
import atlas.test.scenarios.D2DownloadPrintingRepositories
import atlas.test.scenarios.D2PathOnly
import atlas.test.scenarios.D2PathWithVersion
import blueprint.test.assertThatTask
import blueprint.test.buildsSuccessfully
import blueprint.test.outputContains
import blueprint.test.outputDoesNotContain
import kotlin.test.Test

internal class D2ExecutableSettingsTest : ScenarioTest() {
  @Test
  fun `No repository is added when nothing downloads`() =
    runScenario(D2PathOnly) {
      assertThatTask("help")
        .buildsSuccessfully()
        .outputContains("Settings repositories: [Google, MavenRepo]")
        .outputDoesNotContain("atlasD2Releases")
    }

  @Test
  fun `Warn when d2Version can't be used`() =
    runScenario(D2PathWithVersion) {
      assertThatTask("help")
        .buildsSuccessfully()
        .outputContains(
          "Warning: d2Version is set to 0.7.1, but executableSource is Path, so it's ignored"
        )
    }

  @Test
  fun `No warning when d2Version is used`() =
    runScenario(D2DownloadPinnedVersion) {
      assertThatTask("help").buildsSuccessfully().outputDoesNotContain("Warning")
    }

  @Test
  fun `The repository is added when D2 downloads`() =
    runScenario(D2DownloadPrintingRepositories) {
      assertThatTask("help")
        .buildsSuccessfully()
        .outputContains("Settings repositories: [Google, MavenRepo, atlasD2Releases]")
    }
}
