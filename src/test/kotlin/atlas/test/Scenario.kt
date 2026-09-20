package atlas.test

import atlas.core.Framework

internal interface Scenario {
  // Null means the root project has no build file at all, like any other group directory
  val rootBuildFile: String?
    get() = null

  // The body of the `atlas { }` block, which lives in `settings.gradle.kts`.
  val atlasConfig: String
    get() = ""

  val subprojectBuildFiles: Map<String, String>
    get() = emptyMap()

  val gradlePropertiesFile: String
    get() = ""

  val isGroovy: Boolean
    get() = false

  /**
   * The frameworks this scenario generates diagrams for. [ScenarioTest] switches them on in the
   * generated root build file, so scenarios only need to declare the config they're actually
   * testing.
   */
  val frameworks: Set<Framework>

  val pluginId
    get() = "dev.jonpoulton.atlas"
}

internal interface D2Scenario : Scenario {
  override val frameworks: Set<Framework>
    get() = setOf(D2)
}

internal interface GraphvizScenario : Scenario {
  override val frameworks: Set<Framework>
    get() = setOf(Graphviz)
}

internal interface MermaidScenario : Scenario {
  override val frameworks: Set<Framework>
    get() = setOf(Mermaid)
}

internal fun Scenario.withIntermediatesInProjectDir(): Scenario {
  val base = this
  return object : Scenario by base {
    override val atlasConfig =
      (frameworks - Framework.Mermaid).joinToString(
        separator = "\n",
        prefix = base.atlasConfig + "\n",
      ) { framework ->
        "$framework { intermediateFilesInBuildDir = false }"
      }
  }
}

/** The single framework a scenario uses, for tests which assert on generated file paths. */
internal val Scenario.framework: Framework
  get() = frameworks.single()

// Subprojects no longer apply the plugin themselves - the settings plugin wires every project.
@Suppress("UnusedReceiverParameter")
internal val Scenario.javaBuildScript
  get() =
    """
    plugins {
      id("java")
    }
    """
      .trimIndent()

@Suppress("UnusedReceiverParameter")
internal val Scenario.kotlinJvmBuildScript
  get() =
    """
    plugins {
      kotlin("jvm")
    }
    """
      .trimIndent()

internal val Scenario.androidBuildScript
  get() =
    """
  plugins {
    id("com.android.library")
  }

  android {
    namespace = "dev.jonpoulton.dummy.$framework"
    compileSdk = 36
  }
  """
      .trimIndent()
