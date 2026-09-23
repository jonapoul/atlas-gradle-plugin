@file:Suppress("unused", "TooManyFunctions") // public API

package atlas.core

import atlas.d2.FillPattern
import atlas.d2.Font
import atlas.d2.Shape as D2Shape
import atlas.d2.TextTransform
import atlas.graphviz.ImagePos
import atlas.graphviz.NodeStyle
import atlas.graphviz.Shape as GraphvizShape
import org.gradle.api.Action
import org.gradle.api.NamedDomainObjectContainer
import org.gradle.api.NamedDomainObjectProvider
import org.gradle.api.provider.Property
import org.gradle.api.provider.SetProperty
import org.gradle.internal.impldep.org.intellij.lang.annotations.Language

/**
 * Represents a category of project that you can use to match against those in your project. You can
 * use some of the built-in example types like below:
 * ```kotlin
 * atlas {
 *   projectTypes {
 *     androidApp()
 *     kotlinMultiplatform()
 *     androidLibrary()
 *     kotlinJvm()
 *     java()
 *     other()
 *
 *     // or use useDefaults() to include all of the above, in this order
 *   }
 * }
 * ```
 *
 * or create custom types like:
 * ```kotlin
 * atlas {
 *   projectTypes {
 *     // reference some built-in types
 *     androidApp()
 *     java {
 *       // custom overrides
 *       color = "black"
 *     }
 *
 *     // plus some manually-defined ones
 *     hasPluginId(name = "UI", color = "#ABC123", pluginId = "org.jetbrains.kotlin.plugin.compose")
 *     pathMatches(name = "Data", color = "#ABCDEF", pathMatches = ".*data$")
 *     pathContains(name = "Domain", pathContains = "domain") {
 *       // plus any style properties from the frameworks you've configured
 *     }
 *   }
 * }
 * ```
 *
 * Remember that priority is given in descending order, so in the example above the UI project type
 * will be checked before the data or domain types.
 *
 * Set one of [ProjectTypeSpec.pathContains], [ProjectTypeSpec.pathMatches] or
 * [ProjectTypeSpec.hasPluginId]. If none are set, Atlas logs a warning and ignores the type. If
 * more than one is set, only the first of those three is checked.
 */
@AtlasDsl
public interface NamedProjectTypeContainer : NamedDomainObjectContainer<ProjectTypeSpec> {
  public fun hasPluginId(
    name: String,
    pluginId: String,
    color: String? = null,
    action: Action<ProjectTypeSpec>? = null,
  ): NamedDomainObjectProvider<ProjectTypeSpec> =
    register(name) { type ->
      type.color.convention(color)
      type.hasPluginId.convention(pluginId)
      action?.execute(type)
    }

  public fun pathMatches(
    name: String,
    @Language("RegExp") pathMatches: String,
    options: Set<RegexOption> = emptySet(),
    color: String? = null,
    action: Action<ProjectTypeSpec>? = null,
  ): NamedDomainObjectProvider<ProjectTypeSpec> =
    register(name) { type ->
      type.color.convention(color)
      type.pathMatches.convention(pathMatches)
      type.regexOptions.convention(options)
      action?.execute(type)
    }

  public fun pathContains(
    name: String,
    pathContains: String,
    color: String? = null,
    action: Action<ProjectTypeSpec>? = null,
  ): NamedDomainObjectProvider<ProjectTypeSpec> =
    register(name) { type ->
      type.color.convention(color)
      type.pathContains.convention(pathContains)
      action?.execute(type)
    }
}

/**
 * A category of project, plus how its node should be drawn.
 *
 * The matchers ([pathContains], [pathMatches], [hasPluginId]) and [color] apply to every framework.
 * The style properties below are grouped by which framework reads them - you can set any of them
 * whether or not that framework is configured, but Atlas will warn you about any which no
 * configured framework will read. See [StyleSpec] for more on that, and for [StyleSpec.put] to set
 * attributes which don't have a property here yet.
 */
@AtlasDsl
public interface ProjectTypeSpec : StyleSpec {
  /** Required - this will be shown on your generated legend files. */
  public val name: String

  /**
   * Optional. Must be a valid CSS color string. Used as the node's fill color by every framework,
   * unless overridden by the more specific [fill].
   */
  public val color: Property<String>

  /**
   * Checks against the path string of your project, e.g. ":path:to:my:project". This is
   * case-sensitive.
   */
  public val pathContains: Property<String>

  /**
   * Similar to [pathContains] but more flexible with [Regex] pattern checking instead of straight
   * string comparison. The pattern has to match the whole path.
   */
  public val pathMatches: Property<String>

