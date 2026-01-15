package atlas

import assertk.assertThat
import atlas.test.D2Scenario
import atlas.test.GraphvizScenario
import atlas.test.MermaidScenario
import atlas.test.Scenario
import atlas.test.ScenarioTest
import atlas.test.allTasksSuccessful
import atlas.test.runTask
import atlas.test.scenarios.D2Basic
import atlas.test.scenarios.GraphvizBasic
import atlas.test.scenarios.MermaidBasic
import kotlin.test.Test

internal class ProjectIsolationTest : ScenarioTest() {
  @Test fun d2() = runProjectIsolationTest(D2ProjectIsolation)
  @Test fun graphviz() = runProjectIsolationTest(GraphvizProjectIsolation)
  @Test fun mermaid() = runProjectIsolationTest(MermaidProjectIsolation)

  private fun runProjectIsolationTest(scenario: Scenario) = runScenario(scenario) {
    val result = runTask("atlasGenerate").build()
    assertThat(result).allTasksSuccessful()
  }
}

private const val PROPERTIES_FILE = "org.gradle.unsafe.isolated-projects=true"

private object D2ProjectIsolation : D2Scenario by D2Basic {
  override val gradlePropertiesFile = PROPERTIES_FILE
}

private object GraphvizProjectIsolation : GraphvizScenario by GraphvizBasic {
  override val gradlePropertiesFile = PROPERTIES_FILE
}

private object MermaidProjectIsolation : MermaidScenario by MermaidBasic {
  override val gradlePropertiesFile = PROPERTIES_FILE
}
