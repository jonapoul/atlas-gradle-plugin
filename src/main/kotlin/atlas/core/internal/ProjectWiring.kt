package atlas.core.internal

import atlas.core.tasks.AtlasGenerationTask
import atlas.core.tasks.CheckFileDiff
import atlas.core.tasks.CollateProjectLinks
import atlas.core.tasks.CollateProjectTypes
import atlas.core.tasks.WriteProjectLinks
import atlas.core.tasks.WriteProjectTree
import atlas.core.tasks.WriteProjectType
import atlas.core.tasks.WriteReadme
import atlas.d2.internal.d2Releases
import atlas.d2.internal.downloadVersion
import atlas.d2.internal.warnIfVersionIgnored
import org.gradle.api.Project
import org.gradle.api.initialization.ProjectDescriptor
import org.gradle.api.initialization.Settings
import org.gradle.api.initialization.resolve.RepositoriesMode.PREFER_PROJECT
import org.gradle.api.provider.Provider
import org.gradle.api.provider.ProviderFactory
import org.gradle.api.tasks.TaskProvider
import org.gradle.language.base.plugins.LifecycleBasePlugin
import org.gradle.language.base.plugins.LifecycleBasePlugin.VERIFICATION_GROUP

@Suppress("UnstableApiUsage")
internal fun onSettingsEvaluated(
  settings: Settings,
  wiring: AtlasWiring,
  extension: AtlasExtensionImpl,
  warnings: AtlasWarnings,
) {
  extension.enableFrameworksFromGradleProperties(settings.providers)
  val repositories = settings.dependencyResolutionManagement
  val d2DownloadVersion =
    if (D2 in extension.frameworks) extension.d2Spec.downloadVersion(settings.providers) else null

  wiring.config =
    extension.snapshot(
      rootDir = settings.rootDir,
      subprojectPaths = chartedSubprojectPaths(settings.rootProject),
      preferProjectRepositories = repositories.repositoriesMode.get() == PREFER_PROJECT,
      d2DownloadVersion = d2DownloadVersion,
    )

  // Read here rather than on apply, since the settings script sets it after `plugins { }`
  if (d2DownloadVersion != null) repositories.repositories.d2Releases()
  extension.warnAboutConfig(warnings)
  if (D2 in extension.frameworks) extension.d2Spec.warnIfVersionIgnored(warnings)
}

private fun chartedSubprojectPaths(project: ProjectDescriptor): List<String> = buildList {
  project.children.forEach { child ->
    if (child.buildFile.exists()) add(child.path)
    addAll(chartedSubprojectPaths(child))
  }
}

internal fun wireProject(target: Project, wiring: AtlasWiring) {
  val context = AtlasContext(project = target, config = wiring.config, wiring = wiring)

  // Nested projects whose group directories have no build file shouldn't be nodes in the chart. The
  // root is never a node either, but it still has to be wired: it's where every subproject resolves
  // the collated files and the shared legends from, whether or not it has a build file of its own.
  if (!context.isRoot && !target.buildFile.exists()) return

  target.pluginManager.apply(LifecycleBasePlugin::class.java)

  if (context.isRoot) wireRoot(context) else wireChild(context)

  target.configurePrintFilesToConsole(context)
  target.registerAtlasCheckTask()
}

/** The root collates every subproject's contribution, and draws the shared legends. */
private fun wireRoot(context: AtlasContext) =
  with(context.project) {
    val paths = context.config.subprojectPaths

    val collateTypes =
      CollateProjectTypes.register(
        target = this,
        projectTypeFiles = consumeAtlasArtifact(ProjectType, paths, lenient = true),
      )
    val collateLinks =
      CollateProjectLinks.register(
        target = this,
        config = context.config,
        projectLinkFiles = consumeAtlasArtifact(ProjectLinks, paths, lenient = true),
      )

    publishAtlasArtifact(
      artifact = CollatedTypes,
      file = collateTypes.flatMap { it.outputFile },
      builtBy = collateTypes,
    )
    publishAtlasArtifact(
      artifact = CollatedLinks,
      file = collateLinks.flatMap { it.outputFile },
      builtBy = collateLinks,
    )

    context.config.frameworks.forEach { framework -> framework.tasks.registerRootTasks(context) }
  }

/** Every other project describes itself, then draws its own slice of the graph. */
private fun wireChild(context: AtlasContext) =
  with(context.project) {
    val writeType = WriteProjectType.register(this, context.config)
    val writeLinks = WriteProjectLinks.register(this, context.config)

    publishAtlasArtifact(
      artifact = ProjectType,
      file = writeType.flatMap { it.outputFile },
      builtBy = writeType,
    )
    publishAtlasArtifact(
      artifact = ProjectLinks,
      file = writeLinks.flatMap { it.outputFile },
      builtBy = writeLinks,
    )

    WriteProjectTree.register(
      target = this,
      config = context.config,
      collatedLinks = context.fromRoot(CollatedLinks),
    )

    val atlasGenerate = registerAtlasGenerateTask()
    registerGenerationTaskOnSync(atlasGenerate, context.config)

    val charts =
      context.config.frameworks.map { framework -> framework.tasks.registerChildTasks(context) }
    registerReadmeTask(charts)
  }

private fun Project.registerReadmeTask(charts: List<ChartFiles>) {
  val writeReadme = WriteReadme.register(target = this, charts = charts)
  writeReadme.configure { task ->
    charts.forEach { chart ->
      task.dependsOn(chart.chart)
      chart.legend?.let(task::dependsOn)
    }
  }
}

private fun Project.configurePrintFilesToConsole(context: AtlasContext) {
  tasks.withType(AtlasGenerationTask::class.java).configureEach { task ->
    task.printFilesToConsole.convention(context.config.printFilesToConsole)
  }
}

private fun Project.registerAtlasGenerateTask() =
  tasks.register("atlasGenerate") { task ->
    task.group = ATLAS_TASK_GROUP
    task.description = "Aggregates all Atlas generation tasks"
    task.dependsOn(
      tasks.withType(AtlasGenerationTask::class.java).matching { it !is DummyAtlasGenerationTask }
    )
  }

private fun Project.registerAtlasCheckTask() =
  tasks.register("atlasCheck") { task ->
    task.group = VERIFICATION_GROUP
    task.description = "Aggregates all Atlas verification tasks"
    task.dependsOn(tasks.withType(CheckFileDiff::class.java))
  }

private fun Project.registerGenerationTaskOnSync(
  atlasGenerate: TaskProvider<*>,
  config: AtlasConfig,
) {
  if (config.generateOnSync && providers.isIntellijSyncing.getOrElse(false)) {
    tasks.register("prepareKotlinIdeaImport") { task -> task.dependsOn(atlasGenerate) }
  }
}

private val ProviderFactory.isIntellijSyncing: Provider<Boolean>
  get() = systemProperty("idea.sync.active").map(String::toBoolean)
