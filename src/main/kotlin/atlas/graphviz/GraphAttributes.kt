@file:Suppress("SpellCheckingInspection")

package atlas.graphviz

import atlas.core.AtlasDsl
import atlas.core.PropertiesSpec

/**
 * These attributes will be applied to the root graph area.
 *
 * See [https://graphviz.org/docs/graph/](https://graphviz.org/docs/graph/) for any restrictions,
 * this interface will just pass-through the attribute and let Graphviz handle validation.
 */
@AtlasDsl
public interface GraphAttributes : PropertiesSpec {
  /**
   * [https://graphviz.org/docs/attrs/background/](https://graphviz.org/docs/attrs/background/)
   *
   * Also controlled by the `atlas.graphviz.graph.background` Gradle property.
   */
  public var background: String?

  /**
   * [https://graphviz.org/docs/attrs/bb/](https://graphviz.org/docs/attrs/bb/)
   *
   * Also controlled by the `atlas.graphviz.graph.bb` Gradle property.
   */
  public var bb: String?

  /**
   * [https://graphviz.org/docs/attrs/beautify/](https://graphviz.org/docs/attrs/beautify/)
   *
   * Also controlled by the `atlas.graphviz.graph.beautify` Gradle property.
   */
  public var beautify: Boolean?

  /**
   * [https://graphviz.org/docs/attrs/bgcolor/](https://graphviz.org/docs/attrs/bgcolor/)
   *
   * Also controlled by the `atlas.graphviz.graph.bgColor` Gradle property.
   */
  public var bgColor: String?

  /**
   * [https://graphviz.org/docs/attrs/center/](https://graphviz.org/docs/attrs/center/)
   *
   * Also controlled by the `atlas.graphviz.graph.center` Gradle property.
   */
  public var center: Boolean?

  /**
   * [https://graphviz.org/docs/attrs/charset/](https://graphviz.org/docs/attrs/charset/)
   *
   * Also controlled by the `atlas.graphviz.graph.charset` Gradle property.
   */
  public var charset: String?

  /**
   * [https://graphviz.org/docs/attrs/clusterrank/](https://graphviz.org/docs/attrs/clusterrank/)
   *
   * Also controlled by the `atlas.graphviz.graph.clusterRank` Gradle property.
   */
  public var clusterRank: ClusterMode?

  /**
   * [https://graphviz.org/docs/attrs/colorscheme/](https://graphviz.org/docs/attrs/colorscheme/)
   *
   * Also controlled by the `atlas.graphviz.graph.colorScheme` Gradle property.
   */
  public var colorScheme: String?

  /**
   * [https://graphviz.org/docs/attrs/comment/](https://graphviz.org/docs/attrs/comment/)
   *
   * Also controlled by the `atlas.graphviz.graph.comment` Gradle property.
   */
  public var comment: String?

  /**
   * [https://graphviz.org/docs/attrs/compound/](https://graphviz.org/docs/attrs/compound/)
   *
   * Also controlled by the `atlas.graphviz.graph.compound` Gradle property.
   */
  public var compound: Boolean?

  /**
   * [https://graphviz.org/docs/attrs/concentrate/](https://graphviz.org/docs/attrs/concentrate/)
   *
   * Also controlled by the `atlas.graphviz.graph.concentrate` Gradle property.
   */
  public var concentrate: Boolean?

  /**
   * [https://graphviz.org/docs/attrs/Damping/](https://graphviz.org/docs/attrs/Damping/)
   *
   * Also controlled by the `atlas.graphviz.graph.damping` Gradle property.
   */
  public var damping: Float?

  /**
   * [https://graphviz.org/docs/attrs/defaultdist/](https://graphviz.org/docs/attrs/defaultdist/)
   *
   * Also controlled by the `atlas.graphviz.graph.defaultDist` Gradle property.
   */
  public var defaultDist: Number?

  /**
   * [https://graphviz.org/docs/attrs/dim/](https://graphviz.org/docs/attrs/dim/)
   *
   * Also controlled by the `atlas.graphviz.graph.dim` Gradle property.
   */
  public var dim: Int?

  /**
   * [https://graphviz.org/docs/attrs/dimen/](https://graphviz.org/docs/attrs/dimen/)
   *
   * Also controlled by the `atlas.graphviz.graph.dimen` Gradle property.
   */
  public var dimen: Int?

