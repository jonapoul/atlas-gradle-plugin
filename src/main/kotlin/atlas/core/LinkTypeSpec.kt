@file:Suppress("unused", "TooManyFunctions") // public API

package atlas.core

import atlas.d2.Font
import atlas.d2.TextTransform
import atlas.graphviz.ArrowType
import atlas.graphviz.Dir
import java.io.Serializable as JSerializable
import kotlinx.serialization.Serializable as KSerializable
import org.gradle.api.Action
import org.gradle.api.NamedDomainObjectContainer
import org.gradle.api.NamedDomainObjectProvider
import org.gradle.api.provider.Property

/**
 * Used to configure expected link "types" between your projects. The majority of the time, these
 * are only ever going to be [NamedLinkTypeContainer.api] or
 * [NamedLinkTypeContainer.implementation], hence those being listed for easier access. Configure
 * like:
 * ```kotlin
 * atlas {
 *   linkTypes {
 *     api(color = "green")
 *     implementation(color = "#5555FF")
 *     "compileOnly"(style = LinkStyle.Dotted, displayName = "Compile Only")
 *     "^withRegex.*"(style = LinkStyle.Dashed, displayName = "Supports case-insensitive regex")
 *   }
 * }
 * ```
 *
 * You can create new types with the string invoke operator as above (similar to one used in Gradle
 * dependencies sometimes), or just call one of the [register] overloads.
 *
 * Added entries are checked in priority order, so a configuration of `apiImplementationCompileOnly`
 * in the example above would match `api` but not reach `implementation` or `compileOnly`.
 */
@AtlasDsl
public interface NamedLinkTypeContainer : NamedDomainObjectContainer<LinkTypeSpec> {
  public fun register(
    configuration: String,
    style: LinkStyle? = null,
    color: String? = null,
    displayName: String = configuration,
    action: Action<LinkTypeSpec>? = null,
  ): NamedDomainObjectProvider<LinkTypeSpec> =
    register(displayName) { spec ->
      spec.configuration.set(configuration)
      spec.style.set(style)
      spec.color.set(color)
      action?.execute(spec)
    }

  public fun api(
    style: LinkStyle? = null,
    color: String? = null,
    displayName: String = "api",
    action: Action<LinkTypeSpec>? = null,
  ): NamedDomainObjectProvider<LinkTypeSpec> =
    register(
      configuration = ".*?api",
      style = style,
      color = color,
      displayName = displayName,
      action = action,
    )

  public fun implementation(
    style: LinkStyle? = null,
    color: String? = null,
    displayName: String = "implementation",
    action: Action<LinkTypeSpec>? = null,
  ): NamedDomainObjectProvider<LinkTypeSpec> =
    register(
      configuration = ".*?implementation",
      style = style,
      color = color,
      displayName = displayName,
      action = action,
    )

  public operator fun String.invoke(
    style: LinkStyle? = null,
    color: String? = null,
    displayName: String = this,
    action: Action<LinkTypeSpec>? = null,
  ): NamedDomainObjectProvider<LinkTypeSpec> =
    register(
      configuration = this,
      style = style,
      color = color,
      displayName = displayName,
      action = action,
    )
}

/**
 * A category of link between two projects, plus how it should be drawn.
 *
 * [configuration], [style] and [color] apply to every framework. The style properties below are
 * grouped by which framework reads them - you can set any of them whether or not that framework is
 * configured, but Atlas will warn you about any which no configured framework will read. See
 * [StyleSpec] for more on that, and for [StyleSpec.put] to set attributes which don't have a
 * property here yet.
 */
@AtlasDsl
public interface LinkTypeSpec : StyleSpec {
  /** Shown on your generated legend files. */
  public val name: String

  /** Regex matched against the Gradle configuration name which created the link, e.g. "api". */
  public val configuration: Property<String>

  /** How the line is drawn. Defaults to [LinkStyle.Solid]. */
  public val style: Property<LinkStyle>

  /** The color of the line. Overridden by the more specific [stroke]. */
  public val color: Property<String>

