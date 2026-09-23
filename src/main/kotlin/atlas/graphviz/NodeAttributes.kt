@file:Suppress("SpellCheckingInspection")

package atlas.graphviz

import atlas.core.AtlasDsl
import atlas.core.PropertiesSpec

/**
 * These attributes will be applied by default to all project nodes, unless overridden by that
 * node's [atlas.core.ProjectTypeSpec].
 *
 * See [the Graphviz docs](https://graphviz.org/docs/nodes/) for any restrictions, this interface
 * will just pass-through the attribute and let Graphviz handle validation.
 */
@AtlasDsl
public interface NodeAttributes : PropertiesSpec {
  /**
   * [https://graphviz.org/docs/attrs/color/](https://graphviz.org/docs/attrs/color/)
   *
   * Also controlled by the `atlas.graphviz.node.lineColor` Gradle property.
   */
  public var lineColor: String?

  /**
   * [https://graphviz.org/docs/attrs/colorscheme/](https://graphviz.org/docs/attrs/colorscheme/)
   *
   * Also controlled by the `atlas.graphviz.node.colorScheme` Gradle property.
   */
  public var colorScheme: String?

  /**
   * [https://graphviz.org/docs/attrs/comment/](https://graphviz.org/docs/attrs/comment/)
   *
   * Also controlled by the `atlas.graphviz.node.comment` Gradle property.
   */
  public var comment: String?

  /**
   * [https://graphviz.org/docs/attrs/distortion/](https://graphviz.org/docs/attrs/distortion/)
   *
   * Also controlled by the `atlas.graphviz.node.distortion` Gradle property.
   */
  public var distortion: String?

  /**
   * [https://graphviz.org/docs/attrs/fillcolor/](https://graphviz.org/docs/attrs/fillcolor/)
   *
   * Also controlled by the `atlas.graphviz.node.fillColor` Gradle property.
   */
  public var fillColor: String?

  /**
   * [https://graphviz.org/docs/attrs/fixedsize/](https://graphviz.org/docs/attrs/fixedsize/)
   *
   * Also controlled by the `atlas.graphviz.node.fixedSize` Gradle property.
   */
  public var fixedSize: String?

  /**
   * [https://graphviz.org/docs/attrs/fontcolor/](https://graphviz.org/docs/attrs/fontcolor/)
   *
   * Also controlled by the `atlas.graphviz.node.fontColor` Gradle property.
   */
  public var fontColor: String?

  /**
   * [https://graphviz.org/docs/attrs/fontname/](https://graphviz.org/docs/attrs/fontname/)
   *
   * Also controlled by the `atlas.graphviz.node.fontName` Gradle property.
   */
  public var fontName: String?

  /**
   * [https://graphviz.org/docs/attrs/fontsize/](https://graphviz.org/docs/attrs/fontsize/)
   *
   * Also controlled by the `atlas.graphviz.node.fontSize` Gradle property.
   */
  public var fontSize: String?

  /**
   * [https://graphviz.org/docs/attrs/gradientangle/](https://graphviz.org/docs/attrs/gradientangle/)
   *
   * Also controlled by the `atlas.graphviz.node.gradientAngle` Gradle property.
   */
  public var gradientAngle: Int?

  /**
   * [https://graphviz.org/docs/attrs/group/](https://graphviz.org/docs/attrs/group/)
   *
   * Also controlled by the `atlas.graphviz.node.group` Gradle property.
   */
  public var group: String?

  /**
   * [https://graphviz.org/docs/attrs/height/](https://graphviz.org/docs/attrs/height/)
   *
   * Also controlled by the `atlas.graphviz.node.height` Gradle property.
   */
  public var height: Number?

  /**
   * [https://graphviz.org/docs/attrs/href/](https://graphviz.org/docs/attrs/href/)
   *
   * Also controlled by the `atlas.graphviz.node.href` Gradle property.
   */
  public var href: String?

  /**
   * [https://graphviz.org/docs/attrs/id/](https://graphviz.org/docs/attrs/id/)
   *
   * Also controlled by the `atlas.graphviz.node.id` Gradle property.
   */
  public var id: String?