  /**
   * [https://graphviz.org/docs/attrs/diredgeconstraints/](https://graphviz.org/docs/attrs/diredgeconstraints/)
   *
   * Also controlled by the `atlas.graphviz.graph.dirEdgeConstraints` Gradle property.
   */
  public var dirEdgeConstraints: String?

  /**
   * [https://graphviz.org/docs/attrs/dpi/](https://graphviz.org/docs/attrs/dpi/)
   *
   * Also controlled by the `atlas.graphviz.graph.dpi` Gradle property.
   */
  public var dpi: Number?

  /**
   * [https://graphviz.org/docs/attrs/epsilon/](https://graphviz.org/docs/attrs/epsilon/)
   *
   * Also controlled by the `atlas.graphviz.graph.epsilon` Gradle property.
   */
  public var epsilon: Float?

  /**
   * [https://graphviz.org/docs/attrs/esep/](https://graphviz.org/docs/attrs/esep/)
   *
   * Also controlled by the `atlas.graphviz.graph.esep` Gradle property.
   */
  public var esep: String?

  /**
   * [https://graphviz.org/docs/attrs/fontcolor/](https://graphviz.org/docs/attrs/fontcolor/)
   *
   * Also controlled by the `atlas.graphviz.graph.fontColor` Gradle property.
   */
  public var fontColor: String?

  /**
   * [https://graphviz.org/docs/attrs/fontname/](https://graphviz.org/docs/attrs/fontname/)
   *
   * Also controlled by the `atlas.graphviz.graph.fontName` Gradle property.
   */
  public var fontName: String?

  /**
   * [https://graphviz.org/docs/attrs/fontnames/](https://graphviz.org/docs/attrs/fontnames/)
   *
   * Also controlled by the `atlas.graphviz.graph.fontNames` Gradle property.
   */
  public var fontNames: String?

  /**
   * [https://graphviz.org/docs/attrs/fontpath/](https://graphviz.org/docs/attrs/fontpath/)
   *
   * Also controlled by the `atlas.graphviz.graph.fontPath` Gradle property.
   */
  public var fontPath: String?

  /**
   * [https://graphviz.org/docs/attrs/fontsize/](https://graphviz.org/docs/attrs/fontsize/)
   *
   * Also controlled by the `atlas.graphviz.graph.fontSize` Gradle property.
   */
  public var fontSize: String?

  /**
   * [https://graphviz.org/docs/attrs/forcelabels/](https://graphviz.org/docs/attrs/forcelabels/)
   *
   * Also controlled by the `atlas.graphviz.graph.forceLabels` Gradle property.
   */
  public var forceLabels: Boolean?

  /**
   * [https://graphviz.org/docs/attrs/gradientangle/](https://graphviz.org/docs/attrs/gradientangle/)
   *
   * Also controlled by the `atlas.graphviz.graph.gradientAngle` Gradle property.
   */
  public var gradientAngle: Int?

  /**
   * [https://graphviz.org/docs/attrs/href/](https://graphviz.org/docs/attrs/href/)
   *
   * Also controlled by the `atlas.graphviz.graph.href` Gradle property.
   */
  public var href: String?

  /**
   * [https://graphviz.org/docs/attrs/id/](https://graphviz.org/docs/attrs/id/)
   *
   * Also controlled by the `atlas.graphviz.graph.id` Gradle property.
   */
  public var id: String?

  /**
   * [https://graphviz.org/docs/attrs/imagepath/](https://graphviz.org/docs/attrs/imagepath/)
   *
   * Also controlled by the `atlas.graphviz.graph.imagePath` Gradle property.
   */
  public var imagePath: String?

  /**
   * [https://graphviz.org/docs/attrs/inputscale/](https://graphviz.org/docs/attrs/inputscale/)
   *
   * Also controlled by the `atlas.graphviz.graph.inputScale` Gradle property.
   */
  public var inputScale: Number?

  /**
   * [https://graphviz.org/docs/attrs/K/](https://graphviz.org/docs/attrs/K/)
   *
   * Also controlled by the `atlas.graphviz.graph.k` Gradle property.
   */
  public var k: Number?

  /**
   * [https://graphviz.org/docs/attrs/label/](https://graphviz.org/docs/attrs/label/)
   *
   * Also controlled by the `atlas.graphviz.graph.label` Gradle property.
   */
  public var label: String?

  /**
   * [https://graphviz.org/docs/attrs/label_scheme/](https://graphviz.org/docs/attrs/label_scheme/)
   *
   * Also controlled by the `atlas.graphviz.graph.labelScheme` Gradle property.
   */
  public var labelScheme: Int?

