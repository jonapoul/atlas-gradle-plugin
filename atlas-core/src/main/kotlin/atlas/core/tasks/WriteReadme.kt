package atlas.core.tasks

import atlas.core.InternalAtlasApi
import atlas.core.internal.ATLAS_TASK_GROUP
import atlas.core.internal.logIfConfigured
import org.gradle.api.DefaultTask
import org.gradle.api.Project
import org.gradle.api.file.RegularFile
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.Property
import org.gradle.api.provider.Provider
import org.gradle.api.tasks.CacheableTask
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputFile
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.Optional
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.PathSensitive
import org.gradle.api.tasks.PathSensitivity.NONE
import org.gradle.api.tasks.TaskAction
import org.gradle.api.tasks.TaskProvider
import org.gradle.internal.extensions.stdlib.capitalized
import java.io.File
import kotlin.text.RegexOption.DOT_MATCHES_ALL

/**
 * Creates or updates a README.md file to inject the generated chart/legend files. If the readme contains a block like
 * below:
 *
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
 * then the text in between will be replaced with the generated images. Anything outside will be left as-is.
 *
 * If generating a new README, the output will look like below:
 *
 * ```markdown
 * # :path:to:my:project
 *
 * <!--region chart-->
 * ![](atlas/chart.png)
 * ![](../atlas/legend.png)
 * <!--endregion-->
 * ```
 *
 * Remember, this readme task will also be run when calling `gradle atlasGenerate`.
 */
@CacheableTask
public abstract class WriteReadme : DefaultTask(), AtlasGenerationTask, TaskWithOutputFile {
  @get:[PathSensitive(NONE) InputFile] public abstract val chartFile: RegularFileProperty
  @get:[PathSensitive(NONE) InputFile Optional] public abstract val legendFile: RegularFileProperty
  @get:Internal public abstract val readmeFile: RegularFileProperty
  @get:Input public abstract val projectPath: Property<String>
  @get:OutputFile abstract override val outputFile: RegularFileProperty

  init {
    group = ATLAS_TASK_GROUP
    description = "Creates/updates the project README file to insert the project's chart"
  }

  @TaskAction
  public fun execute() {
    val readmeFile = readmeFile.asFile.get()

    val newContents = if (readmeFile.exists()) {
      injectInto(readmeFile)
    } else {
      newReadme()
    }

    readmeFile.writeText(newContents)
    logIfConfigured(readmeFile)
  }

  private fun injectInto(file: File): String {
    val contents = file.readText()
    val result = REGION_REGEX.find(contents)
      ?: error("No injectable region found in $file. Requires a block matching regex: $REGION_REGEX")

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
    val chart = diagramContents(tag = "chart", chartFile.get().asFile)
    if (!chart.isBlank()) appendLine(chart)

    legendFile.orNull?.asFile?.let {
      val legend = diagramContents(tag = "legend", it)
      if (!legend.isBlank()) {
        if (!chart.isBlank()) appendLine()
        appendLine(legend)
      }
    }
  }

  @Suppress("ktlint:standard:when-entry-bracing")
  private fun diagramContents(tag: String, file: File) = when (file.extension.lowercase()) {
    "md", "txt" -> buildString {
      file
        .readLines()
        .onEach { appendLine(it) }
    }

    "mmd" -> buildString {
      appendLine("```mermaid")
      file
        .readLines()
        .forEach { appendLine(it) }
      appendLine("```")
    }

    else -> {
      val readmeFile = outputFile.get().asFile
      val relativePath = file.relativeTo(readmeFile.parentFile)
      "![$tag]($relativePath)"
    }
  }.trim()

  @InternalAtlasApi
  public companion object {
    private const val REGION_START = "<!--region chart-->"
    private const val REGION_END = "<!--endregion-->"
    private val REGION_REGEX = "(.*$REGION_START)(.*?)($REGION_END.*)".toRegex(DOT_MATCHES_ALL)

    @InternalAtlasApi
    public fun register(
      target: Project,
      flavor: String,
      chartFile: Provider<RegularFile>,
      legendTask: Provider<out TaskWithOutputFile>?,
    ): TaskProvider<WriteReadme> = with(target) {
      return tasks.register("write${flavor.capitalized()}Readme", WriteReadme::class.java) { task ->
        task.projectPath.convention(target.path)
        if (legendTask == null) {
          task.legendFile.convention(null)
        } else {
          task.legendFile.convention(legendTask.flatMap { it.outputFile })
        }
        task.chartFile.convention(chartFile)
        val readme = layout.projectDirectory.file("README.md")
        task.readmeFile.convention(readme)
        task.outputFile.convention(readme)
      }
    }
  }
}
