@file:Suppress("TooManyFunctions", "LongParameterList")

package atlas.d2.internal

import atlas.core.LinkType
import atlas.core.ProjectType
import atlas.core.Replacement
import atlas.core.internal.ChartWriter
import atlas.core.internal.IndentedStringBuilder
import atlas.core.internal.ProjectLink
import atlas.core.internal.Subgraph
import atlas.core.internal.TypedProject
import atlas.core.internal.buildIndentedString
import atlas.core.internal.contains

internal class D2Writer(
  override val typedProjects: Set<TypedProject>,
  override val links: Set<ProjectLink>,
  override val replacements: Set<Replacement>,
  override val thisPath: String,
  override val groupProjects: Boolean,
  private val pathToClassesFile: String,
) : ChartWriter() {
  private var subgraphNestingLevel = 0

  override fun invoke(): String = buildIndentedString {
    appendProjects()
    appendLinks()
    appendLegend()
    appendImports()
  }

  /**
   * Imported last, not first. Globs in the classes file - the ones [atlas.d2.D2GlobalPropsSpec]
   * writes, for instance - are live: d2 re-runs them over the whole map every time a later key
   * creates a field. Importing at the top means every project and every link replays them, which
   * blows d2's 65536 work unit glob budget on any chart with a few dozen projects. Importing at the
   * bottom leaves nothing for them to replay against, so they're applied exactly once. The rendered
   * diagram is identical either way.
   */
  private fun IndentedStringBuilder.appendImports() {
    appendLine("...@$pathToClassesFile")
  }

  override fun IndentedStringBuilder.appendSubgraphHeader(graph: Subgraph) {
    val key = graph.path[subgraphNestingLevel].cleaned()
    appendLine("$key: :${graph.name} {")
    indent { appendLine("class: $CONTAINER_CLASS") }
    subgraphNestingLevel++
  }

  override fun IndentedStringBuilder.appendSubgraphFooter() {
    appendLine("}")
    subgraphNestingLevel--
  }

  override fun IndentedStringBuilder.appendProject(project: TypedProject) {
    val (path, type) = project
    val key = path.localKey()
    val name = if (groupProjects) ":$key" else path
    if (type == null) {
      // just list the project with label
      appendLine("$key: $name")
    } else {
      // also include the class key
      appendLine("$key: $name { class: ${type.classId} }")
    }
  }

  private fun IndentedStringBuilder.appendLinks() {
    links
      .map { l -> l.copy(fromPath = l.fromPath.cleaned(), toPath = l.toPath.cleaned()) }
      .sortedWith(compareBy({ it.fromPath.fullKey() }, { it.toPath.fullKey() }))
      .forEach { l ->
        val suffix = l.type?.let { type -> " { class: ${type.classId} }" }.orEmpty()
        appendLine("${l.fromPath.fullKey()} -> ${l.toPath.fullKey()}$suffix")
      }
  }

  private fun IndentedStringBuilder.appendLegend() {
    val projectTypes =
      typedProjects
        .filter { it in links }
        .mapNotNull { it.type }
        .distinct()
        .ifEmpty {
          // single-project case
          typedProjects.firstOrNull { it.projectPath == thisPath }?.type?.let(::listOf).orEmpty()
        }

    val linkTypes = links.mapNotNull { it.type }.distinct()

    if (projectTypes.isEmpty() && linkTypes.isEmpty()) return

    appendLine("vars: {")
    indent {
      appendLine("d2-legend: {")
      indent {
        projectTypes.forEach { appendProjectType(it) }
        if (linkTypes.isNotEmpty()) {
          val exampleNodes = getExampleNodesForLinks()
          linkTypes.forEach { appendLinkType(it, exampleNodes) }
        }
      }
      appendLine("}")
    }
    appendLine("}")
  }

  private fun IndentedStringBuilder.getExampleNodesForLinks(): Pair<ProjectType, ProjectType> {
    val dummy = ProjectType(name = "dummy1", color = "", properties = emptyMap())
    val dummy2 = ProjectType(name = "dummy2", color = "", properties = emptyMap())
    appendLine("${dummy.classId}.class: $HIDDEN_CLASS")
    appendLine("${dummy2.classId}.class: $HIDDEN_CLASS")
    return dummy to dummy2
  }

  private fun IndentedStringBuilder.appendProjectType(type: ProjectType) {
    appendLine("${type.classId}: ${type.name} { class: ${type.classId} }")
  }

  private fun IndentedStringBuilder.appendLinkType(
    type: LinkType,
    exampleNodes: Pair<ProjectType, ProjectType>,
  ) {
    val (a, b) = exampleNodes
    appendLine("${a.classId} -> ${b.classId}: ${type.displayName} { class: ${type.classId} }")
  }

  // Colons in d2 have special meaning, so strip them out of the key string. Not visible in the
  // diagram anyway.
  private fun String.fullKey(): String {
    val stripped =
      DISALLOWED_SUBSTRINGS.fold(initial = cleaned().removePrefix(":").removePrefix("-")) {
        acc,
        string ->
        acc.replace(string, "")
      }

    return if (groupProjects) {
      // Dots are nested group identifiers, so only use them if we have grouping enabled
      stripped.replace(":", ".")
    } else {
      // otherwise just use underscores, so long as they're unique it shouldn't matter
      stripped.replace(":", "_")
    }
  }

  private fun String.localKey(): String =
    if (groupProjects) {
      // "path.to.my.project" -> "project" for subgraphNestingLevel=3
      fullKey().split(".")[subgraphNestingLevel]
    } else {
      fullKey()
    }

  private companion object {
    val DISALLOWED_SUBSTRINGS = setOf("{", "}", "->", "--", "<-", "<->", "|", "#", "\"", "'")
  }
}
