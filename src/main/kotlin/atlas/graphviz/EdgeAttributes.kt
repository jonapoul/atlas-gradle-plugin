@file:Suppress("SpellCheckingInspection")

package atlas.graphviz

import atlas.core.AtlasDsl
import atlas.core.PropertiesSpec

/**
 * These attributes will be applied by default to all project edges (AKA link lines), unless
 * overridden by that link's [atlas.core.LinkTypeSpec].
 *
 * See [the Graphviz docs](https://graphviz.org/docs/edges/) for any restrictions, this interface
 * will just pass-through the attribute and let Graphviz handle validation.
 *
 * [linkColor] and [linkStyle] have been renamed from "color" and "style" to avoid clashes with
 * [atlas.core.LinkTypeSpec] attributes.
 */
@AtlasDsl
public interface EdgeAttributes : PropertiesSpec {
  /**
   * [https://graphviz.org/docs/attrs/arrowhead/](https://graphviz.org/docs/attrs/arrowhead/)
   *
   * Also controlled by the `atlas.graphviz.edge.arrowHead` Gradle property.
   */
  public var arrowHead: ArrowType?

  /**
   * [https://graphviz.org/docs/attrs/arrowsize/](https://graphviz.org/docs/attrs/arrowsize/)
   *
   * Also controlled by the `atlas.graphviz.edge.arrowSize` Gradle property.
   */
  public var arrowSize: Number?

  /**
   * [https://graphviz.org/docs/attrs/arrowtail/](https://graphviz.org/docs/attrs/arrowtail/)
   *
   * Also controlled by the `atlas.graphviz.edge.arrowTail` Gradle property.
   */
  public var arrowTail: ArrowType?

  /**
   * [https://graphviz.org/docs/attrs/color/](https://graphviz.org/docs/attrs/color/)
   *
   * Also controlled by the `atlas.graphviz.edge.linkColor` Gradle property.
   */
  public var linkColor: String?

  /**
   * [https://graphviz.org/docs/attrs/colorscheme/](https://graphviz.org/docs/attrs/colorscheme/)
   *
   * Also controlled by the `atlas.graphviz.edge.colorScheme` Gradle property.
   */
  public var colorScheme: String?

  /**
   * [https://graphviz.org/docs/attrs/comment/](https://graphviz.org/docs/attrs/comment/)
   *
   * Also controlled by the `atlas.graphviz.edge.comment` Gradle property.
   */
  public var comment: String?

  /**
   * [https://graphviz.org/docs/attrs/constraint/](https://graphviz.org/docs/attrs/constraint/)
   *
   * Also controlled by the `atlas.graphviz.edge.constraint` Gradle property.
   */
  public var constraint: Boolean?

  /**
   * [https://graphviz.org/docs/attrs/decorate/](https://graphviz.org/docs/attrs/decorate/)
   *
   * Also controlled by the `atlas.graphviz.edge.decorate` Gradle property.
   */
  public var decorate: Boolean?

  /**
   * [https://graphviz.org/docs/attrs/dir/](https://graphviz.org/docs/attrs/dir/)
   *
   * Also controlled by the `atlas.graphviz.edge.dir` Gradle property.
   */
  public var dir: Dir?

  /**
   * [https://graphviz.org/docs/attrs/edgehref/](https://graphviz.org/docs/attrs/edgehref/)
   *
   * Also controlled by the `atlas.graphviz.edge.edgeHref` Gradle property.
   */
  public var edgeHref: String?

  /**
   * [https://graphviz.org/docs/attrs/edgetarget/](https://graphviz.org/docs/attrs/edgetarget/)
   *
   * Also controlled by the `atlas.graphviz.edge.edgeTarget` Gradle property.
   */
  public var edgeTarget: String?

  /**
   * [https://graphviz.org/docs/attrs/edgetooltip/](https://graphviz.org/docs/attrs/edgetooltip/)
   *
   * Also controlled by the `atlas.graphviz.edge.edgeTooltip` Gradle property.
   */
  public var edgeTooltip: String?

  /**
   * [https://graphviz.org/docs/attrs/edgeURL/](https://graphviz.org/docs/attrs/edgeURL/)
   *
   * Also controlled by the `atlas.graphviz.edge.edgeUrl` Gradle property.
   */
  public var edgeUrl: String?

