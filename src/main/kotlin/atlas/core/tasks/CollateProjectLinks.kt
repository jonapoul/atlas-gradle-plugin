package atlas.core.tasks

import atlas.core.internal.ATLAS_TASK_GROUP
import atlas.core.internal.AtlasConfig
import atlas.core.internal.ProjectLink
import atlas.core.internal.fileInBuildDirectory
import atlas.core.internal.readProjectLinks
import atlas.core.internal.writeProjectLinks
import org.gradle.api.DefaultTask
import org.gradle.api.Project
import org.gradle.api.file.ConfigurableFileCollection
import org.gradle.api.file.FileCollection
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.SetProperty
import org.gradle.api.tasks.CacheableTask
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputFiles
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.PathSensitive
import org.gradle.api.tasks.TaskAction
import org.gradle.api.tasks.TaskProvider

/**
 * Registered on the root project to aggregate the results of [WriteProjectLinks] tasks. This will
 * then be referenced from [WriteProjectTree] to draw up the full project picture. This will then be
 * sub-charted for each project in turn.
 */
@CacheableTask
public abstract class CollateProjectLinks : DefaultTask(), TaskWithOutputFile {
  @get:[PathSensitive(NONE) InputFiles]
  public abstract val projectLinkFiles: ConfigurableFileCollection
  @get:Input public abstract val ignoredProjects: SetProperty<Regex>
  @get:OutputFile abstract override val outputFile: RegularFileProperty

  init {
    group = ATLAS_TASK_GROUP
    description = "Collates all links between all projects"
  }

  @TaskAction
  public fun execute() {
    val outputFile = outputFile.get().asFile

    val links = projectLinkFiles.flatMap(::readProjectLinks).filterProjects().toSet()

    for ((from, to) in links) {
      if (to == from) {
        error("Found a link from '$from' to itself - this will probably cause weird build issues!")
      }
    }

    writeProjectLinks(links, outputFile)

    logger.info("CollateProjectLinks: ${links.size} links")
    links.forEach { link ->
      logger.info(
        "CollateProjectLinks:     from=${link.fromPath}, to=${link.toPath}, config=${link.configuration}",
      )
    }
  }

  private fun List<ProjectLink>.filterProjects() = filter { (from, to) ->
    ignoredProjects.get().none { pattern ->
      from.matches(pattern) || to.matches(pattern)
    }
  }

  internal companion object {
    private const val NAME = "collateProjectLinks"

    internal fun register(
      target: Project,
      config: AtlasConfig,
      projectLinkFiles: FileCollection,
    ): TaskProvider<CollateProjectLinks> =
      with(target) {
        tasks.register(NAME, CollateProjectLinks::class.java) { task ->
          task.outputFile.convention(fileInBuildDirectory("project-links.json"))
          task.ignoredProjects.convention(config.ignoredProjects)
          task.projectLinkFiles.from(projectLinkFiles)
        }
      }
  }
}
