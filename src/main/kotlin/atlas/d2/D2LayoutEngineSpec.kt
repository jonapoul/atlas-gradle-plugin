package atlas.d2

import atlas.core.AtlasDsl
import atlas.core.PropertiesSpec
import org.gradle.api.Action
import org.gradle.api.provider.Property

/**
 * Chooses and configures the engine D2 uses to arrange the chart. Call only one of [elk], [dagre]
 * or [tala] - if you call more than one, the last one wins.
 */
@AtlasDsl
public interface D2LayoutEngineSpec : PropertiesSpec {
  /**
   * The engine picked by calling [elk], [dagre] or [tala]. Unset by default, so D2 uses
   * [LayoutEngine.Dagre].
   *
   * Also controlled by the `atlas.d2.layoutEngine` Gradle property.
   */
  public val layoutEngine: Property<LayoutEngine>

  /** Configure ELK layout engine for the output chart. */
  public val elk: D2ElkSpec

  public fun elk(config: Action<D2ElkSpec>? = null)

  /** Configure DAGRE layout engine for the output chart. This is the default engine. */
  public val dagre: D2DagreSpec

  public fun dagre(config: Action<D2DagreSpec>? = null)

  /**
   * Configure the TALA layout engine for the output chart. TALA is D2's own layout engine, bundled
   * with D2 and open source since 0.9.0 - before that it was a paid closed-source plugin you had to
   * install separately.
   */
  public val tala: D2TalaSpec

  public fun tala(config: Action<D2TalaSpec>? = null)
}

/** CLI configuration options, found from running `d2 layout elk` in the CLI. */
@AtlasDsl
public interface D2ElkSpec : PropertiesSpec {
  /**
   * Layout algorithm (default [ElkAlgorithm.Layered])
   *
   * Also controlled by the `atlas.d2.layoutEngine.elk.algorithm` Gradle property.
   */
  public var algorithm: ElkAlgorithm?

  /**
   * The spacing to be preserved between nodes and edges that are routed next to the node’s layer
   * (default 40)
   *
   * Also controlled by the `atlas.d2.layoutEngine.elk.edgeNodeBetweenLayers` Gradle property.
   */
  public var edgeNodeBetweenLayers: Int?

  /**
   * The spacing to be preserved between any pair of nodes of two adjacent layers (default 70)
   *
   * Also controlled by the `atlas.d2.layoutEngine.elk.nodeNodeBetweenLayers` Gradle property.
   */
  public var nodeNodeBetweenLayers: Int?

  /**
   * Spacing to be preserved between a node and its self loops (default 50)
   *
   * Also controlled by the `atlas.d2.layoutEngine.elk.nodeSelfLoop` Gradle property.
   */
  public var nodeSelfLoop: Int?

  /**
   * The padding to be left to a parent element’s border when placing child elements (default
   * "[top=50,left=50,bottom=50,right=50]")
   *
   * Also controlled by the `atlas.d2.layoutEngine.elk.padding` Gradle property.
   */
  public var padding: String?

  /**
   * Spacing between links routed between two layers (default 50). Every link between two layers
   * adds this much, so lowering it shortens charts with many links. Requires D2 0.10.0 or newer.
   *
   * Also controlled by the `atlas.d2.layoutEngine.elk.edgeEdgeBetweenLayers` Gradle property.
   */
  public var edgeEdgeBetweenLayers: Int?

  /**
   * Minimum spacing between nodes in the same layer (default 20). Requires D2 0.10.0 or newer.
   *
   * Also controlled by the `atlas.d2.layoutEngine.elk.nodeNode` Gradle property.
   */
  public var nodeNode: Int?

  /**
   * Minimum spacing between a node and a link passing it in the same layer (default 40). Requires
   * D2 0.10.0 or newer.
   *
   * Also controlled by the `atlas.d2.layoutEngine.elk.edgeNode` Gradle property.
   */
  public var edgeNode: Int?

  /**
   * Minimum spacing between links passing through the same layer (default 10). Requires D2 0.10.0
   * or newer.
   *
   * Also controlled by the `atlas.d2.layoutEngine.elk.edgeEdge` Gradle property.
   */
  public var edgeEdge: Int?

  /**
   * How links are drawn (default [ElkEdgeRouting.Orthogonal]). Requires D2 0.10.0 or newer.
   *
   * Also controlled by the `atlas.d2.layoutEngine.elk.edgeRouting` Gradle property.
   */
  public var edgeRouting: ElkEdgeRouting?

  /**
   * Lets links into or out of the same node share one attachment point (default false). Tidies up
   * modules with lots of dependents. Requires D2 0.10.0 or newer.
   *
   * Also controlled by the `atlas.d2.layoutEngine.elk.mergeEdges` Gradle property.
   */
  public var mergeEdges: Boolean?

  /**
   * Routes backward links around the main flow (default false). Requires D2 0.10.0 or newer.
   *
   * Also controlled by the `atlas.d2.layoutEngine.elk.feedbackEdges` Gradle property.
   */
  public var feedbackEdges: Boolean?

