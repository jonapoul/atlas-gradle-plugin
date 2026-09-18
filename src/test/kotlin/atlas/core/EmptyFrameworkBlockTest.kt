package atlas.core

import assertk.assertThat
import atlas.test.ScenarioTest
import atlas.test.resolve
import atlas.test.scenarios.GroovyD2Basic
import atlas.test.scenarios.GroovyGraphVizBasic
import atlas.test.scenarios.GroovyMermaidBasic
import blueprint.test.allTasksSuccessful
import blueprint.test.assertThatTask
import blueprint.test.buildsSuccessfully
import blueprint.test.exists
import kotlin.test.Test

/**
 * Groovy settings scripts can't use Kotlin default parameter values, so they need a real zero-arg
 * overload of `d2()`/`graphviz()`/`mermaid()` to call. These scenarios have no `atlasConfig` at
 * all, so they only exercise that bare call.
 */
internal class EmptyFrameworkBlockTest : ScenarioTest() {
  @Test
  fun `Bare d2() with no config registers D2 generation`() =
    runScenario(GroovyD2Basic) {
      // when
      assertThatTask("writeD2Chart").buildsSuccessfully().allTasksSuccessful()

      // then
      assertThat(resolve("a/build/atlas/d2/chart.d2")).exists()
    }

  @Test
  fun `Bare graphviz() with no config registers Graphviz generation`() =
    runScenario(GroovyGraphVizBasic) {
      // when
      assertThatTask("writeGraphvizChart").buildsSuccessfully().allTasksSuccessful()

      // then
      assertThat(resolve("a/build/atlas/graphviz/chart.dot")).exists()
    }

  @Test
  fun `Bare mermaid() with no config registers Mermaid generation`() =
    runScenario(GroovyMermaidBasic) {
      // when
      assertThatTask("writeMermaidChart").buildsSuccessfully().allTasksSuccessful()

      // then
      assertThat(resolve("a/atlas/mermaid/chart.mmd")).exists()
    }
}
