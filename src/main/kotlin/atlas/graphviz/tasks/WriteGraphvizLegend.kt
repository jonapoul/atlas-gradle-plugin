package atlas.graphviz.tasks

import atlas.core.LinkType
import atlas.core.ProjectType
import atlas.core.internal.ATLAS_TASK_GROUP
import atlas.core.internal.AtlasContext
import atlas.core.internal.DummyAtlasGenerationTask
import atlas.core.internal.atlasBuildDirectory
import atlas.core.internal.buildIndentedString
import atlas.core.internal.intermediateFile
import atlas.core.internal.logIfConfigured
import atlas.core.internal.qualifier
import atlas.core.tasks.AtlasGenerationTask
import atlas.core.tasks.TaskWithOutputFile
import atlas.graphviz.DotConfig
import atlas.graphviz.GraphvizSpec
import atlas.graphviz.Shape.Plaintext
import atlas.graphviz.internal.appendHeaderGroup
import atlas.graphviz.internal.attrs
import java.io.File
import org.gradle.api.DefaultTask
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.ListProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.CacheableTask
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.TaskAction
import org.gradle.api.tasks.TaskProvider
import org.gradle.internal.extensions.stdlib.capitalized
import org.gradle.work.DisableCachingByDefault

/** Converts a [DotConfig] into a written legend table, generated once in the project root. */
@CacheableTask
public abstract class WriteGraphvizLegend : DefaultTask(), TaskWithOutputFile, AtlasGenerationTask {
  @get:Input public abstract val projectTypes: ListProperty<ProjectType>
  @get:Input public abstract val linkTypes: ListProperty<LinkType>
  @get:Input public abstract val config: Property<DotConfig>
  @get:OutputFile abstract override val outputFile: RegularFileProperty

  init {
    group = ATLAS_TASK_GROUP
    description = "Generates the legend for a project dependency graph"
  }

  @TaskAction
  public open fun execute() {
    val projectTypes = projectTypes.get()
    val linkTypes = linkTypes.get()
    val outputFile = outputFile.get().asFile
    val config = config.get()

    val hasProjectTypes = projectTypes.isNotEmpty()
    val hasLinkTypes = linkTypes.isNotEmpty()

    val dotFileContents = buildIndentedString {
      appendLine("digraph {")
      indent {
        appendHeaderGroup(name = "node", attrs(config.nodeAttributes) + mapOf("shape" to Plaintext))
        appendHeaderGroup(name = "edge", attrs(config.edgeAttributes))
        appendHeaderGroup(name = "graph", attrs(config.graphAttributes))

        val tableAttrs =
          tableAttributes(config).joinToString(separator = " ") { (k, v) -> "$k=\"$v\"" }

        if (hasProjectTypes) {
          appendLine("projects [label=<")
          appendLine("<TABLE $tableAttrs>")
          appendLine("  <TR><TD COLSPAN=\"2\"><B>Project Types</B></TD></TR>")
          indent {
            projectTypes.forEach { type ->
              val text =
                withFontColor(text = "&lt;project-name&gt;", properties = type.properties(Graphviz))
              appendLine("<TR><TD>${type.name}</TD><TD BGCOLOR=\"${type.color}\">$text</TD></TR>")
            }
          }
          appendLine("</TABLE>")
          appendLine(">];")
        }

        if (hasLinkTypes) {
          appendLine("links [label=<")
          appendLine("<TABLE $tableAttrs>")
          appendLine("  <TR><TD COLSPAN=\"2\"><B>Link Types</B></TD></TR>")
          linkTypes.forEach { type ->
            val bgColor = if (type.color == null) "" else " BGCOLOR=\"${type.color}\""
            val text = type.style?.value?.capitalized() ?: "Solid"
            val style = withFontColor(text, type.properties(Graphviz))
            appendLine("  <TR><TD>${type.displayName}</TD><TD$bgColor>$style</TD></TR>")
          }
          appendLine("</TABLE>")
          appendLine(">];")
        }
      }

      appendLine("}")
    }

    outputFile.writeText(dotFileContents)
    logIfConfigured(outputFile)
  }

  private fun withFontColor(text: String, properties: Map<String, String>): String {
    val fontColor = properties["fontcolor"] ?: return text
    return "<FONT COLOR=\"$fontColor\">$text</FONT>"
  }

  @Suppress("MagicNumber")
  private fun tableAttributes(config: DotConfig) =
    listOf(
        "BORDER" to 0,
        "CELLBORDER" to 1,
        "CELLSPACING" to 0,
        "CELLPADDING" to 4,
        "COLOR" to config.fontColor(),
      )
      .mapNotNull { (k, v) ->
        if (v == null) null else k to v.toString()
      }

  private fun DotConfig.fontColor(): String? =
    listOf(nodeAttributes, graphAttributes).firstNotNullOfOrNull { it?.get("fontcolor") }

  @DisableCachingByDefault
  internal abstract class WriteGraphvizLegendDummy : WriteGraphvizLegend(), DummyAtlasGenerationTask

  internal companion object {
    internal fun real(context: AtlasContext, spec: GraphvizSpec) =
      register<WriteGraphvizLegend>(
        context = context,
        spec = spec,
        outputFile =
          context.project.intermediateFile(
            config = context.config,
            framework = Graphviz,
            variant = Legend,
            fileExtension = spec.fileExtension.get(),
            inBuildDir = spec.intermediateFilesInBuildDir.get(),
          ),
      )

    internal fun dummy(context: AtlasContext, spec: GraphvizSpec) =
      register<WriteGraphvizLegendDummy>(
        context = context,
        spec = spec,
        outputFile = context.project.atlasBuildDirectory.get().file("legend-temp.dot").asFile,
      )

    internal inline fun <reified T : WriteGraphvizLegend> register(
      context: AtlasContext,
      spec: GraphvizSpec,
      outputFile: File,
    ): TaskProvider<T> =
      with(context.project) {
        val name = "write${T::class.qualifier}GraphvizLegend"
        val writeLegend =
          tasks.register(name, T::class.java) { task ->
            task.outputFile.set(outputFile)
          }

        writeLegend.configure { task ->
          task.projectTypes.convention(context.projectTypes)
          task.linkTypes.convention(context.linkTypes)
          task.config.convention(DotConfig(context.config, spec))
        }

        return writeLegend
      }
  }
}
