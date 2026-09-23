package atlas.graphviz.internal

import atlas.core.Replacement
import atlas.core.internal.ChartWriter
import atlas.core.internal.IndentedStringBuilder
import atlas.core.internal.ProjectLink
import atlas.core.internal.Subgraph
import atlas.core.internal.TypedProject
import atlas.core.internal.buildIndentedString
import atlas.graphviz.DotConfig

internal data class DotWriter(
  override val typedProjects: Set<TypedProject>,
  override val links: Set<ProjectLink>,
  override val replacements: Set<Replacement>,
  override val thisPath: String,
  override val groupProjects: Boolean,
  private val config: DotConfig,
) : ChartWriter() {
  override fun invoke(): String = buildIndentedString {
    appendLine("digraph {")
    indent {
      appendHeader()
      appendProjects()
      appendLinks()
    }
    appendLine("}")
  }

  private fun IndentedStringBuilder.appendHeader() {
    appendHeaderGroup(
      name = "edge",
      attrs = attrs(config.edgeAttributes),
    )
    appendHeaderGroup(
      name = "graph",
      attrs = Attrs("layout" to config.layoutEngine) + config.graphAttributes,
    )
    appendHeaderGroup(
      name = "node",
      attrs = attrs(config.nodeAttributes),
    )
  }

  private fun IndentedStringBuilder.appendLinks() {
    val displayLinkLabels = config.displayLinkLabels == true

    links
      .map { link -> link.copy(fromPath = link.fromPath.cleaned(), toPath = link.toPath.cleaned()) }
      .sortedWith(compareBy({ it.fromPath }, { it.toPath }))
      .forEach { (fromPath, toPath, _, type) ->
        val attrs =
          Attrs(
            "style" to type?.style?.value,
            "color" to type?.color,
            "label" to if (displayLinkLabels) type?.displayName else null,
          ) + type?.properties(Graphviz)
        appendLine("\"$fromPath\" -> \"$toPath\"$attrs")
      }
  }

  override fun IndentedStringBuilder.appendSubgraphHeader(graph: Subgraph) {
    val cleanedProjectName = graph.name.filter { it.toString().matches(SUPPORTED_CHAR_REGEX) }
    appendLine("subgraph cluster_$cleanedProjectName {")
    indent {
      appendLine("label = \":${graph.name}\"")
      config.clusterAttributes?.forEach { (key, value) -> appendLine("$key = \"$value\"") }
    }
  }

  override fun IndentedStringBuilder.appendSubgraphFooter() {
    appendLine("}")
  }

  override fun IndentedStringBuilder.appendProject(project: TypedProject) {
    val nodePath = project.projectPath.cleaned()
    val label = project.projectPath.nodeLabel()

    // Graphviz labels a node with its ID by default, so only say otherwise when it differs
    val attrs = Attrs("label" to label.takeIf { it != nodePath })

    project.type?.let { type ->
      val properties = mapOf("fillcolor" to type.color) + type.properties(Graphviz)
      properties.forEach { (key, value) -> attrs[key] = value }
    }

    appendLine("\"$nodePath\"$attrs")
  }
}
