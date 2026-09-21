package atlas.d2

import atlas.test.ScenarioTest
import atlas.test.scenarios.D2DownloadPrintingRepositories
import atlas.test.scenarios.D2PathOnly
import blueprint.test.assertThatTask
import blueprint.test.buildsSuccessfully
import blueprint.test.outputContains
import blueprint.test.outputDoesNotContain
import kotlin.test.Test

internal class D2RepositoryTest : ScenarioTest() {
  @Test
  fun `No repository is added when nothing downloads`() =
    runScenario(D2PathOnly) {
      assertThatTask("help")
        .buildsSuccessfully()
        .outputContains("Settings repositories: [Google, MavenRepo]")
        .outputDoesNotContain("atlasD2Releases")
    }

  @Test
  fun `The repository is added when D2 downloads`() =
    runScenario(D2DownloadPrintingRepositories) {
      assertThatTask("help")
        .buildsSuccessfully()
        .outputContains("Settings repositories: [Google, MavenRepo, atlasD2Releases]")
    }
}