  /**
   * [https://graphviz.org/docs/attrs/image/](https://graphviz.org/docs/attrs/image/)
   *
   * Also controlled by the `atlas.graphviz.node.image` Gradle property.
   */
  public var image: String?

  /**
   * [https://graphviz.org/docs/attrs/imagepos/](https://graphviz.org/docs/attrs/imagepos/)
   *
   * Also controlled by the `atlas.graphviz.node.imagePos` Gradle property.
   */
  public var imagePos: ImagePos?

  /**
   * [https://graphviz.org/docs/attrs/imagescale/](https://graphviz.org/docs/attrs/imagescale/)
   *
   * Also controlled by the `atlas.graphviz.node.imageScale` Gradle property.
   */
  public var imageScale: String?

  /**
   * [https://graphviz.org/docs/attrs/label/](https://graphviz.org/docs/attrs/label/)
   *
   * Also controlled by the `atlas.graphviz.node.label` Gradle property.
   */
  public var label: String?

  /**
   * [https://graphviz.org/docs/attrs/labelloc/](https://graphviz.org/docs/attrs/labelloc/)
   *
   * Also controlled by the `atlas.graphviz.node.labelLoc` Gradle property.
   */
  public var labelLoc: String?

  /**
   * [https://graphviz.org/docs/attrs/layer/](https://graphviz.org/docs/attrs/layer/)
   *
   * Also controlled by the `atlas.graphviz.node.layer` Gradle property.
   */
  public var layer: String?

  /**
   * [https://graphviz.org/docs/attrs/margin/](https://graphviz.org/docs/attrs/margin/)
   *
   * Also controlled by the `atlas.graphviz.node.margin` Gradle property.
   */
  public var margin: String?

  /**
   * [https://graphviz.org/docs/attrs/nojustify/](https://graphviz.org/docs/attrs/nojustify/)
   *
   * Also controlled by the `atlas.graphviz.node.noJustify` Gradle property.
   */
  public var noJustify: Boolean?

  /**
   * [https://graphviz.org/docs/attrs/ordering/](https://graphviz.org/docs/attrs/ordering/)
   *
   * Also controlled by the `atlas.graphviz.node.ordering` Gradle property.
   */
  public var ordering: String?

  /**
   * [https://graphviz.org/docs/attrs/orientation/](https://graphviz.org/docs/attrs/orientation/)
   *
   * Also controlled by the `atlas.graphviz.node.orientation` Gradle property.
   */
  public var orientation: Number?

  /**
   * [https://graphviz.org/docs/attrs/penwidth/](https://graphviz.org/docs/attrs/penwidth/)
   *
   * Also controlled by the `atlas.graphviz.node.penWidth` Gradle property.
   */
  public var penWidth: Number?

  /**
   * [https://graphviz.org/docs/attrs/peripheries/](https://graphviz.org/docs/attrs/peripheries/)
   *
   * Also controlled by the `atlas.graphviz.node.peripheries` Gradle property.
   */
  public var peripheries: Int?

  /**
   * [https://graphviz.org/docs/attrs/pin/](https://graphviz.org/docs/attrs/pin/)
   *
   * Also controlled by the `atlas.graphviz.node.pin` Gradle property.
   */
  public var pin: Boolean?

  /**
   * [https://graphviz.org/docs/attrs/pos/](https://graphviz.org/docs/attrs/pos/)
   *
   * Also controlled by the `atlas.graphviz.node.pos` Gradle property.
   */
  public var pos: String?

  /**
   * [https://graphviz.org/docs/attrs/rects/](https://graphviz.org/docs/attrs/rects/)
   *
   * Also controlled by the `atlas.graphviz.node.rects` Gradle property.
   */
  public var rects: String?

  /**
   * [https://graphviz.org/docs/attrs/regular/](https://graphviz.org/docs/attrs/regular/)
   *
   * Also controlled by the `atlas.graphviz.node.regular` Gradle property.
   */
  public var regular: Boolean?