  /**
   * [https://graphviz.org/docs/attrs/labeljust/](https://graphviz.org/docs/attrs/labeljust/)
   *
   * Also controlled by the `atlas.graphviz.graph.labelJust` Gradle property.
   */
  public var labelJust: String?

  /**
   * [https://graphviz.org/docs/attrs/labelloc/](https://graphviz.org/docs/attrs/labelloc/)
   *
   * Also controlled by the `atlas.graphviz.graph.labelLoc` Gradle property.
   */
  public var labelLoc: String?

  /**
   * [https://graphviz.org/docs/attrs/landscape/](https://graphviz.org/docs/attrs/landscape/)
   *
   * Also controlled by the `atlas.graphviz.graph.landscape` Gradle property.
   */
  public var landscape: Boolean?

  /**
   * [https://graphviz.org/docs/attrs/layerlistsep/](https://graphviz.org/docs/attrs/layerlistsep/)
   *
   * Also controlled by the `atlas.graphviz.graph.layerListSep` Gradle property.
   */
  public var layerListSep: String?

  /**
   * [https://graphviz.org/docs/attrs/layers/](https://graphviz.org/docs/attrs/layers/)
   *
   * Also controlled by the `atlas.graphviz.graph.layers` Gradle property.
   */
  public var layers: String?

  /**
   * [https://graphviz.org/docs/attrs/layerselect/](https://graphviz.org/docs/attrs/layerselect/)
   *
   * Also controlled by the `atlas.graphviz.graph.layerSelect` Gradle property.
   */
  public var layerSelect: String?

  /**
   * [https://graphviz.org/docs/attrs/layersep/](https://graphviz.org/docs/attrs/layersep/)
   *
   * Also controlled by the `atlas.graphviz.graph.layerSep` Gradle property.
   */
  public var layerSep: String?

  /**
   * [https://graphviz.org/docs/attrs/layout/](https://graphviz.org/docs/attrs/layout/)
   *
   * Also controlled by the `atlas.graphviz.graph.layout` Gradle property.
   */
  public var layout: LayoutEngine?

  /**
   * [https://graphviz.org/docs/attrs/levels/](https://graphviz.org/docs/attrs/levels/)
   *
   * Also controlled by the `atlas.graphviz.graph.levels` Gradle property.
   */
  public var levels: Int?

  /**
   * [https://graphviz.org/docs/attrs/levelsgap/](https://graphviz.org/docs/attrs/levelsgap/)
   *
   * Also controlled by the `atlas.graphviz.graph.levelsGap` Gradle property.
   */
  public var levelsGap: Number?

  /**
   * [https://graphviz.org/docs/attrs/lheight/](https://graphviz.org/docs/attrs/lheight/)
   *
   * Also controlled by the `atlas.graphviz.graph.lheight` Gradle property.
   */
  public var lheight: Number?

  /**
   * [https://graphviz.org/docs/attrs/linelength/](https://graphviz.org/docs/attrs/linelength/)
   *
   * Also controlled by the `atlas.graphviz.graph.lineLength` Gradle property.
   */
  public var lineLength: Int?

  /**
   * [https://graphviz.org/docs/attrs/lp/](https://graphviz.org/docs/attrs/lp/)
   *
   * Also controlled by the `atlas.graphviz.graph.lp` Gradle property.
   */
  public var lp: String?

  /**
   * [https://graphviz.org/docs/attrs/lwidth/](https://graphviz.org/docs/attrs/lwidth/)
   *
   * Also controlled by the `atlas.graphviz.graph.lWidth` Gradle property.
   */
  public var lWidth: Number?

  /**
   * [https://graphviz.org/docs/attrs/margin/](https://graphviz.org/docs/attrs/margin/)
   *
   * Also controlled by the `atlas.graphviz.graph.margin` Gradle property.
   */
  public var margin: String?

  /**
   * [https://graphviz.org/docs/attrs/maxiter/](https://graphviz.org/docs/attrs/maxiter/)
   *
   * Also controlled by the `atlas.graphviz.graph.maxiter` Gradle property.
   */
  public var maxiter: Int?

  /**
   * [https://graphviz.org/docs/attrs/mclimit/](https://graphviz.org/docs/attrs/mclimit/)
   *
   * Also controlled by the `atlas.graphviz.graph.mcLimit` Gradle property.
   */
  public var mcLimit: Number?