  /**
   * [https://graphviz.org/docs/attrs/fillcolor/](https://graphviz.org/docs/attrs/fillcolor/)
   *
   * Also controlled by the `atlas.graphviz.edge.fillColor` Gradle property.
   */
  public var fillColor: String?

  /**
   * [https://graphviz.org/docs/attrs/fontcolor/](https://graphviz.org/docs/attrs/fontcolor/)
   *
   * Also controlled by the `atlas.graphviz.edge.fontColor` Gradle property.
   */
  public var fontColor: String?

  /**
   * [https://graphviz.org/docs/attrs/fontname/](https://graphviz.org/docs/attrs/fontname/)
   *
   * Also controlled by the `atlas.graphviz.edge.fontName` Gradle property.
   */
  public var fontName: String?

  /**
   * [https://graphviz.org/docs/attrs/fontsize/](https://graphviz.org/docs/attrs/fontsize/)
   *
   * Also controlled by the `atlas.graphviz.edge.fontSize` Gradle property.
   */
  public var fontSize: String?

  /**
   * [https://graphviz.org/docs/attrs/head_lp/](https://graphviz.org/docs/attrs/head_lp/)
   *
   * Also controlled by the `atlas.graphviz.edge.headLp` Gradle property.
   */
  public var headLp: String?

  /**
   * [https://graphviz.org/docs/attrs/headclip/](https://graphviz.org/docs/attrs/headclip/)
   *
   * Also controlled by the `atlas.graphviz.edge.headClip` Gradle property.
   */
  public var headClip: Boolean?

  /**
   * [https://graphviz.org/docs/attrs/headhref/](https://graphviz.org/docs/attrs/headhref/)
   *
   * Also controlled by the `atlas.graphviz.edge.headHref` Gradle property.
   */
  public var headHref: String?

  /**
   * [https://graphviz.org/docs/attrs/headlabel/](https://graphviz.org/docs/attrs/headlabel/)
   *
   * Also controlled by the `atlas.graphviz.edge.headLabel` Gradle property.
   */
  public var headLabel: String?

  /**
   * [https://graphviz.org/docs/attrs/headport/](https://graphviz.org/docs/attrs/headport/)
   *
   * Also controlled by the `atlas.graphviz.edge.headPort` Gradle property.
   */
  public var headPort: String?

  /**
   * [https://graphviz.org/docs/attrs/headtarget/](https://graphviz.org/docs/attrs/headtarget/)
   *
   * Also controlled by the `atlas.graphviz.edge.headTarget` Gradle property.
   */
  public var headTarget: String?

  /**
   * [https://graphviz.org/docs/attrs/headtooltip/](https://graphviz.org/docs/attrs/headtooltip/)
   *
   * Also controlled by the `atlas.graphviz.edge.headTooltip` Gradle property.
   */
  public var headTooltip: String?

  /**
   * [https://graphviz.org/docs/attrs/headURL/](https://graphviz.org/docs/attrs/headURL/)
   *
   * Also controlled by the `atlas.graphviz.edge.headUrl` Gradle property.
   */
  public var headUrl: String?

  /**
   * [https://graphviz.org/docs/attrs/href/](https://graphviz.org/docs/attrs/href/)
   *
   * Also controlled by the `atlas.graphviz.edge.href` Gradle property.
   */
  public var href: String?

  /**
   * [https://graphviz.org/docs/attrs/id/](https://graphviz.org/docs/attrs/id/)
   *
   * Also controlled by the `atlas.graphviz.edge.id` Gradle property.
   */
  public var id: String?

  /**
   * [https://graphviz.org/docs/attrs/label/](https://graphviz.org/docs/attrs/label/)
   *
   * Also controlled by the `atlas.graphviz.edge.label` Gradle property.
   */
  public var label: String?

  /**
   * [https://graphviz.org/docs/attrs/labelangle/](https://graphviz.org/docs/attrs/labelangle/)
   *
   * Also controlled by the `atlas.graphviz.edge.labelAngle` Gradle property.
   */
  public var labelAngle: Number?

  /**
   * [https://graphviz.org/docs/attrs/labeldistance/](https://graphviz.org/docs/attrs/labeldistance/)
   *
   * Also controlled by the `atlas.graphviz.edge.labelDistance` Gradle property.
   */
  public var labelDistance: Number?

  /**
   * [https://graphviz.org/docs/attrs/labelfloat/](https://graphviz.org/docs/attrs/labelfloat/)
   *
   * Also controlled by the `atlas.graphviz.edge.labelFloat` Gradle property.
   */
  public var labelFloat: Boolean?