  /**
   * Options to use when matching [pathMatches]. Defaults to empty set, which is case-sensitive
   * matching. Unused unless [pathMatches] is set.
   */
  public val regexOptions: SetProperty<RegexOption>

  /** Checks whether the given plugin ID string has been applied to your project. */
  public val hasPluginId: Property<String>

  // -----------------------------------------------------------------------------------------
  // Read by every framework
  // -----------------------------------------------------------------------------------------

  /** The node's background color. Overrides [color]. */
  public var fill: String?

  /** The node's border color. */
  public var stroke: String?

  /** The node's border width. */
  public var strokeWidth: String?

  /** The color of the node's label text. */
  public var fontColor: String?

  /** The size of the node's label text. */
  public var fontSize: String?

  // -----------------------------------------------------------------------------------------
  // D2 and Mermaid
  // -----------------------------------------------------------------------------------------

  /** How see-through the node is, between 0 and 1. */
  public var opacity: Float?

  // -----------------------------------------------------------------------------------------
  // D2 only - see [the D2 docs](https://d2lang.com/tour/style)
  // -----------------------------------------------------------------------------------------

  /** Animates the node's border. Only shows in animatable file formats, i.e. SVG and GIF. */
  public var animated: Boolean?

  /** Draws the node's label text in bold. */
  public var bold: Boolean?

  /** How rounded the node's corners are. */
  public var borderRadius: Int?

  /** Only applicable to [D2Shape.Rectangle] and [D2Shape.Oval]. */
  public var doubleBorder: Boolean?

  /** A texture drawn over the node's background. */
  public var fillPattern: FillPattern?

  /** The font of the node's label text. */
  public var font: Font?

  /** Draws the node's label text in italics. */
  public var italic: Boolean?

  /** Draws the node as a stack of several shapes, as if there are more than one of it. */
  public var multiple: Boolean?

  /** Only applicable to [D2Shape.Rectangle] and [D2Shape.Square]. */
  public var render3D: Boolean?

  /** Draws a drop shadow under the node. */
  public var shadow: Boolean?

  /** The shape of the node in D2 charts. See [graphvizShape] for the Graphviz equivalent. */
  public var d2Shape: D2Shape?

  /** Draws the node's border as a dashed line, with this as the gap between dashes. */
  public var strokeDash: Int?

  /** Changes the case of the node's label text. */
  public var textTransform: TextTransform?

  /** Underlines the node's label text. */
  public var underline: Boolean?

  // -----------------------------------------------------------------------------------------
  // Mermaid only - see
  // [the Mermaid docs](https://mermaid.js.org/syntax/flowchart.html#styling-a-node)
  // -----------------------------------------------------------------------------------------

  /** An array of integers, like `"5 5"`. */
  public var strokeDashArray: String?

  // -----------------------------------------------------------------------------------------
  // Graphviz only - see [the Graphviz docs](https://graphviz.org/docs/nodes/). Atlas won't
  // validate any of these, it just passes them through and lets Graphviz complain.
  // -----------------------------------------------------------------------------------------

  /** The shape of the node in Graphviz charts. See [d2Shape] for the D2 equivalent. */
  public var graphvizShape: GraphvizShape?

  /**
   * [https://graphviz.org/docs/attrs/colorscheme/](https://graphviz.org/docs/attrs/colorscheme/)
   */
  public var colorScheme: String?

  /** [https://graphviz.org/docs/attrs/comment/](https://graphviz.org/docs/attrs/comment/) */
  public var comment: String?

  /** [https://graphviz.org/docs/attrs/distortion/](https://graphviz.org/docs/attrs/distortion/) */
  public var distortion: String?

  /** [https://graphviz.org/docs/attrs/fixedsize/](https://graphviz.org/docs/attrs/fixedsize/) */
  public var fixedSize: String?

  /** [https://graphviz.org/docs/attrs/fontname/](https://graphviz.org/docs/attrs/fontname/) */
  public var fontName: String?

  /**
   * [https://graphviz.org/docs/attrs/gradientangle/](https://graphviz.org/docs/attrs/gradientangle/)
   */
  public var gradientAngle: Int?

  /** [https://graphviz.org/docs/attrs/group/](https://graphviz.org/docs/attrs/group/) */
  public var group: String?

  /** [https://graphviz.org/docs/attrs/height/](https://graphviz.org/docs/attrs/height/) */
  public var height: Number?

  /** [https://graphviz.org/docs/attrs/href/](https://graphviz.org/docs/attrs/href/) */
  public var href: String?

  /** [https://graphviz.org/docs/attrs/id/](https://graphviz.org/docs/attrs/id/) */
  public var id: String?

