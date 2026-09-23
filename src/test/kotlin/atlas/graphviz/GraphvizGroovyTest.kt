package atlas.graphviz

import atlas.test.ScenarioTest
import atlas.test.scenarios.GroovyGraphVizFull
import atlas.test.scenarios.GroovyGraphVizProjectTypes
import blueprint.test.assertThatTask
import blueprint.test.buildsSuccessfully
import blueprint.test.taskSucceeded
import org.junit.jupiter.api.Test

internal class GraphvizGroovyTest : ScenarioTest() {
  @Test
  @RequiresGraphviz
  fun `Configure graphviz project types`() = GroovyGraphVizProjectTypes {
    // when
    assertThatTask("atlasGenerate")
      .buildsSuccessfully()

      // then
      .taskSucceeded(":a:atlasGenerate")
      .taskSucceeded(":b:atlasGenerate")
      .taskSucceeded(":c:atlasGenerate")
  }

  @Test
  @RequiresGraphviz
  fun `Configure graphviz with everything`() = GroovyGraphVizFull {
    // when
    assertThatTask("atlasGenerate")
      .buildsSuccessfully()

      // then
      .taskSucceeded(":a:atlasGenerate")
      .taskSucceeded(":b:atlasGenerate")
      .taskSucceeded(":c:atlasGenerate")
  }
}
