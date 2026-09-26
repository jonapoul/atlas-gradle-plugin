package atlas.test.scenarios

import atlas.core.Framework
import atlas.test.MermaidScenario
import atlas.test.Scenario
import atlas.test.javaBuildScript
import atlas.test.kotlinJvmBuildScript

/** A project type styled with properties which only D2 and Graphviz know what to do with. */
internal object PropertiesForUnusedFrameworks : MermaidScenario {
  override val rootBuildFile = SIMPLE_ROOT_BUILD_FILE

  override val atlasConfig =
    """
    projectTypes {
      kotlinJvm {
        stroke = "black"
        render3D = true
        shadow = true
        peripheries = 2
      }
    }
    """
      .trimIndent()

  override val subprojectBuildFiles = SIMPLE_SUBPROJECTS
}

/** Mermaid has no dotted links, so it'll fall back to dashed ones. */
internal object UnsupportedLinkStyle : MermaidScenario {
  override val rootBuildFile = SIMPLE_ROOT_BUILD_FILE

  override val atlasConfig =
    """
    linkTypes {
      api(style = LinkStyle.Dotted)
    }
    """
      .trimIndent()

  override val subprojectBuildFiles = SIMPLE_SUBPROJECTS
}

/** The plugin is applied, but no framework block is configured. */
internal object NoFrameworksConfigured : Scenario {
  override val frameworks = emptySet<Framework>()

  override val rootBuildFile = SIMPLE_ROOT_BUILD_FILE

  override val atlasConfig =
    """
    projectTypes {
      kotlinJvm()
    }
    """
      .trimIndent()

  override val subprojectBuildFiles = SIMPLE_SUBPROJECTS
}

/** No framework block, but a framework property is set directly. */
internal object FrameworkPropertySetDirectly : Scenario {
  override val frameworks = emptySet<Framework>()

  override val rootBuildFile = SIMPLE_ROOT_BUILD_FILE

  override val atlasConfig = """graphviz.pathToDotCommand = "dot""""

  override val subprojectBuildFiles = SIMPLE_SUBPROJECTS
}

/** No framework block, but a framework's Gradle property is set. */
internal object FrameworkGradlePropertySet : Scenario {
  override val frameworks = emptySet<Framework>()

  override val rootBuildFile = SIMPLE_ROOT_BUILD_FILE

  override val gradlePropertiesFile = "atlas.graphviz.layoutEngine=neato"

  override val subprojectBuildFiles = SIMPLE_SUBPROJECTS
}

private val SIMPLE_ROOT_BUILD_FILE =
  """
  plugins {
    kotlin("jvm") apply false
  }
  """
    .trimIndent()

private val SIMPLE_SUBPROJECTS: Map<String, String>
  get() =
    mapOf(
      "a" to
        """
      ${MermaidBasic.kotlinJvmBuildScript}
      dependencies {
        api(project(":b"))
      }
    """
          .trimIndent(),
      "b" to MermaidBasic.javaBuildScript,
    )