  // -----------------------------------------------------------------------------------------
  // Read by every framework
  // -----------------------------------------------------------------------------------------

  /** The color of the line. Overrides [color]. */
  public var stroke: String?

  /** The width of the line. */
  public var strokeWidth: String?

  /** The color of the link's label text, if [AtlasExtension.displayLinkLabels] is enabled. */
  public var fontColor: String?

  // -----------------------------------------------------------------------------------------
  // D2 and Graphviz
  // -----------------------------------------------------------------------------------------

  /** The size of the link's label text, if [AtlasExtension.displayLinkLabels] is enabled. */
  public var fontSize: String?

  // -----------------------------------------------------------------------------------------
  // D2 and Mermaid
  // -----------------------------------------------------------------------------------------

  /** How see-through the line is, between 0 and 1. */
  public var opacity: Float?

  // -----------------------------------------------------------------------------------------
  // D2 only - see [the D2 docs](https://d2lang.com/tour/style)
  // -----------------------------------------------------------------------------------------

  /** Animates the line. Only shows in animatable file formats, i.e. SVG and GIF. */
  public var animated: Boolean?

  /** Draws the link's label text in bold. */
  public var bold: Boolean?

  /** How rounded the line's corners are, where it bends. */
  public var borderRadius: Int?

  /** The font of the link's label text. */
  public var font: Font?

  /** Draws the link's label text in italics. */
  public var italic: Boolean?

  /** Draws the line dashed, with this as the gap between dashes. */
  public var strokeDash: Int?

  /** Changes the case of the link's label text. */
  public var textTransform: TextTransform?

  /** Underlines the link's label text. */
  public var underline: Boolean?

  // -----------------------------------------------------------------------------------------
  // Mermaid only - see
  // [the Mermaid docs](https://mermaid.js.org/syntax/flowchart.html#links-between-nodes)
  // -----------------------------------------------------------------------------------------

  /** An array of integers, like `"5 5"`. */
  public var strokeDashArray: String?

  // -----------------------------------------------------------------------------------------
  // Graphviz only - see [the Graphviz docs](https://graphviz.org/docs/edges/). Atlas won't
  // validate any of these, it just passes them through and lets Graphviz complain.
  // -----------------------------------------------------------------------------------------

  /** [https://graphviz.org/docs/attrs/arrowhead/](https://graphviz.org/docs/attrs/arrowhead/) */
  public var arrowHead: ArrowType?

  /** [https://graphviz.org/docs/attrs/arrowsize/](https://graphviz.org/docs/attrs/arrowsize/) */
  public var arrowSize: Number?

  /** [https://graphviz.org/docs/attrs/arrowtail/](https://graphviz.org/docs/attrs/arrowtail/) */
  public var arrowTail: ArrowType?

  /**
   * [https://graphviz.org/docs/attrs/colorscheme/](https://graphviz.org/docs/attrs/colorscheme/)
   */
  public var colorScheme: String?

  /** [https://graphviz.org/docs/attrs/comment/](https://graphviz.org/docs/attrs/comment/) */
  public var comment: String?

  /** [https://graphviz.org/docs/attrs/constraint/](https://graphviz.org/docs/attrs/constraint/) */
  public var constraint: Boolean?

  /** [https://graphviz.org/docs/attrs/decorate/](https://graphviz.org/docs/attrs/decorate/) */
  public var decorate: Boolean?

  /** [https://graphviz.org/docs/attrs/dir/](https://graphviz.org/docs/attrs/dir/) */
  public var dir: Dir?

  /** [https://graphviz.org/docs/attrs/edgehref/](https://graphviz.org/docs/attrs/edgehref/) */
  public var edgeHref: String?

  /** [https://graphviz.org/docs/attrs/edgetarget/](https://graphviz.org/docs/attrs/edgetarget/) */
  public var edgeTarget: String?

  /**
   * [https://graphviz.org/docs/attrs/edgetooltip/](https://graphviz.org/docs/attrs/edgetooltip/)
   */
  public var edgeTooltip: String?

