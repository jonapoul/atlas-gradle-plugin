package atlas.test

import blueprint.test.FileTree
import blueprint.test.Scenario as RunningScenario
import blueprint.test.ScenarioTest as BlueprintScenarioTest
import java.io.File
import org.gradle.testkit.runner.GradleRunner

@Suppress("AbstractClassCanBeConcreteClass")
internal abstract class ScenarioTest : BlueprintScenarioTest() {
  override val gradleVersion = GRADLE_VERSION

  private var current: FileTree = fileTree {}

  override val fileTree: FileTree
    get() = current

  protected operator fun Scenario.invoke(
    runner: GradleRunner = defaultRunner(),
    test: RunningScenario.() -> Unit,
  ) = runScenario(this, runner, test)

  protected fun runScenario(
    scenario: Scenario,
    runner: GradleRunner = defaultRunner(),
    test: RunningScenario.() -> Unit,
  ) {
    current = scenario.toFileTree()
    super.runScenario(runner, test)
  }

  private fun Scenario.toFileTree(): FileTree =
    FileTree.Builder(relativeRootPath = "")
      .apply {
        settingsFileName(settingsFile())
        rootBuildFile?.let { buildFileName(it) }
        "gradle.properties"(gradleProperties())

        subprojectBuildFiles.forEach { (path, contents) ->
          val directory = path.toDirectoryPath()
          directory { buildFileName(contents) }
        }
      }
      .build()

  private fun Scenario.settingsFile() = buildString {
    // The `atlas` extension accessor shadows the `atlas` package inside a settings script, so
    // scenarios can't fully qualify Atlas types there - everything they might name gets imported.
    // Framework packages are only pulled in when the scenario uses them, because D2 and Graphviz
    // share several type names (FileFormat, LayoutEngine, Shape, ArrowType).
    val imports =
      frameworks.flatMap { f ->
        listOf("atlas.$f.*", "atlas.$f.tasks.*")
      } + "atlas.core.*"
    imports.forEach { appendLine("import $it") }
    appendLine()

    appendLine(PLUGIN_MANAGEMENT_KTS)
    appendLine()

    if (isGroovy) {
      appendLine("plugins { id '$pluginId' }")
    } else {
      appendLine("plugins { id(\"$pluginId\") }")
    }
    appendLine()

    // This has to come after `plugins { }`, which only tolerates pluginManagement before it
    appendLine(DEPENDENCY_RESOLUTION_MANAGEMENT_KTS)
    appendLine()

    subprojectBuildFiles.keys.forEach { path ->
      if (isGroovy) appendLine("include(':$path')") else appendLine("include(\":$path\")")
    }
    appendLine()

    appendLine(atlasBlock())
  }

  private fun Scenario.atlasBlock(): String {
    val body =
      frameworks.map { framework -> "  $framework()" } +
        atlasConfig.trimIndent().lines().filter(String::isNotBlank).map { "  $it" }
    return if (body.isEmpty()) "" else body.joinToString("\n", "atlas {\n", "\n}\n")
  }

  private fun Scenario.gradleProperties() = buildString {
    appendLine("android.useAndroidX=true")
    // Atlas is built to work under isolated projects, so every scenario runs with it enabled by
    // default
    appendLine("org.gradle.unsafe.isolated-projects=true")
    appendLine(gradlePropertiesFile)
  }

  private fun String.toDirectoryPath() =
    split(":").filter(String::isNotEmpty).joinToString(File.separator)

  private val Scenario.buildFileName
    get() = if (isGroovy) "build.gradle" else "build.gradle.kts"

  private val Scenario.settingsFileName
    get() = if (isGroovy) "settings.gradle" else "settings.gradle.kts"
}

// Blueprint's DEFAULT_REPOSITORIES_KTS bundles these two together, but the settings plugin has to
// be applied between them, so we declare them separately here.
private val PLUGIN_MANAGEMENT_KTS =
  """
  pluginManagement {
    repositories {
      mavenCentral()
      google()
      gradlePluginPortal()
    }
  }
  """
    .trimIndent()

private val DEPENDENCY_RESOLUTION_MANAGEMENT_KTS =
  """
  dependencyResolutionManagement {
    repositories {
      google()
      mavenCentral()
    }
  }
  """
    .trimIndent()