  /** [https://graphviz.org/docs/attrs/image/](https://graphviz.org/docs/attrs/image/) */
  public var image: String?

  /** [https://graphviz.org/docs/attrs/imagepos/](https://graphviz.org/docs/attrs/imagepos/) */
  public var imagePos: ImagePos?

  /** [https://graphviz.org/docs/attrs/imagescale/](https://graphviz.org/docs/attrs/imagescale/) */
  public var imageScale: String?

  /** [https://graphviz.org/docs/attrs/label/](https://graphviz.org/docs/attrs/label/) */
  public var label: String?

  /** [https://graphviz.org/docs/attrs/labelloc/](https://graphviz.org/docs/attrs/labelloc/) */
  public var labelLoc: String?

  /** [https://graphviz.org/docs/attrs/layer/](https://graphviz.org/docs/attrs/layer/) */
  public var layer: String?

  /** [https://graphviz.org/docs/attrs/margin/](https://graphviz.org/docs/attrs/margin/) */
  public var margin: String?

  /** [https://graphviz.org/docs/attrs/nojustify/](https://graphviz.org/docs/attrs/nojustify/) */
  public var noJustify: Boolean?

  /** [https://graphviz.org/docs/attrs/ordering/](https://graphviz.org/docs/attrs/ordering/) */
  public var ordering: String?

  /**
   * [https://graphviz.org/docs/attrs/orientation/](https://graphviz.org/docs/attrs/orientation/)
   */
  public var orientation: Number?

  /**
   * [https://graphviz.org/docs/attrs/peripheries/](https://graphviz.org/docs/attrs/peripheries/)
   */
  public var peripheries: Int?

  /** [https://graphviz.org/docs/attrs/pin/](https://graphviz.org/docs/attrs/pin/) */
  public var pin: Boolean?

  /** [https://graphviz.org/docs/attrs/pos/](https://graphviz.org/docs/attrs/pos/) */
  public var pos: String?

  /** [https://graphviz.org/docs/attrs/rects/](https://graphviz.org/docs/attrs/rects/) */
  public var rects: String?

  /** [https://graphviz.org/docs/attrs/regular/](https://graphviz.org/docs/attrs/regular/) */
  public var regular: Boolean?

  /** [https://graphviz.org/docs/attrs/root/](https://graphviz.org/docs/attrs/root/) */
  public var root: String?

  /**
   * [https://graphviz.org/docs/attrs/samplepoints/](https://graphviz.org/docs/attrs/samplepoints/)
   */
  public var samplePoints: Int?

  /** [https://graphviz.org/docs/attrs/shapefile/](https://graphviz.org/docs/attrs/shapefile/) */
  public var shapeFile: String?

  /** [https://graphviz.org/docs/attrs/showboxes/](https://graphviz.org/docs/attrs/showboxes/) */
  public var showBoxes: Int?

  /** [https://graphviz.org/docs/attrs/sides/](https://graphviz.org/docs/attrs/sides/) */
  public var sides: Int?

  /** [https://graphviz.org/docs/attrs/skew/](https://graphviz.org/docs/attrs/skew/) */
  public var skew: Number?

  /** [https://graphviz.org/docs/attrs/sortv/](https://graphviz.org/docs/attrs/sortv/) */
  public var sortv: Int?

  /** [https://graphviz.org/docs/attrs/style/](https://graphviz.org/docs/attrs/style/) */
  public var style: NodeStyle?

  /** [https://graphviz.org/docs/attrs/target/](https://graphviz.org/docs/attrs/target/) */
  public var target: String?

  /** [https://graphviz.org/docs/attrs/tooltip/](https://graphviz.org/docs/attrs/tooltip/) */
  public var tooltip: String?

  /** [https://graphviz.org/docs/attrs/URL/](https://graphviz.org/docs/attrs/URL/) */
  public var url: String?

  /** [https://graphviz.org/docs/attrs/vertices/](https://graphviz.org/docs/attrs/vertices/) */
  public var vertices: String?

  /** [https://graphviz.org/docs/attrs/width/](https://graphviz.org/docs/attrs/width/) */
  public var width: Number?

  /** [https://graphviz.org/docs/attrs/xlabel/](https://graphviz.org/docs/attrs/xlabel/) */
  public var xlabel: String?

  /** [https://graphviz.org/docs/attrs/xlp/](https://graphviz.org/docs/attrs/xlp/) */
  public var xlp: String?

  /** [https://graphviz.org/docs/attrs/z/](https://graphviz.org/docs/attrs/z/) */
  public var z: Number?
}