  /** [https://graphviz.org/docs/attrs/edgeURL/](https://graphviz.org/docs/attrs/edgeURL/) */
  public var edgeUrl: String?

  /** [https://graphviz.org/docs/attrs/fillcolor/](https://graphviz.org/docs/attrs/fillcolor/) */
  public var fillColor: String?

  /** [https://graphviz.org/docs/attrs/fontname/](https://graphviz.org/docs/attrs/fontname/) */
  public var fontName: String?

  /** [https://graphviz.org/docs/attrs/head_lp/](https://graphviz.org/docs/attrs/head_lp/) */
  public var headLp: String?

  /** [https://graphviz.org/docs/attrs/headclip/](https://graphviz.org/docs/attrs/headclip/) */
  public var headClip: Boolean?

  /** [https://graphviz.org/docs/attrs/headhref/](https://graphviz.org/docs/attrs/headhref/) */
  public var headHref: String?

  /** [https://graphviz.org/docs/attrs/headlabel/](https://graphviz.org/docs/attrs/headlabel/) */
  public var headLabel: String?

  /** [https://graphviz.org/docs/attrs/headport/](https://graphviz.org/docs/attrs/headport/) */
  public var headPort: String?

  /** [https://graphviz.org/docs/attrs/headtarget/](https://graphviz.org/docs/attrs/headtarget/) */
  public var headTarget: String?

  /**
   * [https://graphviz.org/docs/attrs/headtooltip/](https://graphviz.org/docs/attrs/headtooltip/)
   */
  public var headTooltip: String?

  /** [https://graphviz.org/docs/attrs/headURL/](https://graphviz.org/docs/attrs/headURL/) */
  public var headUrl: String?

  /** [https://graphviz.org/docs/attrs/href/](https://graphviz.org/docs/attrs/href/) */
  public var href: String?

  /** [https://graphviz.org/docs/attrs/id/](https://graphviz.org/docs/attrs/id/) */
  public var id: String?

  /** [https://graphviz.org/docs/attrs/label/](https://graphviz.org/docs/attrs/label/) */
  public var label: String?

  /** [https://graphviz.org/docs/attrs/labelangle/](https://graphviz.org/docs/attrs/labelangle/) */
  public var labelAngle: Number?

  /**
   * [https://graphviz.org/docs/attrs/labeldistance/](https://graphviz.org/docs/attrs/labeldistance/)
   */
  public var labelDistance: Number?

  /** [https://graphviz.org/docs/attrs/labelfloat/](https://graphviz.org/docs/attrs/labelfloat/) */
  public var labelFloat: Boolean?

  /**
   * [https://graphviz.org/docs/attrs/labelfontcolor/](https://graphviz.org/docs/attrs/labelfontcolor/)
   */
  public var labelFontColor: String?

  /**
   * [https://graphviz.org/docs/attrs/labelfontname/](https://graphviz.org/docs/attrs/labelfontname/)
   */
  public var labelFontName: String?

  /**
   * [https://graphviz.org/docs/attrs/labelfontsize/](https://graphviz.org/docs/attrs/labelfontsize/)
   */
  public var labelFontSize: String?

  /** [https://graphviz.org/docs/attrs/labelhref/](https://graphviz.org/docs/attrs/labelhref/) */
  public var labelHref: String?

  /**
   * [https://graphviz.org/docs/attrs/labeltarget/](https://graphviz.org/docs/attrs/labeltarget/)
   */
  public var labelTarget: String?

  /**
   * [https://graphviz.org/docs/attrs/labeltooltip/](https://graphviz.org/docs/attrs/labeltooltip/)
   */
  public var labelTooltip: String?

  /** [https://graphviz.org/docs/attrs/labelurl/](https://graphviz.org/docs/attrs/labelurl/) */
  public var labelUrl: String?

  /** [https://graphviz.org/docs/attrs/layer/](https://graphviz.org/docs/attrs/layer/) */
  public var layer: String?

