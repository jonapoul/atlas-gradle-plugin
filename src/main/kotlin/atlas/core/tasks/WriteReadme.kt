package atlas.core.tasks

import atlas.core.internal.ATLAS_TASK_GROUP
import atlas.core.internal.AtlasArtifact
import atlas.core.internal.ChartFiles
import atlas.core.internal.logIfConfigured
import atlas.core.internal.singleFile
import java.io.File
import kotlin.text.RegexOption.DOT_MATCHES_ALL
import org.gradle.api.DefaultTask
import org.gradle.api.Project
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.ListProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.CacheableTask
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputFile
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.Nested
import org.gradle.api.tasks.Optional
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.PathSensitive
import org.gradle.api.tasks.TaskAction
import org.gradle.api.tasks.TaskProvider

/**
 * Creates or updates a README.md file to inject the generated chart/legend files. If the readme
 * contains a block like below:
 * ```markdown
 * Something above
 *
 * <!--region chart-->
 * Anything in between
 * <!--endregion-->
 *
 * Something below
 * ```
 *
 * then the text in between will be replaced with the generated images. Anything outside will be
 * left as-is.
 *
 * If generating a new README, the output will look like below:
 * ```markdown
 * # :path:to:my:project
 *
 * <!--region chart-->
 * ![](chart-d2.png)
 * ![](../atlas/legend-d2.png)
 * <!--endregion-->
 * ```
 *
 * One block is injected per configured framework, in a fixed order, so a project generating both D2
 * and Mermaid charts gets both.
 *
 * Remember, this readme task will also be run when calling `gradle atlasGenerate`.
 */
@CacheableTask
public abstract class WriteReadme : DefaultTask(), AtlasGenerationTask, TaskWithOutputFile {
  @get:Nested public abstract val diagrams: ListProperty<Diagram>
  @get:Internal public abstract val readmeFile: RegularFileProperty
  @get:Input public abstract val projectPath: Property<String>
  @get:OutputFile abstract override val outputFile: RegularFileProperty

  init {
    group = ATLAS_TASK_GROUP
    description = "Creates/updates the project README file to insert the project's chart"
  }

  /** A single framework's contribution to the readme. */
  public interface Diagram {
    @get:[PathSensitive(NONE) InputFile]
    public val chartFile: RegularFileProperty
    @get:[PathSensitive(NONE) InputFile Optional]
    public val legendFile: RegularFileProperty
  }

  @TaskAction
  public fun execute() {
    val readmeFile = readmeFile.asFile.get()

    val newContents =
      if (readmeFile.exists()) {
        injectInto(readmeFile)
      } else {
        newReadme()
      }

    readmeFile.writeText(newContents)
    logIfConfigured(readmeFile)
  }

  private fun injectInto(file: File): String {
    val contents = file.readText()
    val result =
      REGION_REGEX.find(contents)
        ?: error(
          "No injectable region found in $file. Requires a block matching regex: $REGION_REGEX"
        )

    val (startRegion, _, endRegion) = result.destructured
    return buildString {
      appendLine(startRegion)
      appendContents()
      append(endRegion)
    }
  }

  private fun newReadme(): String {
    val expectedTitle = projectPath.get().removePrefix(prefix = ":")
    return buildString {
      appendLine("# $expectedTitle")
      appendLine()
      appendLine(REGION_START)
      appendContents()
      append(REGION_END)
    }
  }

  private fun StringBuilder.appendContents() {
    val blocks =
      diagrams.get().flatMap { diagram ->
        val chart = diagramContents(tag = "chart", file = diagram.chartFile.get().asFile)
        val legend =
          diagram.legendFile.orNull?.asFile?.let { diagramContents(tag = "legend", file = it) }
        listOfNotNull(chart, legend).filter { it.isNotBlank() }
      }

    blocks.forEachIndexed { index, block ->
      if (index > 0) appendLine()
      appendLine(block)
    }
  }

  private fun diagramContents(tag: String, file: File) =
    when (file.extension.lowercase()) {
      "md",
      "txt" -> {
        buildString {
          file.readLines().onEach { appendLine(it) }
        }
      }

      "mmd" -> {
        buildString {
          appendLine("```mermaid")
          file.readLines().forEach { appendLine(it) }
          appendLine("```")
        }
      }

      else -> {
        val readmeFile = outputFile.get().asFile
        val relativePath = file.relativeTo(readmeFile.parentFile)
        "![$tag]($relativePath)"
      }
    }.trim()

  internal companion object {
    private const val REGION_START = "<!--region chart-->"
    private const val REGION_END = "<!--endregion-->"
    private val REGION_REGEX = "(.*$REGION_START)(.*?)($REGION_END.*)".toRegex(DOT_MATCHES_ALL)

    internal const val NAME = "writeReadme"

    internal fun register(
      target: Project,
      charts: List<ChartFiles>,
    ): TaskProvider<WriteReadme> =
      with(target) {
        tasks.register(NAME, WriteReadme::class.java) { task ->
          task.projectPath.convention(target.path)
          val readme = layout.projectDirectory.file("README.md")
          task.readmeFile.convention(readme)
          task.outputFile.convention(readme)

          charts.forEach { chart ->
            val diagram = objects.newInstance(Diagram::class.java)
            diagram.chartFile.set(chart.chart)
            chart.legend?.let { legend ->
              diagram.legendFile.fileProvider(
                legend.singleFile(AtlasArtifact.legend(chart.framework))
              )
            }
            task.diagrams.add(diagram)
          }
        }
      }
  }
}