  /**
   * [https://graphviz.org/docs/attrs/mindist/](https://graphviz.org/docs/attrs/mindist/)
   *
   * Also controlled by the `atlas.graphviz.graph.minDist` Gradle property.
   */
  public var minDist: Number?

  /**
   * [https://graphviz.org/docs/attrs/mode/](https://graphviz.org/docs/attrs/mode/)
   *
   * Also controlled by the `atlas.graphviz.graph.mode` Gradle property.
   */
  public var mode: String?

  /**
   * [https://graphviz.org/docs/attrs/model/](https://graphviz.org/docs/attrs/model/)
   *
   * Also controlled by the `atlas.graphviz.graph.model` Gradle property.
   */
  public var model: String?

  /**
   * [https://graphviz.org/docs/attrs/newrank/](https://graphviz.org/docs/attrs/newrank/)
   *
   * Also controlled by the `atlas.graphviz.graph.newRank` Gradle property.
   */
  public var newRank: Boolean?

  /**
   * [https://graphviz.org/docs/attrs/nodesep/](https://graphviz.org/docs/attrs/nodesep/)
   *
   * Also controlled by the `atlas.graphviz.graph.nodeSep` Gradle property.
   */
  public var nodeSep: Number?

  /**
   * [https://graphviz.org/docs/attrs/nojustify/](https://graphviz.org/docs/attrs/nojustify/)
   *
   * Also controlled by the `atlas.graphviz.graph.noJustify` Gradle property.
   */
  public var noJustify: Boolean?

  /**
   * [https://graphviz.org/docs/attrs/normalize/](https://graphviz.org/docs/attrs/normalize/)
   *
   * Also controlled by the `atlas.graphviz.graph.normalize` Gradle property.
   */
  public var normalize: String?

  /**
   * [https://graphviz.org/docs/attrs/notranslate/](https://graphviz.org/docs/attrs/notranslate/)
   *
   * Also controlled by the `atlas.graphviz.graph.noTranslate` Gradle property.
   */
  public var noTranslate: Boolean?

  /**
   * [https://graphviz.org/docs/attrs/nslimit/](https://graphviz.org/docs/attrs/nslimit/)
   *
   * Also controlled by the `atlas.graphviz.graph.nsLimit` Gradle property.
   */
  public var nsLimit: Number?

  /**
   * [https://graphviz.org/docs/attrs/nslimit1/](https://graphviz.org/docs/attrs/nslimit1/)
   *
   * Also controlled by the `atlas.graphviz.graph.nsLimit1` Gradle property.
   */
  public var nsLimit1: Number?

  /**
   * [https://graphviz.org/docs/attrs/oneblock/](https://graphviz.org/docs/attrs/oneblock/)
   *
   * Also controlled by the `atlas.graphviz.graph.oneBlock` Gradle property.
   */
  public var oneBlock: Boolean?

  /**
   * [https://graphviz.org/docs/attrs/ordering/](https://graphviz.org/docs/attrs/ordering/)
   *
   * Also controlled by the `atlas.graphviz.graph.ordering` Gradle property.
   */
  public var ordering: String?

  /**
   * [https://graphviz.org/docs/attrs/orientation/](https://graphviz.org/docs/attrs/orientation/)
   *
   * Also controlled by the `atlas.graphviz.graph.orientation` Gradle property.
   */
  public var orientation: Number?

  /**
   * [https://graphviz.org/docs/attrs/outputorder/](https://graphviz.org/docs/attrs/outputorder/)
   *
   * Also controlled by the `atlas.graphviz.graph.outputOrder` Gradle property.
   */
  public var outputOrder: String?

  /**
   * [https://graphviz.org/docs/attrs/overlap/](https://graphviz.org/docs/attrs/overlap/)
   *
   * Also controlled by the `atlas.graphviz.graph.overlap` Gradle property.
   */
  public var overlap: String?

  /**
   * [https://graphviz.org/docs/attrs/overlap_scaling/](https://graphviz.org/docs/attrs/overlap_scaling/)
   *
   * Also controlled by the `atlas.graphviz.graph.overlapScaling` Gradle property.
   */
  public var overlapScaling: Number?

  /**
   * [https://graphviz.org/docs/attrs/overlap_shrink/](https://graphviz.org/docs/attrs/overlap_shrink/)
   *
   * Also controlled by the `atlas.graphviz.graph.overlapShrink` Gradle property.
   */
  public var overlapShrink: Boolean?

