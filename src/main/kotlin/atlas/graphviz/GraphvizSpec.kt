package atlas.graphviz

import atlas.core.AtlasDsl
import atlas.core.AtlasSpec
import org.gradle.api.Action
import org.gradle.api.provider.Property

/**
 * Used to configure Graphviz output from Atlas. For barebones output with the default config, call
 * `graphviz()` in the `atlas` block. Or for a more fleshed-out config:
 * ```kotlin
 * atlas {
 *   // other Atlas config
 *
 *   graphviz {
 *     pathToDotCommand = "/custom/path/to/dot"
 *     fileFormat = FileFormat.Svg
 *     intermediateFilesInBuildDir = false
 *     layoutEngine = LayoutEngine.Dot
 *
 *     node {
 *       ...
 *     }
 *
 *     edge {
 *       ...
 *     }
 *
 *     graph {
 *       ...
 *     }
 *
 *     cluster {
 *       ...
 *     }
 *   }
 * }
 * ```
 */
@AtlasDsl
public interface GraphvizSpec : AtlasSpec {
  /**
   * Use this if you want to specify a "dot" command which isn't on the system path. This should be
   * an absolute path.
   */
  public val pathToDotCommand: Property<String>

  /**
   * The format of the rendered chart file. Defaults to [FileFormat.Svg].
   *
   * Also controlled by the `atlas.graphviz.fileFormat` Gradle property.
   */
  public val fileFormat: Property<FileFormat>

  /**
   * Set to true to write the `chart-graphviz.dot` and `legend-graphviz.dot` files to `build/atlas/`
   * instead of the project directory and the root project's `atlas/` directory. Only the rendered
   * image goes alongside the project. Defaults to true.
   *
   * [atlas.core.AtlasExtension.checkOutputs] only verifies these files, so no Graphviz check tasks
   * are registered while this is enabled.
   *
   * Also controlled by the `atlas.graphviz.intermediateFilesInBuildDir` Gradle property.
   */
  public val intermediateFilesInBuildDir: Property<Boolean>

  /**
   * Customise the layout engine used to organise your project nodes in the chart. Unset by default,
   * so Graphviz uses [LayoutEngine.Dot].
   *
   * Also controlled by the `atlas.graphviz.layoutEngine` Gradle property.
   */
  public val layoutEngine: Property<LayoutEngine>

  /**
   * Configure the attributes applied by default to all project nodes, unless overridden by that
   * node's [atlas.core.ProjectTypeSpec].
   */
  public val node: NodeAttributes

  public fun node(action: Action<NodeAttributes>)

  /**
   * Configure the attributes applied by default to all links between nodes, unless overridden by
   * that link's [atlas.core.LinkTypeSpec].
   */
  public val edge: EdgeAttributes

  public fun edge(action: Action<EdgeAttributes>)

  /** Configure the attributes applied to the root graph. */
  public val graph: GraphAttributes

  public fun graph(action: Action<GraphAttributes>)

  /**
   * Configure the attributes applied to each cluster of grouped projects. Only used when
   * [atlas.core.AtlasExtension.groupProjects] is enabled.
   */
  public val cluster: ClusterAttributes

  public fun cluster(action: Action<ClusterAttributes>)
}
