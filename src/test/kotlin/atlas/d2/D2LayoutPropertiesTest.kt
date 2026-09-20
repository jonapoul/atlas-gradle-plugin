package atlas.d2

import assertk.assertThat
import assertk.assertions.doesNotContain
import atlas.test.ScenarioTest
import atlas.test.contains
import atlas.test.resolve
import atlas.test.scenarios.D2LayoutFromProperties
import blueprint.test.assertThatTask
import blueprint.test.buildsSuccessfully
import blueprint.test.contentContains
import blueprint.test.taskSucceeded
import kotlin.test.Test

internal class D2LayoutPropertiesTest : ScenarioTest() {
  @Test
  fun `Configure the layout engine through gradle properties`() =
    runScenario(D2LayoutFromProperties) {
      // when
      assertThatTask(":a:execD2Chart").buildsSuccessfully().taskSucceeded(":a:execD2Chart")

      // then the engine itself came from atlas.d2.layoutEngine
      assertThat(resolve("build/atlas/classes-d2.d2")).contentContains("layout-engine: elk")

      // and its sub-properties reached the command, which is echo'd here
      val args = resolve("a/chart-d2.txt").readText()
      assertThat(args)
        .contains("--elk-algorithm=mrtree")
        .contains("--elk-nodeNodeBetweenLayers=70")
        // set in the DSL as well, so the DSL value wins
        .contains("--elk-nodeSelfLoop=25")
        .doesNotContain("--elk-nodeSelfLoop=99")
    }
}
