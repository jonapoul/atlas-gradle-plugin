package atlas.core.tasks

import atlas.core.LinkType
import atlas.core.internal.ATLAS_TASK_GROUP
import atlas.core.internal.AtlasConfig
import atlas.core.internal.ProjectLink
import atlas.core.internal.fileInBuildDirectory
import atlas.core.internal.readProjectLinks
import atlas.core.internal.singleFile
import atlas.core.internal.writeProjectLinks
import org.gradle.api.DefaultTask
import org.gradle.api.Project
import org.gradle.api.file.FileCollection
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.Property
import org.gradle.api.provider.SetProperty
import org.gradle.api.tasks.CacheableTask
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputFile
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.PathSensitive
import org.gradle.api.tasks.TaskAction
import org.gradle.api.tasks.TaskProvider

/**
 * Gathers a snapshot of the whole-project [collatedLinks] data to only find those projects and
 * links relevant to [thisPath]. If [alsoTraverseUpwards] is enabled, upstream projects will be
 * included too.
 */
@CacheableTask
public abstract class WriteProjectTree : DefaultTask(), TaskWithOutputFile {
  @get:[PathSensitive(NONE) InputFile]
  public abstract val collatedLinks: RegularFileProperty
  @get:Input public abstract val alsoTraverseUpwards: Property<Boolean>
  @get:Input public abstract val thisPath: Property<String>
  @get:Input public abstract val linkTypes: SetProperty<LinkType>
  @get:OutputFile abstract override val outputFile: RegularFileProperty

  init {
    group = ATLAS_TASK_GROUP
    description = "Generates the tree of project links"
  }

  @TaskAction
  public fun execute() {
    val thisPath = thisPath.get()
    val allLinks = readProjectLinks(collatedLinks.get().asFile)
    val links = filterLinks(allLinks)

    val tree = mutableSetOf<ProjectLink>()
    if (alsoTraverseUpwards.get()) {
      calculate(thisPath, Up, links, tree)
    }
    calculate(thisPath, Down, links, tree)

    val outputFile = outputFile.get().asFile
    writeProjectLinks(tree, outputFile)

    logger.info(
      "WriteProjectTree: written ${tree.size} links from ${links.size} across the project",
    )
    tree.forEach { link ->
      logger.info("WriteProjectTree:     link = $link")
    }
  }

  private fun filterLinks(links: Set<ProjectLink>): Set<ProjectLink> {
    val types = linkTypes.get()
    val filtered = hashSetOf<ProjectLink>()

    links
      .groupBy { it.fromPath to it.toPath }
      .values
      .forEach { groupedLinks ->
        val linksWithTypes = groupedLinks.filter { it.type != null }
        filtered +=
          when (linksWithTypes.size) {
            0 -> groupedLinks.first()
            1 -> linksWithTypes[0]
            else -> highestPriorityLink(linksWithTypes, types)
          }
      }

    return filtered
  }

  private fun highestPriorityLink(links: List<ProjectLink>, types: Set<LinkType>): ProjectLink {
    for (type in types) {
      for (link in links) {
        if (type == link.type) {
          return link
        }
      }
    }
    return links.first()
  }

  private enum class Direction {
    Up,
    Down,
  }

  private fun calculate(
    targetPath: String,
    direction: Direction,
    links: Set<ProjectLink>,
    tree: MutableSet<ProjectLink>,
  ) {
    when (direction) {
      Up -> {
        val relevantLinks = links.filter { it.toPath == targetPath }
        tree += relevantLinks
        for (link in relevantLinks) {
          calculate(link.fromPath, direction, links, tree)
        }
      }

      Down -> {
        val relevantLinks = links.filter { it.fromPath == targetPath }
        tree += relevantLinks
        for (link in relevantLinks) {
          calculate(link.toPath, direction, links, tree)
        }
      }
    }
  }

  internal companion object {
    private const val NAME = "writeProjectTree"

    internal fun get(target: Project): TaskProvider<WriteProjectTree> =
      target.tasks.named(NAME, WriteProjectTree::class.java)

    internal fun register(
      target: Project,
      config: AtlasConfig,
      collatedLinks: FileCollection,
    ): TaskProvider<WriteProjectTree> =
      with(target) {
        val writeProjectTree =
          tasks.register(NAME, WriteProjectTree::class.java) { task ->
            task.thisPath.convention(target.path)
            task.outputFile.convention(fileInBuildDirectory("project-tree.json"))
            task.alsoTraverseUpwards.convention(config.alsoTraverseUpwards)
          }

        writeProjectTree.configure { task ->
          task.linkTypes.convention(config.linkTypes)
          task.collatedLinks.fileProvider(collatedLinks.singleFile(CollatedLinks))
          task.dependsOn(collatedLinks)
        }

        writeProjectTree
      }
  }
}
