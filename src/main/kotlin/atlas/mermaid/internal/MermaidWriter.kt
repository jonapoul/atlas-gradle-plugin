package atlas.mermaid.internal

import atlas.core.LinkStyle
import atlas.core.LinkType
import atlas.core.Replacement
import atlas.core.internal.ChartWriter
import atlas.core.internal.IndentedStringBuilder
import atlas.core.internal.ProjectLink
import atlas.core.internal.Subgraph
import atlas.core.internal.TypedProject
import atlas.core.internal.buildIndentedString
import atlas.core.internal.contains
import atlas.core.internal.sortedByKeys
import atlas.mermaid.MermaidConfig

internal class MermaidWriter(
  override val typedProjects: Set<TypedProject>,
  override val links: Set<ProjectLink>,
  override val replacements: Set<Replacement>,
  override val thisPath: String,
  override val groupProjects: Boolean,
  private val config: MermaidConfig,
) : ChartWriter() {
  override fun invoke(): String = buildIndentedString {
    appendConfig()
    appendLine("graph TD")
    indent {
      appendProjects()
      appendTypes()
      appendLinks()
    }
  }

  private fun IndentedStringBuilder.appendConfig() {
    val layout = config.layout
    val look = config.look
    val theme = config.theme
    val themeVariables = config.themeVariables
    if (listOfNotNull(layout, look, theme, themeVariables).isEmpty()) return

    appendLine("---")
    appendLine("config:")
    indent {
      layout?.let { appendLine("layout: $it") }
      look?.let { appendLine("look: $it") }
      theme?.let { appendLine("theme: $it") }
      if (layout != null) appendProperties(name = layout, config.layoutProperties)
      appendProperties(name = "themeVariables", themeVariables)
    }
    appendLine("---")
  }

  private fun IndentedStringBuilder.appendProperties(
    name: String,
    properties: Map<String, String>?,
  ) {
    if (properties.isNullOrEmpty()) return
    appendLine("$name:")
    indent {
      for ((key, value) in properties) {
        appendLine("$key: $value")
      }
    }
  }

  override fun IndentedStringBuilder.appendSubgraphHeader(graph: Subgraph) {
    val cleanedProjectName = graph.name.filter { it.toString().matches(SUPPORTED_CHAR_REGEX) }
    appendLine("subgraph $cleanedProjectName[\":${graph.name}\"]")
  }

  override fun IndentedStringBuilder.appendSubgraphFooter() {
    appendLine("end")
  }

  override fun IndentedStringBuilder.appendProject(project: TypedProject) {
    appendLine("${project.label}[\"${project.projectPath.nodeLabel()}\"]")
  }

  private fun IndentedStringBuilder.appendTypes() {
    for (project in typedProjects) {
      if (project !in links && project.projectPath != thisPath) continue

      val attrs = Attrs("fill" to project.type?.color)

      project.type?.properties(Mermaid)?.forEach { (key, value) -> attrs[key] = value }

      if (attrs.isNotEmpty()) {
        appendLine("style ${project.cleaned().label} $attrs")
      }
    }
  }

  // See https://mermaid.js.org/syntax/flowchart.html#links-between-nodes
  private fun IndentedStringBuilder.appendLinks() {
    val animateLinks = config.animateLinks == true
    links.forEachIndexed { i, link ->
      val arrowPrefix = if (animateLinks) "link$i@" else ""
      val style = link.type?.style ?: Solid

      val arrow = getArrow(link.type, style)
      val from = typedProjects.first { it.projectPath == link.fromPath }.cleaned().label
      val to = typedProjects.first { it.projectPath == link.toPath }.cleaned().label
      appendLine("$from $arrowPrefix$arrow $to")

      link.type?.let { type ->
        val attrs = mutableMapOf<String, String>()
        type.color?.let { attrs["stroke"] = it }
        attrs.putAll(type.properties(Mermaid))
        if (attrs.isNotEmpty()) {
          val attrString = attrs.sortedByKeys().joinToString(separator = ",") { (k, v) -> "$k:$v" }
          appendLine("linkStyle $i $attrString")
        }
      }
    }

    if (animateLinks) {
      for (i in links.indices) {
        appendLine("link$i@{ animate: true }")
      }
    }
  }

  private fun getArrow(type: LinkType?, style: LinkStyle): String {
    val showLabels = config.displayLinkLabels == true
    val name = type?.displayName
    // Mermaid has no dotted or tapered links, so they fall back to the closest thing it has
    return if (showLabels && name != null) {
      when (style) {
        Solid,
        Tapered -> "--$name-->"
        Bold -> "==$name==>"
        Dashed,
        Dotted -> "-.$name.->"
        Invisible -> "~~~|$name|"
      }
    } else {
      when (style) {
        Solid,
        Tapered -> "-->"
        Bold -> "==>"
        Dashed,
        Dotted -> "-.->"
        Invisible -> "~~~"
      }
    }
  }

  private val TypedProject.label: String
    get() = projectPath.label

  private val String.label: String
    get() = split(":", "-", " ").joinToString("_")

  @Suppress("SpreadOperator")
  private class Attrs private constructor(private val delegate: MutableMap<String, Any?>) :
    MutableMap<String, Any?> by delegate {
    constructor(vararg entries: Pair<String, Any?>) : this(mutableMapOf(*entries))

    init {
      delegate.toList().forEach { (key, value) ->
        if (value == null) delegate.remove(key)
      }
    }

    override fun toString(): String {
      if (values.filterNotNull().isEmpty()) return ""
      return mapNotNull { (k, v) -> if (v == null) null else "$k:$v" }.joinToString(separator = ",")
    }
  }
}
