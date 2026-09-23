@file:Suppress("SpellCheckingInspection")

package atlas.graphviz

import atlas.core.AtlasDsl
import atlas.core.PropertiesSpec

/**
 * These attributes will be applied to every cluster, i.e. the bordered containers drawn around
 * grouped projects when [atlas.core.AtlasExtension.groupProjects] is enabled.
 *
 * See [https://graphviz.org/docs/clusters/](https://graphviz.org/docs/clusters/) for any
 * restrictions, this interface will just pass-through the attribute and let Graphviz handle
 * validation.
 */
@AtlasDsl
public interface ClusterAttributes : PropertiesSpec {
  /**
   * [https://graphviz.org/docs/attrs/bgcolor/](https://graphviz.org/docs/attrs/bgcolor/)
   *
   * Also controlled by the `atlas.graphviz.cluster.bgColor` Gradle property.
   */
  public var bgColor: String?

  /**
   * [https://graphviz.org/docs/attrs/fillcolor/](https://graphviz.org/docs/attrs/fillcolor/)
   *
   * Also controlled by the `atlas.graphviz.cluster.fillColor` Gradle property.
   */
  public var fillColor: String?

  /**
   * [https://graphviz.org/docs/attrs/fontcolor/](https://graphviz.org/docs/attrs/fontcolor/)
   *
   * Also controlled by the `atlas.graphviz.cluster.fontColor` Gradle property.
   */
  public var fontColor: String?

  /**
   * [https://graphviz.org/docs/attrs/fontname/](https://graphviz.org/docs/attrs/fontname/)
   *
   * Also controlled by the `atlas.graphviz.cluster.fontName` Gradle property.
   */
  public var fontName: String?

  /**
   * [https://graphviz.org/docs/attrs/fontsize/](https://graphviz.org/docs/attrs/fontsize/)
   *
   * Also controlled by the `atlas.graphviz.cluster.fontSize` Gradle property.
   */
  public var fontSize: String?

  /**
   * [https://graphviz.org/docs/attrs/labeljust/](https://graphviz.org/docs/attrs/labeljust/)
   *
   * Also controlled by the `atlas.graphviz.cluster.labelJust` Gradle property.
   */
  public var labelJust: String?

  /**
   * [https://graphviz.org/docs/attrs/labelloc/](https://graphviz.org/docs/attrs/labelloc/)
   *
   * Also controlled by the `atlas.graphviz.cluster.labelLoc` Gradle property.
   */
  public var labelLoc: String?

  /**
   * [https://graphviz.org/docs/attrs/color/](https://graphviz.org/docs/attrs/color/)
   *
   * Also controlled by the `atlas.graphviz.cluster.lineColor` Gradle property.
   */
  public var lineColor: String?

  /**
   * [https://graphviz.org/docs/attrs/margin/](https://graphviz.org/docs/attrs/margin/)
   *
   * Also controlled by the `atlas.graphviz.cluster.margin` Gradle property.
   */
  public var margin: String?

  /**
   * [https://graphviz.org/docs/attrs/pencolor/](https://graphviz.org/docs/attrs/pencolor/)
   *
   * Also controlled by the `atlas.graphviz.cluster.penColor` Gradle property.
   */
  public var penColor: String?

  /**
   * [https://graphviz.org/docs/attrs/penwidth/](https://graphviz.org/docs/attrs/penwidth/)
   *
   * Also controlled by the `atlas.graphviz.cluster.penWidth` Gradle property.
   */
  public var penWidth: Number?

  /**
   * [https://graphviz.org/docs/attrs/style/](https://graphviz.org/docs/attrs/style/)
   *
   * Also controlled by the `atlas.graphviz.cluster.style` Gradle property.
   */
  public var style: String?
}