  /**
   * [https://graphviz.org/docs/attrs/labelfontcolor/](https://graphviz.org/docs/attrs/labelfontcolor/)
   *
   * Also controlled by the `atlas.graphviz.edge.labelFontColor` Gradle property.
   */
  public var labelFontColor: String?

  /**
   * [https://graphviz.org/docs/attrs/labelfontname/](https://graphviz.org/docs/attrs/labelfontname/)
   *
   * Also controlled by the `atlas.graphviz.edge.labelFontName` Gradle property.
   */
  public var labelFontName: String?

  /**
   * [https://graphviz.org/docs/attrs/labelfontsize/](https://graphviz.org/docs/attrs/labelfontsize/)
   *
   * Also controlled by the `atlas.graphviz.edge.labelFontSize` Gradle property.
   */
  public var labelFontSize: String?

  /**
   * [https://graphviz.org/docs/attrs/labelhref/](https://graphviz.org/docs/attrs/labelhref/)
   *
   * Also controlled by the `atlas.graphviz.edge.labelHref` Gradle property.
   */
  public var labelHref: String?

  /**
   * [https://graphviz.org/docs/attrs/labeltarget/](https://graphviz.org/docs/attrs/labeltarget/)
   *
   * Also controlled by the `atlas.graphviz.edge.labelTarget` Gradle property.
   */
  public var labelTarget: String?

  /**
   * [https://graphviz.org/docs/attrs/labeltooltip/](https://graphviz.org/docs/attrs/labeltooltip/)
   *
   * Also controlled by the `atlas.graphviz.edge.labelTooltip` Gradle property.
   */
  public var labelTooltip: String?

  /**
   * [https://graphviz.org/docs/attrs/labelurl/](https://graphviz.org/docs/attrs/labelurl/)
   *
   * Also controlled by the `atlas.graphviz.edge.labelUrl` Gradle property.
   */
  public var labelUrl: String?

  /**
   * [https://graphviz.org/docs/attrs/layer/](https://graphviz.org/docs/attrs/layer/)
   *
   * Also controlled by the `atlas.graphviz.edge.layer` Gradle property.
   */
  public var layer: String?

  /**
   * [https://graphviz.org/docs/attrs/len/](https://graphviz.org/docs/attrs/len/)
   *
   * Also controlled by the `atlas.graphviz.edge.len` Gradle property.
   */
  public var len: Number?

  /**
   * [https://graphviz.org/docs/attrs/lhead/](https://graphviz.org/docs/attrs/lhead/)
   *
   * Also controlled by the `atlas.graphviz.edge.lhead` Gradle property.
   */
  public var lhead: String?

  /**
   * [https://graphviz.org/docs/attrs/lp/](https://graphviz.org/docs/attrs/lp/)
   *
   * Also controlled by the `atlas.graphviz.edge.lp` Gradle property.
   */
  public var lp: String?

  /**
   * [https://graphviz.org/docs/attrs/ltail/](https://graphviz.org/docs/attrs/ltail/)
   *
   * Also controlled by the `atlas.graphviz.edge.ltail` Gradle property.
   */
  public var ltail: String?

  /**
   * [https://graphviz.org/docs/attrs/minlen/](https://graphviz.org/docs/attrs/minlen/)
   *
   * Also controlled by the `atlas.graphviz.edge.minLen` Gradle property.
   */
  public var minLen: Int?

  /**
   * [https://graphviz.org/docs/attrs/nojustify/](https://graphviz.org/docs/attrs/nojustify/)
   *
   * Also controlled by the `atlas.graphviz.edge.noJustify` Gradle property.
   */
  public var noJustify: Boolean?

  /**
   * [https://graphviz.org/docs/attrs/penwidth/](https://graphviz.org/docs/attrs/penwidth/)
   *
   * Also controlled by the `atlas.graphviz.edge.penWidth` Gradle property.
   */
  public var penWidth: Number?

  /**
   * [https://graphviz.org/docs/attrs/pos/](https://graphviz.org/docs/attrs/pos/)
   *
   * Also controlled by the `atlas.graphviz.edge.pos` Gradle property.
   */
  public var pos: String?

  /**
   * [https://graphviz.org/docs/attrs/samehead/](https://graphviz.org/docs/attrs/samehead/)
   *
   * Also controlled by the `atlas.graphviz.edge.sameHead` Gradle property.
   */
  public var sameHead: String?

