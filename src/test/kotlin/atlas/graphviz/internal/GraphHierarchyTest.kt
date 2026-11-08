package atlas.graphviz.internal

import assertk.assertThat
import assertk.assertions.isEqualTo
import atlas.core.ProjectType
import atlas.core.internal.Subgraph
import atlas.core.internal.buildGraphElements
import atlas.test.OneLevelOfSubprojects
import atlas.test.SingleNestedProjectWithNoLinks
import atlas.test.TwoLevelsOfSubprojects
import atlas.test.node
import kotlin.test.Test

internal class GraphHierarchyTest {
  @Test
  fun `Single-level groups`() {
    val dataSubGraph =
      Subgraph(
        path = listOf("data"),
        elements =
          mutableListOf(
            node(path = ":data:a"),
            node(path = ":data:b"),
          ),
      )
    val domainSubGraph =
      Subgraph(
        path = listOf("domain"),
        elements =
          mutableListOf(
            node(path = ":domain:a"),
            node(path = ":domain:b"),
          ),
      )
    val uiSubGraph =
      Subgraph(
        path = listOf("ui"),
        elements =
          mutableListOf(
            node(path = ":ui:a"),
            node(path = ":ui:b"),
            node(path = ":ui:c"),
          ),
      )

    val elements =
      buildGraphElements(
        typedProjects = OneLevelOfSubprojects.projects,
        links = OneLevelOfSubprojects.links,
        thisPath = ":app",
      )
    assertThat(elements)
      .isEqualTo(
        listOf(
          node(path = ":app"),
          dataSubGraph,
          domainSubGraph,
          uiSubGraph,
        ),
      )
  }

  @Test
  fun `Multi-level groups`() {
    val dataSubGraph =
      Subgraph(
        path = listOf("data"),
        elements =
          mutableListOf(
            node(path = ":data:a"),
            node(path = ":data:b"),
            Subgraph(
              path = listOf("data", "sub"),
              elements =
                listOf(
                  node(path = ":data:sub:sub1"),
                  node(path = ":data:sub:sub2"),
                ),
            ),
          ),
      )
    val domainSubGraph =
      Subgraph(
        path = listOf("domain"),
        elements =
          mutableListOf(
            node(path = ":domain:a"),
            node(path = ":domain:b"),
          ),
      )
    val uiSubGraph =
      Subgraph(
        path = listOf("ui"),
        elements =
          mutableListOf(
            node(path = ":ui:a"),
            node(path = ":ui:b"),
            node(path = ":ui:c"),
          ),
      )

    val elements =
      buildGraphElements(
        typedProjects = TwoLevelsOfSubprojects.projects,
        links = TwoLevelsOfSubprojects.links,
        thisPath = ":app",
      )
    assertThat(elements)
      .isEqualTo(
        listOf(
          node(path = ":app"),
          dataSubGraph,
          domainSubGraph,
          uiSubGraph,
        ),
      )
  }

  @Test
  fun `Single nested project, no links, grouped`() {
    val node =
      node(
        path = ":a:b",
        type = ProjectType(name = "red", color = "red"),
      )
    val aGraph =
      Subgraph(
        path = listOf("a"),
        elements = listOf(node),
      )

    val elements =
      buildGraphElements(
        typedProjects = SingleNestedProjectWithNoLinks.projects,
        links = SingleNestedProjectWithNoLinks.links,
        thisPath = ":a:b",
      )
    assertThat(elements).isEqualTo(listOf(aGraph))
  }
}
