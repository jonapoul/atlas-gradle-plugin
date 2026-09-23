package atlas.test.scenarios

import atlas.test.D2Scenario
import atlas.test.GraphvizScenario
import atlas.test.MermaidScenario
import atlas.test.Scenario

/**
 * No `atlasConfig` at all, so the only framework config is the bare `d2()`/`graphviz()`/`mermaid()`
 * call.
 */
internal interface GroovyBasic : Scenario {
  override val isGroovy: Boolean
    get() = true

  override val rootBuildFile: String
    get() =
      """
      plugins {
        id 'org.jetbrains.kotlin.jvm'
      }
      """
        .trimIndent()

  override val subprojectBuildFiles: Map<String, String>
    get() =
      mapOf(
        "a" to
          """
          plugins {
            id 'org.jetbrains.kotlin.jvm'
          }

          dependencies {
            api(project(':b'))
            implementation(project(':c'))
          }
          """
            .trimIndent(),
        "b" to
          """
          plugins {
            id 'org.jetbrains.kotlin.jvm'
          }
          """
            .trimIndent(),
        "c" to
          """
          plugins {
            id 'java'
          }
          """
            .trimIndent(),
      )
}

internal object GroovyD2Basic : GroovyBasic, D2Scenario

internal object GroovyGraphVizBasic : GroovyBasic, GraphvizScenario

internal object GroovyMermaidBasic : GroovyBasic, MermaidScenario
