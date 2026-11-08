package atlas.core.tasks

import assertk.assertThat
import assertk.assertions.containsExactlyInAnyOrder
import assertk.assertions.hasSize
import atlas.core.internal.ProjectLink
import atlas.core.tasks.WriteProjectTree.Companion.projectTree
import java.time.Duration
import kotlin.test.Test
import org.junit.jupiter.api.assertTimeoutPreemptively

internal class ProjectTreeTest {
  @Test
  fun `Diamond from the top includes every link`() {
    val links = setOf(link(":top", ":a"), link(":top", ":b"), link(":a", ":z"), link(":b", ":z"))

    assertThat(projectTree(links, ":top", alsoTraverseUpwards = false))
      .containsExactlyInAnyOrder(*links.toTypedArray())
  }

  @Test
  fun `Upwards traversal only follows links into this project`() {
    val links = setOf(link(":top", ":a"), link(":top", ":b"), link(":a", ":z"), link(":b", ":z"))

    assertThat(projectTree(links, ":a", alsoTraverseUpwards = true))
      .containsExactlyInAnyOrder(link(":top", ":a"), link(":a", ":z"))
  }

  @Test
  fun `Upwards links are skipped unless enabled`() {
    val links = setOf(link(":top", ":a"), link(":a", ":z"))

    assertThat(projectTree(links, ":a", alsoTraverseUpwards = false))
      .containsExactlyInAnyOrder(link(":a", ":z"))
  }

  @Test
  fun `Cycles terminate`() {
    val links = setOf(link(":a", ":b"), link(":b", ":c"), link(":c", ":a"))

    assertThat(projectTree(links, ":a", alsoTraverseUpwards = true))
      .containsExactlyInAnyOrder(*links.toTypedArray())
  }

  @Test
  fun `Densely layered graph is walked once per project`() {
    // 12 layers of 30 projects, each depending on 5 in the next layer: tens of millions of routes
    // from the top, which took forever when each route was walked separately
    val layers = 12
    val width = 30
    val links = buildSet {
      for (layer in 0 until layers - 1) {
        for (i in 0 until width) {
          for (k in 0 until 5) add(link(":l$layer:p$i", ":l${layer + 1}:p${(i + k) % width}"))
        }
      }
    }

    val tree =
      assertTimeoutPreemptively(Duration.ofSeconds(5)) {
        projectTree(links, ":l0:p0", alsoTraverseUpwards = true)
      }

    // From :l0:p0, layer n reaches 1 + 4n projects until the layer is full
    val expected = (0 until layers - 1).sumOf { layer -> minOf(width, 1 + 4 * layer) * 5 }
    assertThat(tree).hasSize(expected)
  }

  private fun link(from: String, to: String) =
    ProjectLink(fromPath = from, toPath = to, configuration = "implementation", type = null)
}
