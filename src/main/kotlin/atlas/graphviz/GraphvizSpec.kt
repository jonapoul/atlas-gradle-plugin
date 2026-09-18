package atlas.graphviz

import atlas.core.AtlasDsl
import atlas.core.AtlasSpec
import org.gradle.api.Action
import org.gradle.api.provider.Property

/**
 * Used to configure Graphviz output from Atlas. For barebones output to a `.dot` file, you can just
 * add the `"dev.jonpoulton.atlas.graphviz"` gradle plugin. Or for a more fleshed-out config:
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

  /** Manually interact with output formats from Graphviz. Defaults to [FileFormat.Svg]. */
  public val fileFormat: Property<FileFormat>

  /**
   * Set to true to write the `chart.dot` and `legend.dot` files to `build/atlas/graphviz/` instead
   * of the project's `atlas/graphviz/` directory. Only the rendered image goes in
   * `atlas/graphviz/`. Defaults to true.
   *
   * [atlas.core.AtlasExtension.checkOutputs] only verifies these files, so no Graphviz check tasks
   * are registered while this is enabled.
   */
  public val intermediateFilesInBuildDir: Property<Boolean>

  /**
   * Customise the layout engine used to organise your project nodes in the chart. Defaults to
   * [LayoutEngine.Dot].
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
}
