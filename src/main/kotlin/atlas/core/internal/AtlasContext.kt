package atlas.core.internal

import atlas.core.Framework
import atlas.core.LinkType
import atlas.core.ProjectType
import atlas.d2.internal.D2SpecImpl
import atlas.graphviz.internal.GraphvizSpecImpl
import atlas.mermaid.internal.MermaidSpecImpl
import org.gradle.api.Project
import org.gradle.api.file.FileCollection

/**
 * Everything a project needs in order to register its Atlas tasks. Replaces the root project's
 * extension, which under isolated projects a subproject may not read.
 */
internal class AtlasContext(
  val project: Project,
  val config: AtlasConfig,
  private val wiring: AtlasWiring,
) {
  val d2: D2SpecImpl
    get() = wiring.d2

  val graphviz: GraphvizSpecImpl
    get() = wiring.graphviz

  val mermaid: MermaidSpecImpl
    get() = wiring.mermaid

  val projectTypes: List<ProjectType>
    get() = config.projectTypes.map { it.type }

  val linkTypes: List<LinkType>
    get() = config.linkTypes

  val isRoot: Boolean
    get() = project.path == ":"

  private val rootDependencies by lazy {
    project.atlasDependencies("atlasFromRoot", listOf(":"), default = AtlasArtifact.CollatedLinks)
  }

  /** Resolves a file the root project publishes. */
  fun fromRoot(artifact: AtlasArtifact): FileCollection =
    rootDependencies.atlasArtifacts(artifact, lenient = false).artifactFiles

  fun specFor(framework: Framework) =
    when (framework) {
      D2 -> d2
      Graphviz -> graphviz
      Mermaid -> mermaid
    }
}