  /** [https://graphviz.org/docs/attrs/len/](https://graphviz.org/docs/attrs/len/) */
  public var len: Number?

  /** [https://graphviz.org/docs/attrs/lhead/](https://graphviz.org/docs/attrs/lhead/) */
  public var lhead: String?

  /** [https://graphviz.org/docs/attrs/lp/](https://graphviz.org/docs/attrs/lp/) */
  public var lp: String?

  /** [https://graphviz.org/docs/attrs/ltail/](https://graphviz.org/docs/attrs/ltail/) */
  public var ltail: String?

  /** [https://graphviz.org/docs/attrs/minlen/](https://graphviz.org/docs/attrs/minlen/) */
  public var minLen: Int?

  /** [https://graphviz.org/docs/attrs/nojustify/](https://graphviz.org/docs/attrs/nojustify/) */
  public var noJustify: Boolean?

  /** [https://graphviz.org/docs/attrs/pos/](https://graphviz.org/docs/attrs/pos/) */
  public var pos: String?

  /** [https://graphviz.org/docs/attrs/samehead/](https://graphviz.org/docs/attrs/samehead/) */
  public var sameHead: String?

  /** [https://graphviz.org/docs/attrs/sametail/](https://graphviz.org/docs/attrs/sametail/) */
  public var sameTail: String?

  /** [https://graphviz.org/docs/attrs/showboxes/](https://graphviz.org/docs/attrs/showboxes/) */
  public var showBoxes: Int?

  /** [https://graphviz.org/docs/attrs/tail_lp/](https://graphviz.org/docs/attrs/tail_lp/) */
  public var tailLp: String?

  /** [https://graphviz.org/docs/attrs/tailclip/](https://graphviz.org/docs/attrs/tailclip/) */
  public var tailClip: Boolean?

  /** [https://graphviz.org/docs/attrs/tailhref/](https://graphviz.org/docs/attrs/tailhref/) */
  public var tailHref: String?

  /** [https://graphviz.org/docs/attrs/taillabel/](https://graphviz.org/docs/attrs/taillabel/) */
  public var tailLabel: String?

  /** [https://graphviz.org/docs/attrs/tailport/](https://graphviz.org/docs/attrs/tailport/) */
  public var tailPort: String?

  /** [https://graphviz.org/docs/attrs/tailtarget/](https://graphviz.org/docs/attrs/tailtarget/) */
  public var tailTarget: String?

  /**
   * [https://graphviz.org/docs/attrs/tailtooltip/](https://graphviz.org/docs/attrs/tailtooltip/)
   */
  public var tailTooltip: String?

  /** [https://graphviz.org/docs/attrs/tailURL/](https://graphviz.org/docs/attrs/tailURL/) */
  public var tailUrl: String?

  /** [https://graphviz.org/docs/attrs/target/](https://graphviz.org/docs/attrs/target/) */
  public var target: String?

  /** [https://graphviz.org/docs/attrs/tooltip/](https://graphviz.org/docs/attrs/tooltip/) */
  public var tooltip: String?

  /** [https://graphviz.org/docs/attrs/url/](https://graphviz.org/docs/attrs/url/) */
  public var url: String?

  /** [https://graphviz.org/docs/attrs/weight/](https://graphviz.org/docs/attrs/weight/) */
  public var weight: Number?

  /** [https://graphviz.org/docs/attrs/xlabel/](https://graphviz.org/docs/attrs/xlabel/) */
  public var xLabel: String?

  /** [https://graphviz.org/docs/attrs/xlp/](https://graphviz.org/docs/attrs/xlp/) */
  public var xlp: String?
}

@KSerializable
public data class LinkType(
  public val configuration: String,
  public val style: LinkStyle? = null,
  public val color: String? = null,
  public val displayName: String = configuration,
  public val properties: Map<String, Map<String, String>> = emptyMap(),
) : JSerializable {
  /** The attributes which [framework] should apply to this link. */
  public fun properties(framework: Framework): Map<String, String> =
    properties[framework.string].orEmpty()
}
