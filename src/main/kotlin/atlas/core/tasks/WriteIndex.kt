package atlas.core.tasks

import atlas.core.Framework
import atlas.core.internal.ATLAS_TASK_GROUP
import atlas.core.internal.AtlasJson
import atlas.core.internal.fileInBuildDirectory
import java.io.File
import kotlinx.serialization.Serializable
import kotlinx.serialization.builtins.ListSerializer
import org.gradle.api.DefaultTask
import org.gradle.api.Project
import org.gradle.api.artifacts.ArtifactCollection
import org.gradle.api.artifacts.component.ComponentArtifactIdentifier
import org.gradle.api.artifacts.component.ProjectComponentIdentifier
import org.gradle.api.file.ConfigurableFileCollection
import org.gradle.api.file.RegularFile
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.ListProperty
import org.gradle.api.provider.Provider
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputFile
import org.gradle.api.tasks.InputFiles
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.PathSensitive
import org.gradle.api.tasks.TaskAction
import org.gradle.api.tasks.TaskProvider
import org.gradle.work.DisableCachingByDefault

/**
 * Registered on the root project to write a single HTML page linking to every project's chart, with
 * a filterable and sortable table and a preview pane. Mermaid charts are rendered in the browser,
 * which loads Mermaid from a CDN the first time one is previewed.
 *
 * [chartFrameworks], [chartPaths] and [chartFiles] are parallel lists: the nth chart was drawn by
 * the nth framework for the nth project path.
 *
 * Switched off with `atlas.generateIndex`.
 */
@DisableCachingByDefault(because = "Records each chart's size and modification time")
public abstract class WriteIndex : DefaultTask(), TaskWithOutputFile {
  @get:Input public abstract val chartFrameworks: ListProperty<String>
  @get:Input public abstract val chartPaths: ListProperty<String>
  @get:[PathSensitive(ABSOLUTE) InputFiles]
  public abstract val chartFiles: ConfigurableFileCollection
  @get:[PathSensitive(NONE) InputFile]
  public abstract val projectTypes: RegularFileProperty
  @get:[PathSensitive(NONE) InputFile]
  public abstract val projectLinks: RegularFileProperty
  @get:OutputFile abstract override val outputFile: RegularFileProperty

  init {
    group = ATLAS_TASK_GROUP
    description = "Writes an HTML page linking to every project's chart"
    doNotTrackState("Always prints the page's URL")
  }

  @TaskAction
  public fun execute() {
    val output = outputFile.get().asFile
    val frameworks = chartFrameworks.get()
    val paths = chartPaths.get()
    val files = chartFiles.files.toList()
    check(frameworks.size == paths.size && paths.size == files.size) {
      "Mismatched charts: ${frameworks.size} frameworks, ${paths.size} paths, ${files.size} files"
    }

    val entries =
      List(files.size) { i -> entry(frameworks[i], paths[i], files[i], output.parentFile) }
        .sortedWith(compareBy({ it.path }, { it.framework }))

    val template =
      checkNotNull(WriteIndex::class.java.getResource(TEMPLATE)) { "Missing $TEMPLATE" }.readText()
    val values =
      mapOf(
        "GENERATED" to System.currentTimeMillis().toString(),
        "CHARTS" to AtlasJson.encodeToString(ListSerializer(Entry.serializer()), entries).escaped(),
        "TYPES" to projectTypes.get().asFile.readText().escaped(),
        "LINKS" to projectLinks.get().asFile.readText().escaped(),
      )
    output.writeText(fillTemplate(template, values))
    logger.lifecycle("Atlas index: ${output.toURI()}")
  }

  private fun entry(framework: String, path: String, chart: File, outputDir: File) =
    Entry(
      path = path,
      framework = framework,
      url = chart.relativeToOrNull(outputDir)?.invariantSeparatorsPath ?: chart.toURI().toString(),
      size = chart.length(),
      modified = chart.lastModified(),
      // Browsers won't fetch a file:// URL from a file:// page, so Mermaid source goes inline
      source = if (chart.extension == "mmd") chart.readText() else null,
    )

  // Stops a "</script>" inside any of the JSON from closing the page's script early
  private fun String.escaped() = replace("</", "<\\/")

  @Serializable
  private class Entry(
    val path: String,
    val framework: String,
    val url: String,
    val size: Long,
    val modified: Long,
    val source: String? = null,
  )

  internal companion object {
    private const val NAME = "atlasWriteIndex"
    private const val TEMPLATE = "index.html"
    private val PLACEHOLDER = "__([A-Z]+)__".toRegex()

    /**
     * Swaps each `__KEY__` in [template] for its value, in one pass so that nothing inserted is
     * scanned again - a chart's source or a project path may well contain `__TYPES__` itself.
     */
    internal fun fillTemplate(template: String, values: Map<String, String>): String =
      PLACEHOLDER.replace(template) { match ->
        values[match.groupValues[1]] ?: match.value
      }

    internal fun register(
      target: Project,
      frameworks: List<Framework>,
      charts: (Framework) -> ArtifactCollection,
      projectTypes: Provider<RegularFile>,
      projectLinks: Provider<RegularFile>,
    ): TaskProvider<WriteIndex> =
      with(target) {
        // Resolved up front, since each framework's configurations may only be declared once
        val resolved = frameworks.associateWith(charts)

        tasks.register(NAME, WriteIndex::class.java) { task ->
          resolved.forEach { (framework, artifacts) ->
            val name = framework.displayName
            val results = artifacts.resolvedArtifacts
            task.chartFrameworks.addAll(results.map { r -> r.map { name } })
            task.chartPaths.addAll(results.map { r -> r.map { it.id.projectPath() } })
            task.chartFiles.from(artifacts.artifactFiles)
          }
          task.projectTypes.convention(projectTypes)
          task.projectLinks.convention(projectLinks)
          task.outputFile.convention(fileInBuildDirectory("index.html"))
        }
      }

    private fun ComponentArtifactIdentifier.projectPath() =
      (componentIdentifier as ProjectComponentIdentifier).projectPath
  }
}