  /**
   * [https://graphviz.org/docs/attrs/root/](https://graphviz.org/docs/attrs/root/)
   *
   * Also controlled by the `atlas.graphviz.node.root` Gradle property.
   */
  public var root: String?

  /**
   * [https://graphviz.org/docs/attrs/samplepoints/](https://graphviz.org/docs/attrs/samplepoints/)
   *
   * Also controlled by the `atlas.graphviz.node.samplePoints` Gradle property.
   */
  public var samplePoints: Int?

  /**
   * [https://graphviz.org/docs/attrs/shape/](https://graphviz.org/docs/attrs/shape/)
   *
   * Also controlled by the `atlas.graphviz.node.shape` Gradle property.
   */
  public var shape: Shape?

  /**
   * [https://graphviz.org/docs/attrs/shapefile/](https://graphviz.org/docs/attrs/shapefile/)
   *
   * Also controlled by the `atlas.graphviz.node.shapeFile` Gradle property.
   */
  public var shapeFile: String?

  /**
   * [https://graphviz.org/docs/attrs/showboxes/](https://graphviz.org/docs/attrs/showboxes/)
   *
   * Also controlled by the `atlas.graphviz.node.showBoxes` Gradle property.
   */
  public var showBoxes: Int?

  /**
   * [https://graphviz.org/docs/attrs/sides/](https://graphviz.org/docs/attrs/sides/)
   *
   * Also controlled by the `atlas.graphviz.node.sides` Gradle property.
   */
  public var sides: Int?

  /**
   * [https://graphviz.org/docs/attrs/skew/](https://graphviz.org/docs/attrs/skew/)
   *
   * Also controlled by the `atlas.graphviz.node.skew` Gradle property.
   */
  public var skew: Number?

  /**
   * [https://graphviz.org/docs/attrs/sortv/](https://graphviz.org/docs/attrs/sortv/)
   *
   * Also controlled by the `atlas.graphviz.node.sortv` Gradle property.
   */
  public var sortv: Int?

  /**
   * [https://graphviz.org/docs/attrs/style/](https://graphviz.org/docs/attrs/style/)
   *
   * Also controlled by the `atlas.graphviz.node.style` Gradle property.
   */
  public var style: NodeStyle?

  /**
   * [https://graphviz.org/docs/attrs/target/](https://graphviz.org/docs/attrs/target/)
   *
   * Also controlled by the `atlas.graphviz.node.target` Gradle property.
   */
  public var target: String?

  /**
   * [https://graphviz.org/docs/attrs/tooltip/](https://graphviz.org/docs/attrs/tooltip/)
   *
   * Also controlled by the `atlas.graphviz.node.tooltip` Gradle property.
   */
  public var tooltip: String?

  /**
   * [https://graphviz.org/docs/attrs/URL/](https://graphviz.org/docs/attrs/URL/)
   *
   * Also controlled by the `atlas.graphviz.node.url` Gradle property.
   */
  public var url: String?

  /**
   * [https://graphviz.org/docs/attrs/vertices/](https://graphviz.org/docs/attrs/vertices/)
   *
   * Also controlled by the `atlas.graphviz.node.vertices` Gradle property.
   */
  public var vertices: String?

  /**
   * [https://graphviz.org/docs/attrs/width/](https://graphviz.org/docs/attrs/width/)
   *
   * Also controlled by the `atlas.graphviz.node.width` Gradle property.
   */
  public var width: Number?

  /**
   * [https://graphviz.org/docs/attrs/xlabel/](https://graphviz.org/docs/attrs/xlabel/)
   *
   * Also controlled by the `atlas.graphviz.node.xlabel` Gradle property.
   */
  public var xlabel: String?

  /**
   * [https://graphviz.org/docs/attrs/xlp/](https://graphviz.org/docs/attrs/xlp/)
   *
   * Also controlled by the `atlas.graphviz.node.xlp` Gradle property.
   */
  public var xlp: String?

  /**
   * [https://graphviz.org/docs/attrs/z/](https://graphviz.org/docs/attrs/z/)
   *
   * Also controlled by the `atlas.graphviz.node.z` Gradle property.
   */
  public var z: Number?
}
