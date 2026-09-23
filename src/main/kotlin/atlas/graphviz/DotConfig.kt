package atlas.graphviz

import atlas.core.internal.AtlasConfig
import java.io.Serializable as JSerializable
import kotlinx.serialization.Serializable as KSerializable

/**
 * Used to configure the [atlas.graphviz.tasks.WriteGraphvizChart] and
 * [atlas.graphviz.tasks.WriteGraphvizLegend] tasks.
 */
@KSerializable
public class DotConfig(
  public val displayLinkLabels: Boolean? = null,
  public val layoutEngine: LayoutEngine? = null,
  public val nodeAttributes: Map<String, String>? = null,
  public val edgeAttributes: Map<String, String>? = null,
  public val graphAttributes: Map<String, String>? = null,
  public val clusterAttributes: Map<String, String>? = null,
) : JSerializable

internal fun DotConfig(
  config: AtlasConfig,
  spec: GraphvizSpec,
): DotConfig =
  DotConfig(
    displayLinkLabels = config.displayLinkLabels,
    layoutEngine = spec.layoutEngine.orNull,
    nodeAttributes = spec.node.properties.orNull,
    edgeAttributes = spec.edge.properties.orNull,
    graphAttributes = spec.graph.properties.orNull,
    clusterAttributes = spec.cluster.properties.orNull,
  )