  /**
   * [https://graphviz.org/docs/attrs/pack/](https://graphviz.org/docs/attrs/pack/)
   *
   * Also controlled by the `atlas.graphviz.graph.pack` Gradle property.
   */
  public var pack: String?

  /**
   * [https://graphviz.org/docs/attrs/packmode/](https://graphviz.org/docs/attrs/packmode/)
   *
   * Also controlled by the `atlas.graphviz.graph.packMode` Gradle property.
   */
  public var packMode: String?

  /**
   * [https://graphviz.org/docs/attrs/pad/](https://graphviz.org/docs/attrs/pad/)
   *
   * Also controlled by the `atlas.graphviz.graph.pad` Gradle property.
   */
  public var pad: String?

  /**
   * [https://graphviz.org/docs/attrs/page/](https://graphviz.org/docs/attrs/page/)
   *
   * Also controlled by the `atlas.graphviz.graph.page` Gradle property.
   */
  public var page: String?

  /**
   * [https://graphviz.org/docs/attrs/pagedir/](https://graphviz.org/docs/attrs/pagedir/)
   *
   * Also controlled by the `atlas.graphviz.graph.pageDir` Gradle property.
   */
  public var pageDir: String?

  /**
   * [https://graphviz.org/docs/attrs/quadtree/](https://graphviz.org/docs/attrs/quadtree/)
   *
   * Also controlled by the `atlas.graphviz.graph.quadTree` Gradle property.
   */
  public var quadTree: String?

  /**
   * [https://graphviz.org/docs/attrs/quantum/](https://graphviz.org/docs/attrs/quantum/)
   *
   * Also controlled by the `atlas.graphviz.graph.quantum` Gradle property.
   */
  public var quantum: Number?

  /**
   * [https://graphviz.org/docs/attrs/rankdir/](https://graphviz.org/docs/attrs/rankdir/)
   *
   * Also controlled by the `atlas.graphviz.graph.rankDir` Gradle property.
   */
  public var rankDir: RankDir?

  /**
   * [https://graphviz.org/docs/attrs/ranksep/](https://graphviz.org/docs/attrs/ranksep/)
   *
   * Also controlled by the `atlas.graphviz.graph.rankSep` Gradle property.
   */
  public var rankSep: Number?

  /**
   * [https://graphviz.org/docs/attrs/ratio/](https://graphviz.org/docs/attrs/ratio/)
   *
   * Also controlled by the `atlas.graphviz.graph.ratio` Gradle property.
   */
  public var ratio: String?

  /**
   * [https://graphviz.org/docs/attrs/remincross/](https://graphviz.org/docs/attrs/remincross/)
   *
   * Also controlled by the `atlas.graphviz.graph.reminCross` Gradle property.
   */
  public var reminCross: Boolean?

  /**
   * [https://graphviz.org/docs/attrs/repulsiveforce/](https://graphviz.org/docs/attrs/repulsiveforce/)
   *
   * Also controlled by the `atlas.graphviz.graph.repulsiveForce` Gradle property.
   */
  public var repulsiveForce: Number?

  /**
   * [https://graphviz.org/docs/attrs/resolution/](https://graphviz.org/docs/attrs/resolution/)
   *
   * Also controlled by the `atlas.graphviz.graph.resolution` Gradle property.
   */
  public var resolution: Number?

  /**
   * [https://graphviz.org/docs/attrs/root/](https://graphviz.org/docs/attrs/root/)
   *
   * Also controlled by the `atlas.graphviz.graph.root` Gradle property.
   */
  public var root: String?

  /**
   * [https://graphviz.org/docs/attrs/rotate/](https://graphviz.org/docs/attrs/rotate/)
   *
   * Also controlled by the `atlas.graphviz.graph.rotate` Gradle property.
   */
  public var rotate: Int?

  /**
   * [https://graphviz.org/docs/attrs/rotation/](https://graphviz.org/docs/attrs/rotation/)
   *
   * Also controlled by the `atlas.graphviz.graph.rotation` Gradle property.
   */
  public var rotation: Number?

  /**
   * [https://graphviz.org/docs/attrs/scale/](https://graphviz.org/docs/attrs/scale/)
   *
   * Also controlled by the `atlas.graphviz.graph.scale` Gradle property.
   */
  public var scale: String?

