@file:Suppress("unused", "SpellCheckingInspection") // public API

package atlas.mermaid

import atlas.core.StringEnum

/**
 * [ElkLayoutSpec]-specific option affecting how nodes are placed. Default value is [BrandesKoepf].
 * See
 * [the Mermaid docs](https://mermaid.js.org/config/schema-docs/config-properties-elk.html#nodeplacementstrategy)
 */
public enum class NodePlacementStrategy(override val value: String) : StringEnum {
  Simple("SIMPLE"),
  NetworkSimplex("NETWORK_SIMPLEX"),
  LinearSegments("LINEAR_SEGMENTS"),
  BrandesKoepf("BRANDES_KOEPF");

  override fun toString(): String = value
}

/**
 * This strategy decides how to find cycles in the graph and deciding which edges need adjustment to
 * break loops. Default value is [GreedyModelOrder]. See
 * [the Mermaid docs](https://mermaid.js.org/config/schema-docs/config-properties-elk.html#cyclebreakingstrategy)
 */
public enum class CycleBreakingStrategy(override val value: String) : StringEnum {
  Greedy("GREEDY"),
  DepthFirst("DEPTH_FIRST"),
  Interactive("INTERACTIVE"),
  ModelOrder("MODEL_ORDER"),
  GreedyModelOrder("GREEDY_MODEL_ORDER");

  override fun toString(): String = value
}

/**
 * Preserves the order of nodes and edges in the model file if this does not lead to additional edge
 * crossings. Depending on the strategy this is not always possible since the node and edge order
 * might be conflicting. Default value is [NodesAndEdges]. See
 * [the Mermaid docs](https://mermaid.js.org/config/schema-docs/config-properties-elk.html#considermodelorder)
 */
public enum class ConsiderModelOrder(override val value: String) : StringEnum {
  None("NONE"),
  NodesAndEdges("NODES_AND_EDGES"),
  PreferEdges("PREFER_EDGES"),
  PreferNodes("PREFER_NODES");

  override fun toString(): String = value
}

/**
 * Defaults to [Classic]. See
 * [the Mermaid docs](https://mermaid.js.org/intro/syntax-reference.html#layout-and-look)
 */
public enum class Look(override val value: String) : StringEnum {
  Classic("classic"),
  HandDrawn("handDrawn");

  override fun toString(): String = value
}

/** See [the Mermaid docs](https://mermaid.js.org/config/theming.html) */
public enum class Theme(override val value: String) : StringEnum {
  Default("default"),
  Neutral("neutral"),
  Dark("dark"),
  Forest("forest"),
  Base("base");

  override fun toString(): String = value
}
