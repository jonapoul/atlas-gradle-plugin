package atlas.core

import atlas.test.ScenarioTest
import atlas.test.scenarios.MermaidBasic
import atlas.test.scenarios.NoFrameworksConfigured
import atlas.test.scenarios.PropertiesForUnusedFrameworks
import atlas.test.scenarios.UnsupportedLinkStyle
import blueprint.test.assertThatTask
import blueprint.test.buildsSuccessfully
import blueprint.test.outputContains
import blueprint.test.outputDoesNotContain
import blueprint.test.withArgument
import kotlin.test.Test

internal class FrameworkConfigTest : ScenarioTest() {
  @Test
  fun `Warn about style properties which no configured framework reads`() =
    PropertiesForUnusedFrameworks {
      // when
      assertThatTask("help")
        .buildsSuccessfully()

        // then the D2-only and Graphviz-only properties are called out, grouped by framework, but
        // stroke is understood by Mermaid so it isn't mentioned
        .outputContains(
          "Warning: project type 'Kotlin JVM' sets render3D and shadow, which only D2 uses. " +
            "Configure the d2 { } block to use them, or remove the config."
        )
        .outputContains(
          "Warning: project type 'Kotlin JVM' sets peripheries, which only Graphviz uses. " +
            "Configure the graphviz { } block to use it, or remove the config."
        )
        .outputDoesNotContain("sets stroke")
    }

  @Test
  fun `Don't warn about properties the configured framework reads`() = MermaidBasic {
    // when
    assertThatTask("help").buildsSuccessfully().outputDoesNotContain("Warning")
  }

  @Test
  fun `Warn when a link style isn't supported by a configured framework`() = UnsupportedLinkStyle {
    // when, then
    assertThatTask("help")
      .buildsSuccessfully()
      .outputContains(
        "Warning: link type 'api' uses the dotted style, which Mermaid can't draw - " +
          "Atlas will fall back to the closest style it has."
      )
  }

  @Test
  fun `Warn when no frameworks are configured`() = NoFrameworksConfigured {
    // when, then
    assertThatTask("help")
      .buildsSuccessfully()
      .outputContains(
        "Warning: no Atlas diagram frameworks are configured, so no charts will be generated."
      )
  }

  @Test
  fun `Only register tasks for configured frameworks`() = MermaidBasic {
    // when
    assertThatTask("tasks")
      .withArgument("--all")
      .buildsSuccessfully()

      // then
      .outputContains("writeMermaidChart")
      .outputDoesNotContain("writeGraphvizChart")
      .outputDoesNotContain("writeD2Chart")
  }
}
