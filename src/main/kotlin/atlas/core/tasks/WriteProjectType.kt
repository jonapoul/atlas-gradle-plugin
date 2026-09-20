package atlas.core.tasks

import atlas.core.ProjectType
import atlas.core.internal.ATLAS_TASK_GROUP
import atlas.core.internal.AtlasConfig
import atlas.core.internal.ProjectTypeMatcher
import atlas.core.internal.TypedProject
import atlas.core.internal.fileInBuildDirectory
import atlas.core.internal.writeProjectType
import org.gradle.api.DefaultTask
import org.gradle.api.Project
import org.gradle.api.UnknownTaskException
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.CacheableTask
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Optional
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.TaskAction
import org.gradle.api.tasks.TaskProvider

/**
 * Dumps the [atlas.core.ProjectTypeSpec] of this project to a file. This will then by aggregated in
 * [CollateProjectTypes].
 */
@CacheableTask
public abstract class WriteProjectType : DefaultTask(), TaskWithOutputFile {
  @get:Input public abstract val projectPath: Property<String>
  @get:[Input Optional]
  public abstract val projectType: Property<ProjectType>
  @get:OutputFile abstract override val outputFile: RegularFileProperty

  init {
    group = ATLAS_TASK_GROUP
    description = "Caches the project's path and type, for use in other tasks"
  }

  @TaskAction
  public fun execute() {
    val projectPath = projectPath.get()
    val projectType = projectType.orNull
    val outputFile = outputFile.get().asFile

    writeProjectType(
      project = TypedProject(projectPath = projectPath, type = projectType),
      outputFile = outputFile,
    )
  }

  internal companion object {
    internal const val NAME = "writeProjectType"

    internal fun get(target: Project): TaskProvider<WriteProjectType>? =
      try {
        target.tasks.named(NAME, WriteProjectType::class.java)
      } catch (_: UnknownTaskException) {
        null
      }

    internal fun register(target: Project, config: AtlasConfig): TaskProvider<WriteProjectType> =
      with(target) {
        val writeProject =
          tasks.register(NAME, WriteProjectType::class.java) { task ->
            task.projectPath.convention(target.path)
            task.outputFile.convention(fileInBuildDirectory("project-type.json"))
          }

        // hasPluginId can only be answered once this project's own plugins have been applied, which
        // is still its own project's afterEvaluate and so stays within isolated projects' rules.
        @Suppress("AvoidAfterEvaluate")
        afterEvaluate {
          val matching = config.projectTypes.firstOrNull { type -> type.matches(target) }?.type
          writeProject.configure { task ->
            task.projectType.convention(matching)
          }
        }

        writeProject
      }

    private fun ProjectTypeMatcher.matches(project: Project): Boolean =
      with(project) {
        when {
          pathContains != null -> path.contains(pathContains)
          pathMatches != null -> path.matches(pathMatches.toRegex(regexOptions))
          hasPluginId != null -> pluginManager.hasPlugin(hasPluginId)
          else -> false
        }
      }
  }
}