  /**
   * [https://graphviz.org/docs/attrs/searchsize/](https://graphviz.org/docs/attrs/searchsize/)
   *
   * Also controlled by the `atlas.graphviz.graph.searchSize` Gradle property.
   */
  public var searchSize: Int?

  /**
   * [https://graphviz.org/docs/attrs/sep/](https://graphviz.org/docs/attrs/sep/)
   *
   * Also controlled by the `atlas.graphviz.graph.sep` Gradle property.
   */
  public var sep: String?

  /**
   * [https://graphviz.org/docs/attrs/showboxes/](https://graphviz.org/docs/attrs/showboxes/)
   *
   * Also controlled by the `atlas.graphviz.graph.showBoxes` Gradle property.
   */
  public var showBoxes: Int?

  /**
   * [https://graphviz.org/docs/attrs/size/](https://graphviz.org/docs/attrs/size/)
   *
   * Also controlled by the `atlas.graphviz.graph.size` Gradle property.
   */
  public var size: String?

  /**
   * [https://graphviz.org/docs/attrs/smoothing/](https://graphviz.org/docs/attrs/smoothing/)
   *
   * Also controlled by the `atlas.graphviz.graph.smoothing` Gradle property.
   */
  public var smoothing: SmoothType?

  /**
   * [https://graphviz.org/docs/attrs/sortv/](https://graphviz.org/docs/attrs/sortv/)
   *
   * Also controlled by the `atlas.graphviz.graph.sortv` Gradle property.
   */
  public var sortv: Int?

  /**
   * [https://graphviz.org/docs/attrs/splines/](https://graphviz.org/docs/attrs/splines/)
   *
   * Also controlled by the `atlas.graphviz.graph.splines` Gradle property.
   */
  public var splines: String?

  /**
   * [https://graphviz.org/docs/attrs/start/](https://graphviz.org/docs/attrs/start/)
   *
   * Also controlled by the `atlas.graphviz.graph.start` Gradle property.
   */
  public var start: String?

  /**
   * [https://graphviz.org/docs/attrs/style/](https://graphviz.org/docs/attrs/style/)
   *
   * Also controlled by the `atlas.graphviz.graph.style` Gradle property.
   */
  public var style: String?

  /**
   * [https://graphviz.org/docs/attrs/stylesheet/](https://graphviz.org/docs/attrs/stylesheet/)
   *
   * Also controlled by the `atlas.graphviz.graph.styleSheet` Gradle property.
   */
  public var styleSheet: String?

  /**
   * [https://graphviz.org/docs/attrs/target/](https://graphviz.org/docs/attrs/target/)
   *
   * Also controlled by the `atlas.graphviz.graph.target` Gradle property.
   */
  public var target: String?

  /**
   * [https://graphviz.org/docs/attrs/TBbalance/](https://graphviz.org/docs/attrs/TBbalance/)
   *
   * Also controlled by the `atlas.graphviz.graph.tbBalance` Gradle property.
   */
  public var tbBalance: String?

  /**
   * [https://graphviz.org/docs/attrs/tooltip/](https://graphviz.org/docs/attrs/tooltip/)
   *
   * Also controlled by the `atlas.graphviz.graph.tooltip` Gradle property.
   */
  public var tooltip: String?

  /**
   * [https://graphviz.org/docs/attrs/truecolor/](https://graphviz.org/docs/attrs/truecolor/)
   *
   * Also controlled by the `atlas.graphviz.graph.trueColor` Gradle property.
   */
  public var trueColor: Boolean?

  /**
   * [https://graphviz.org/docs/attrs/URL/](https://graphviz.org/docs/attrs/URL/)
   *
   * Also controlled by the `atlas.graphviz.graph.url` Gradle property.
   */
  public var url: String?

  /**
   * [https://graphviz.org/docs/attrs/viewport/](https://graphviz.org/docs/attrs/viewport/)
   *
   * Also controlled by the `atlas.graphviz.graph.viewPort` Gradle property.
   */
  public var viewPort: String?

  /**
   * [https://graphviz.org/docs/attrs/voro_margin/](https://graphviz.org/docs/attrs/voro_margin/)
   *
   * Also controlled by the `atlas.graphviz.graph.voroMargin` Gradle property.
   */
  public var voroMargin: Number?

  /**
   * [https://graphviz.org/docs/attrs/xdotversion/](https://graphviz.org/docs/attrs/xdotversion/)
   *
   * Also controlled by the `atlas.graphviz.graph.xdotVersion` Gradle property.
   */
  public var xdotVersion: String?
}
