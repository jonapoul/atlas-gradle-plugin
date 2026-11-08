package atlas.core.internal

import atlas.core.Replacement

internal sealed interface GraphElement

internal data class Node(val typedProject: TypedProject) : GraphElement

internal data class Subgraph(
  val path: List<String>,
  val elements: List<GraphElement>,
) : GraphElement {
  val name: String = path.last()
}

internal fun buildGraphElements(
  typedProjects: Set<TypedProject>,
  links: Set<ProjectLink>,
  thisPath: String,
  replacements: Set<Replacement> = emptySet(),
): List<GraphElement> {
  val cleanedProjects = typedProjects.map { it.cleaned(replacements) }
  val cleanedLinks = links.map { it.cleaned(replacements) }
  val linkedPaths = cleanedLinks.linkedPaths()
  val cleanedThisPath = thisPath.cleaned(replacements)
  return buildHierarchy(
    nodeData =
      cleanedProjects
        .filter { project ->
          project.projectPath in linkedPaths ||
            (cleanedLinks.isEmpty() && project.projectPath == cleanedThisPath)
        }
        .map { project -> project to project.projectPath.split(":").filter { it.isNotEmpty() } },
  )
}

private fun buildHierarchy(nodeData: List<Pair<TypedProject, List<String>>>): List<GraphElement> {
  val elements = mutableListOf<GraphElement>()

  val (rootNodes, nestedNodes) =
    nodeData.partition { (_, parts) ->
      parts.size == 1 || parts.isEmpty()
    }

  rootNodes.forEach { (project, _) ->
    elements.add(Node(project))
  }

  nestedNodes
    .groupBy { (_, parts) -> parts.first() }
    .forEach { (firstPart, groupNodes) ->
      val subgraphElements = buildSubgraphElements(groupNodes, level = 1)
      elements.add(Subgraph(listOf(firstPart), subgraphElements))
    }

  return elements
}

private fun buildSubgraphElements(
  nodeData: List<Pair<TypedProject, List<String>>>,
  level: Int,
): List<GraphElement> {
  val elements = mutableListOf<GraphElement>()

  val (leafNodes, deeperNodes) =
    nodeData.partition { (_, parts) ->
      parts.size == level + 1
    }

  leafNodes.forEach { (project, _) ->
    elements.add(Node(project))
  }

  // Group deeper nodes by their part at this level
  deeperNodes
    .groupBy { (_, parts) -> parts[level] }
    .forEach { (_, groupNodes) ->
      val parentPath = groupNodes.first().second.take(level + 1)
      val subgraphElements = buildSubgraphElements(groupNodes, level = level + 1)
      elements.add(Subgraph(parentPath, subgraphElements))
    }

  return elements
}

internal fun String.cleaned(replacements: Set<Replacement>): String {
  var string = this
  replacements.forEach { r -> string = string.replace(r.pattern, r.replacement) }
  return string
}

internal fun TypedProject.cleaned(replacements: Set<Replacement>): TypedProject =
  copy(projectPath = projectPath.cleaned(replacements))

private fun ProjectLink.cleaned(replacements: Set<Replacement>) =
  ProjectLink(
    fromPath = fromPath.cleaned(replacements),
    toPath = toPath.cleaned(replacements),
    configuration = configuration,
    type = type,
  )

internal abstract class ChartWriter {
  abstract operator fun invoke(): String

  protected abstract val typedProjects: Set<TypedProject>
  protected abstract val links: Set<ProjectLink>
  protected abstract val replacements: Set<Replacement>
  protected abstract val groupProjects: Boolean
  protected abstract val thisPath: String

  // [typedProjects], [links] and [thisPath] with the path replacements applied. Writers only use
  // these, so every path is replaced exactly once and nodes always match the links pointing at
  // them.
  protected val chartProjects: List<TypedProject> by lazy {
    typedProjects.map { it.cleaned(replacements) }
  }
  protected val chartLinks: List<ProjectLink> by lazy { links.map { it.cleaned(replacements) } }
  protected val chartThisPath: String by lazy { thisPath.cleaned(replacements) }

  // Paths at either end of [chartLinks], so checking whether a project is in the chart doesn't
  // scan them
  protected val linkedPaths: Set<String> by lazy { chartLinks.linkedPaths() }

  // How many subgraphs deep we currently are, i.e. 0 while writing top-level nodes
  protected var subgraphNestingLevel: Int = 0
    private set

  protected abstract fun IndentedStringBuilder.appendProject(project: TypedProject)

  protected abstract fun IndentedStringBuilder.appendSubgraphHeader(graph: Subgraph)

  protected abstract fun IndentedStringBuilder.appendSubgraphFooter()

  protected fun IndentedStringBuilder.appendProjects() {
    if (groupProjects) {
      val elements = buildGraphElements(chartProjects.toSet(), chartLinks.toSet(), chartThisPath)
      for (element in elements) {
        appendGraphNode(element)
      }
    } else {
      appendUngroupedNodes()
    }
  }

  private fun IndentedStringBuilder.appendGraphNode(element: GraphElement) {
    when (element) {
      is Node -> appendProject(element.typedProject)
      is Subgraph -> appendSubgraph(element)
    }
  }

  private fun IndentedStringBuilder.appendSubgraph(graph: Subgraph) {
    appendSubgraphHeader(graph)
    subgraphNestingLevel++
    indent {
      for (element in graph.elements) {
        appendGraphNode(element)
      }
    }
    subgraphNestingLevel--
    appendSubgraphFooter()
  }

  protected fun IndentedStringBuilder.appendUngroupedNodes() {
    chartProjects
      .filter { it.projectPath in linkedPaths }
      .sortedBy { it.projectPath }
      .forEach { appendProject(it) }

    if (chartLinks.isEmpty() || chartProjects.size == 1) {
      // Single-project case - we still want this project to be shown along with its type
      chartProjects.firstOrNull { it.projectPath == chartThisPath }?.let { appendProject(it) }
    }
  }

  protected fun String.nodeLabel(): String {
    if (!groupProjects) return this
    val segment = split(":").filter { it.isNotEmpty() }.getOrNull(subgraphNestingLevel)
    return if (segment == null) this else ":$segment"
  }

  internal companion object {
    internal val SUPPORTED_CHAR_REGEX: Regex =
      "^[a-zA-Z\\u0080-\\u00FF_][a-zA-Z\\u0080-\\u00FF_0-9]*$".toRegex()
  }
}
