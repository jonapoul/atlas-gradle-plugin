package atlas.d2

import atlas.core.AtlasDsl
import atlas.core.AtlasSpec
import atlas.core.PropertiesSpec
import org.gradle.api.Action
import org.gradle.api.provider.Property

/**
 * Used to configure D2 output from Atlas. For barebones output with the default config, call `d2()`
 * in the `atlas` block. Or for a more fleshed-out config:
 * ```kotlin
 * atlas {
 *   // other Atlas config
 *
 *   d2 {
 *     animateLinks = true
 *     center = true
 *     direction = Direction.Down
 *     fileFormat = FileFormat.Svg
 *     groupLabelLocation = Location.Inside
 *     groupLabelPosition = Position.TopCenter
 *     intermediateFilesInBuildDir = false
 *     pad = 5
 *     pathToD2Command = "/path/to/d2"
 *     scale = 0.5f
 *     sketch = true
 *     theme = Theme.ColorblindClear
 *     themeDark = Theme.DarkMauve
 *
 *     rootStyle {
 *       ...
 *     }
 *
 *     globalProps {
 *       ...
 *     }
 *
 *     layoutEngine {
 *       ...
 *     }
 *   }
 * }
 * ```
 */
@AtlasDsl
public interface D2Spec : AtlasSpec {
  /**
   * When true, dashed and dotted links are animated. Only works for SVG and GIF [fileFormat]s, and
   * Atlas warns you if you pick another one. Defaults to false.
   *
   * Also controlled by the `atlas.d2.animateLinks` Gradle property.
   */
  public val animateLinks: Property<Boolean>

  /**
   * Milliseconds between frames. Only used when [fileFormat] is [FileFormat.Gif]. Unset by default,
   * so D2 uses its own default of 1000ms.
   *
   * Also controlled by the `atlas.d2.animateInterval` Gradle property.
   */
  public val animateInterval: Property<Int>

  /**
   * Centers the SVG within its containing viewbox. Defaults to false.
   *
   * Also controlled by the `atlas.d2.center` Gradle property.
   */
  public val center: Property<Boolean>

  /**
   * The flow direction of the chart. Unset by default, so D2 uses [Direction.Down].
   *
   * Also controlled by the `atlas.d2.direction` Gradle property.
   */
  public val direction: Property<Direction>

  /**
   * The format of the rendered chart file. Defaults to [FileFormat.Svg].
   *
   * Also controlled by the `atlas.d2.fileFormat` Gradle property.
   */
  public val fileFormat: Property<FileFormat>

  /**
   * Whether group labels sit inside, outside or on the border of their group. Only used when
   * [atlas.core.AtlasExtension.groupProjects] is true.
   *
   * Also controlled by the `atlas.d2.groupLabelLocation` Gradle property.
   */
  public val groupLabelLocation: Property<Location>

  /**
   * Where group labels sit along their group's edge. Only used when
   * [atlas.core.AtlasExtension.groupProjects] is true.
   *
   * Also controlled by the `atlas.d2.groupLabelPosition` Gradle property.
   */
  public val groupLabelPosition: Property<Position>

  /**
   * Set to true to write the `chart.d2` and `classes.d2` files to `build/atlas/d2/` instead of the
   * project's `atlas/d2/` directory. Only the rendered image goes in `atlas/d2/`. Defaults to true.
   *
   * [atlas.core.AtlasExtension.checkOutputs] only verifies these files, so no D2 check tasks are
   * registered while this is enabled.
   *
   * Also controlled by the `atlas.d2.intermediateFilesInBuildDir` Gradle property.
   */
  public val intermediateFilesInBuildDir: Property<Boolean>

  /**
   * Padding around the chart, in pixels. Unset by default, so D2 uses its own default of 100.
   *
   * Also controlled by the `atlas.d2.pad` Gradle property.
   */
  public val pad: Property<Int>

  /**
   * Use this if you want to specify a "d2" command which isn't on the system path. This should be
   * an absolute path.
   *
   * Also controlled by the `atlas.d2.pathToD2Command` Gradle property.
   */
  public val pathToD2Command: Property<String>

  /**
   * Scales the rendered chart, passed to D2's `--scale`. Unset by default, so D2 fits SVGs to the
   * viewer's screen and renders other formats at their natural size.
   *
   * Also controlled by the `atlas.d2.scale` Gradle property.
   */
  public val scale: Property<Float>

  /**
   * Draws the chart in a hand-drawn style. Defaults to false.
   *
   * Also controlled by the `atlas.d2.sketch` Gradle property.
   */
  public val sketch: Property<Boolean>

  /**
   * The color scheme of the chart. Unset by default, so D2 uses its default theme.
   *
   * Also controlled by the `atlas.d2.theme` Gradle property. See
   * [the D2 docs](https://d2lang.com/tour/themes/)
   */
  public val theme: Property<Theme>

  /**
   * The color scheme used when the viewer is in dark mode. Only works for SVGs. Unset by default.
   *
   * Also controlled by the `atlas.d2.darkTheme` Gradle property. See
   * [the D2 docs](https://d2lang.com/tour/themes/)
   */
  public val themeDark: Property<Theme>

  /** Configure the layout engine used to arrange the chart. */
  public val layoutEngine: D2LayoutEngineSpec

  public fun layoutEngine(action: Action<D2LayoutEngineSpec>)

  /** Style properties applied to the chart itself, e.g. its background. */
  public val rootStyle: D2RootStyleSpec

  public fun rootStyle(action: Action<D2RootStyleSpec>)

  /**
   * Style properties applied to all nodes and links, unless overridden by a project or link type.
   */
  public val globalProps: D2GlobalPropsSpec

  public fun globalProps(action: Action<D2GlobalPropsSpec>)
}

/** https://d2lang.com/tour/style/#root */
@AtlasDsl
public interface D2RootStyleSpec : PropertiesSpec {
  /** The chart's background color, e.g. "transparent" or any CSS color or hex string. */
  public var fill: String?

  /** A texture drawn over the chart's background. */
  public var fillPattern: FillPattern?

  /** The color of the chart's border. */
  public var stroke: String?

  /** The width of the chart's border. */
  public var strokeWidth: Int?

  /** Draws the chart's border as a dashed line, with this as the gap between dashes. */
  public var strokeDash: Int?

  /** Draws a second border around the chart. */
  public var doubleBorder: Boolean?
}

/**
 * Use this for any arbitrary global properties that you want to apply to all charts. These
 * properties will be appended to the bottom of the chart. Chances are, you'll want to use some
 * [globs](https://d2lang.com/tour/globs/) in here, so make sure to read the D2 docs on those.
 */
@AtlasDsl
public interface D2GlobalPropsSpec : PropertiesSpec {
  /** The shape of the arrowhead on every link. */
  public var arrowType: ArrowType?

  /** Whether arrowheads on every link are filled in. */
  public var fillArrowHeads: Boolean?

  /**
   * The font of all text in the chart. [Font.Mono] is the only option, so leave this unset for D2's
   * default font.
   */
  public var font: Font?

  /** The size of all text in the chart. */
  public var fontSize: Int?
}