  /**
   * [https://graphviz.org/docs/attrs/sametail/](https://graphviz.org/docs/attrs/sametail/)
   *
   * Also controlled by the `atlas.graphviz.edge.sameTail` Gradle property.
   */
  public var sameTail: String?

  /**
   * [https://graphviz.org/docs/attrs/showboxes/](https://graphviz.org/docs/attrs/showboxes/)
   *
   * Also controlled by the `atlas.graphviz.edge.showBoxes` Gradle property.
   */
  public var showBoxes: Int?

  /**
   * [https://graphviz.org/docs/attrs/style/](https://graphviz.org/docs/attrs/style/)
   *
   * Also controlled by the `atlas.graphviz.edge.linkStyle` Gradle property.
   */
  public var linkStyle: EdgeStyle?

  /**
   * [https://graphviz.org/docs/attrs/tail_lp/](https://graphviz.org/docs/attrs/tail_lp/)
   *
   * Also controlled by the `atlas.graphviz.edge.tailLp` Gradle property.
   */
  public var tailLp: String?

  /**
   * [https://graphviz.org/docs/attrs/tailclip/](https://graphviz.org/docs/attrs/tailclip/)
   *
   * Also controlled by the `atlas.graphviz.edge.tailClip` Gradle property.
   */
  public var tailClip: Boolean?

  /**
   * [https://graphviz.org/docs/attrs/tailhref/](https://graphviz.org/docs/attrs/tailhref/)
   *
   * Also controlled by the `atlas.graphviz.edge.tailHref` Gradle property.
   */
  public var tailHref: String?

  /**
   * [https://graphviz.org/docs/attrs/taillabel/](https://graphviz.org/docs/attrs/taillabel/)
   *
   * Also controlled by the `atlas.graphviz.edge.tailLabel` Gradle property.
   */
  public var tailLabel: String?

  /**
   * [https://graphviz.org/docs/attrs/tailport/](https://graphviz.org/docs/attrs/tailport/)
   *
   * Also controlled by the `atlas.graphviz.edge.tailPort` Gradle property.
   */
  public var tailPort: String?

  /**
   * [https://graphviz.org/docs/attrs/tailtarget/](https://graphviz.org/docs/attrs/tailtarget/)
   *
   * Also controlled by the `atlas.graphviz.edge.tailTarget` Gradle property.
   */
  public var tailTarget: String?

  /**
   * [https://graphviz.org/docs/attrs/tailtooltip/](https://graphviz.org/docs/attrs/tailtooltip/)
   *
   * Also controlled by the `atlas.graphviz.edge.tailTooltip` Gradle property.
   */
  public var tailTooltip: String?

  /**
   * [https://graphviz.org/docs/attrs/tailURL/](https://graphviz.org/docs/attrs/tailURL/)
   *
   * Also controlled by the `atlas.graphviz.edge.tailUrl` Gradle property.
   */
  public var tailUrl: String?

  /**
   * [https://graphviz.org/docs/attrs/target/](https://graphviz.org/docs/attrs/target/)
   *
   * Also controlled by the `atlas.graphviz.edge.target` Gradle property.
   */
  public var target: String?

  /**
   * [https://graphviz.org/docs/attrs/tooltip/](https://graphviz.org/docs/attrs/tooltip/)
   *
   * Also controlled by the `atlas.graphviz.edge.tooltip` Gradle property.
   */
  public var tooltip: String?

  /**
   * [https://graphviz.org/docs/attrs/url/](https://graphviz.org/docs/attrs/url/)
   *
   * Also controlled by the `atlas.graphviz.edge.url` Gradle property.
   */
  public var url: String?

  /**
   * [https://graphviz.org/docs/attrs/weight/](https://graphviz.org/docs/attrs/weight/)
   *
   * Also controlled by the `atlas.graphviz.edge.weight` Gradle property.
   */
  public var weight: Number?

  /**
   * [https://graphviz.org/docs/attrs/xlabel/](https://graphviz.org/docs/attrs/xlabel/)
   *
   * Also controlled by the `atlas.graphviz.edge.xLabel` Gradle property.
   */
  public var xLabel: String?

  /**
   * [https://graphviz.org/docs/attrs/xlp/](https://graphviz.org/docs/attrs/xlp/)
   *
   * Also controlled by the `atlas.graphviz.edge.xlp` Gradle property.
   */
  public var xlp: String?
}
