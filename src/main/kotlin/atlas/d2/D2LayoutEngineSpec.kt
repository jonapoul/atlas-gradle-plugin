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
  /** Layout algorithm (default [ElkAlgorithm.Layered]) */
  public var algorithm: ElkAlgorithm?

  /**
   * The spacing to be preserved between nodes and edges that are routed next to the node’s layer
   * (default 40)
   */
  public var edgeNodeBetweenLayers: Int?

  /** The spacing to be preserved between any pair of nodes of two adjacent layers (default 70) */
  public var nodeNodeBetweenLayers: Int?

  /** Spacing to be preserved between a node and its self loops (default 50) */
  public var nodeSelfLoop: Int?

  /**
   * The padding to be left to a parent element’s border when placing child elements (default
   * "[top=50,left=50,bottom=50,right=50]")
   */
  public var padding: String?

  /** Sets [padding] to the same value on all four sides. */
  public fun padding(all: Int)

  /** Sets [padding] to one value on the left and right sides, and another on the top and bottom. */
  public fun padding(horizontal: Int = 50, vertical: Int = 50)

  /** Sets [padding] to a different value per side. */
  public fun padding(top: Int = 50, left: Int = 50, bottom: Int = 50, right: Int = 50)
}

/** From running `d2 layout dagre` in the CLI */
@AtlasDsl
public interface D2DagreSpec : PropertiesSpec {
  /** Number of pixels that separate nodes horizontally. (default 60) */
  public var nodeSep: Int?

  /** Number of pixels that separate edges horizontally. (default 20) */
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
   */
  public var seeds: List<Long>?
}