  /**
   * Wraps a long chart into several rows (default [ElkWrappingStrategy.Off]). Requires D2 0.10.0 or
   * newer.
   *
   * Also controlled by the `atlas.d2.layoutEngine.elk.wrappingStrategy` Gradle property.
   */
  public var wrappingStrategy: ElkWrappingStrategy?

  /**
   * Keeps declaration order within each layer at the top level of the chart (default false).
   * Requires D2 0.10.0 or newer.
   *
   * Also controlled by the `atlas.d2.layoutEngine.elk.forceNodeModelOrder` Gradle property.
   */
  public var forceNodeModelOrder: Boolean?

  /**
   * The force model, for [ElkAlgorithm.Force] only (default [ElkForceModel.FruchtermanReingold]).
   * Requires D2 0.10.0 or newer.
   *
   * Also controlled by the `atlas.d2.layoutEngine.elk.forceModel` Gradle property.
   */
  public var forceModel: ElkForceModel?

  /**
   * Repulsion between unconnected nodes, for [ElkForceModel.Eades] only (default 5). Requires D2
   * 0.10.0 or newer.
   *
   * Also controlled by the `atlas.d2.layoutEngine.elk.forceRepulsion` Gradle property.
   */
  public var forceRepulsion: Float?

  /**
   * Target distance between the centres of linked nodes, for [ElkAlgorithm.Stress] only (default
   * 100). Must be positive. Requires D2 0.10.0 or newer.
   *
   * Also controlled by the `atlas.d2.layoutEngine.elk.stressDesiredEdgeLength` Gradle property.
   */
  public var stressDesiredEdgeLength: Int?

  /**
   * Spanning tree traversal, for [ElkAlgorithm.MrTree] only (default [ElkMrTreeSearchOrder.Dfs]).
   * Requires D2 0.10.0 or newer.
   *
   * Also controlled by the `atlas.d2.layoutEngine.elk.mrTreeSearchOrder` Gradle property.
   */
  public var mrTreeSearchOrder: ElkMrTreeSearchOrder?

  /**
   * Sibling ordering, for [ElkAlgorithm.MrTree] only (default [ElkMrTreeWeighting.ModelOrder]).
   * Requires D2 0.10.0 or newer.
   *
   * Also controlled by the `atlas.d2.layoutEngine.elk.mrTreeWeighting` Gradle property.
   */
  public var mrTreeWeighting: ElkMrTreeWeighting?

  /**
   * Initial distance between tree levels, for [ElkAlgorithm.Radial] only (default 0, which lets ELK
   * choose). Requires D2 0.10.0 or newer.
   *
   * Also controlled by the `atlas.d2.layoutEngine.elk.radialRadius` Gradle property.
   */
  public var radialRadius: Int?

  /**
   * How angular space is divided, for [ElkAlgorithm.Radial] only (default
   * [ElkRadialWedgeCriteria.NodeSize]). Requires D2 0.10.0 or newer.
   *
   * Also controlled by the `atlas.d2.layoutEngine.elk.radialWedgeCriteria` Gradle property.
   */
  public var radialWedgeCriteria: ElkRadialWedgeCriteria?

  /**
   * Compaction, for [ElkAlgorithm.Radial] only (default [ElkRadialCompactor.None]). Requires D2
   * 0.10.0 or newer.
   *
   * Also controlled by the `atlas.d2.layoutEngine.elk.radialCompactor` Gradle property.
   */
  public var radialCompactor: ElkRadialCompactor?

  /** Sets [padding] to the same value on all four sides. */
  public fun padding(all: Int)

  /** Sets [padding] to one value on the left and right sides, and another on the top and bottom. */
  public fun padding(horizontal: Int, vertical: Int)

  /** Sets [padding] to a different value per side. */
  public fun padding(top: Int, left: Int, bottom: Int, right: Int)
}

/** From running `d2 layout dagre` in the CLI. */
@AtlasDsl
public interface D2DagreSpec : PropertiesSpec {
  /**
   * Number of pixels that separate nodes horizontally. (default 60)
   *
   * Also controlled by the `atlas.d2.layoutEngine.dagre.nodeSep` Gradle property.
   */
  public var nodeSep: Int?

  /**
   * Number of pixels that separate edges horizontally. (default 20)
   *
   * Also controlled by the `atlas.d2.layoutEngine.dagre.edgeSep` Gradle property.
   */
  public var edgeSep: Int?
}

/**
 * From running `d2 layout tala` in the CLI, where seeds is the engine's only setting. Requires D2
 * 0.9.0 or newer.
 */
@AtlasDsl
public interface D2TalaSpec : PropertiesSpec {
  /**
   * Random seeds for TALA's layout attempts. D2 lays the chart out once per seed and keeps the best
   * complete result, so more seeds gives a tidier chart at the cost of a slower build. Layout is
   * deterministic for a given set of seeds.
   *
   * D2 takes at most 16 unique seeds and fails the build past that. Duplicates are dropped rather
   * than rejected, and an empty list is ignored, leaving D2's default in place. (default [1, 2, 3])
   *
   * Also controlled by the `atlas.d2.layoutEngine.tala.seeds` Gradle property, which takes a
   * comma-separated list.
   */
  public var seeds: List<Long>?
}
