package atlas.d2

import atlas.core.AtlasDsl
import atlas.core.AtlasSpec
import atlas.core.PropertiesSpec
import org.gradle.api.Action
import org.gradle.api.file.RegularFileProperty
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
 *     asciiMode = AsciiMode.Standard
 *     center = true
 *     d2Executable = file("/path/to/d2")
 *     d2Version = "0.9.0"
 *     direction = Direction.Down
 *     executableSource = ExecutableSource.Download
 *     fileFormat = FileFormat.Svg
 *     groupLabelLocation = Location.Inside
 *     groupLabelPosition = Position.TopCenter
 *     intermediateFilesInBuildDir = false
 *     noXmlTag = true
 *     omitVersion = true
 *     pad = 5
 *     scale = 0.5f
 *     sketch = true
 *     theme = Theme.ColorblindClear
 *     themeDark = Theme.DarkMauve
 *     timeout = 300
 *
 *     fonts {
 *       ...
 *     }
 *
 *     rootStyle {
 *       ...
 *     }
 *
 *     themeOverrides {
 *       ...
 *     }
 *
 *     themeDarkOverrides {
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
   * Which characters to draw the chart with. Only used when [fileFormat] is [FileFormat.Ascii].
   * Unset by default, so D2 uses [AsciiMode.Extended]. Requires D2 0.7.1 or newer.
   *
   * Also controlled by the `atlas.d2.asciiMode` Gradle property.
   */
  public val asciiMode: Property<AsciiMode>

  /**
   * Centers the SVG within its containing viewbox. Defaults to false.
   *
   * Also controlled by the `atlas.d2.center` Gradle property.
   */
  public val center: Property<Boolean>

  /**
   * The `d2` executable to run. Unset by default, so [executableSource] decides where it comes
   * from. When set, it always wins.
   *
   * Also controlled by the `atlas.d2.d2Executable` Gradle property, which should be an absolute
   * path.
   */
  public val d2Executable: RegularFileProperty

  /**
   * The D2 version to download, when [executableSource] needs to. Defaults to the version this
   * release of Atlas is tested against. Downloads are cached in the Gradle user home.
   *
   * Also controlled by the `atlas.d2.d2Version` Gradle property.
   */
  public val d2Version: Property<String>

  /**
   * The flow direction of the chart. Unset by default, so D2 uses [Direction.Down].
   *
   * Also controlled by the `atlas.d2.direction` Gradle property.
   */
  public val direction: Property<Direction>

  /**
   * Where to find `d2` when [d2Executable] isn't set. Defaults to [ExecutableSource.Auto], which
   * uses the system PATH and falls back to downloading [d2Version].
   *
   * Also controlled by the `atlas.d2.executableSource` Gradle property.
   */
  public val executableSource: Property<ExecutableSource>

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
   * Set to true to write the `chart-d2.d2` and `classes-d2.d2` files to `build/atlas/` instead of
   * the project directory and the root project's `atlas/` directory. Only the rendered image goes
   * alongside the project. Defaults to true.
   *
   * [atlas.core.AtlasExtension.checkOutputs] only verifies these files, so no D2 check tasks are
   * registered while this is enabled.
   *
   * Also controlled by the `atlas.d2.intermediateFilesInBuildDir` Gradle property.
   */
  public val intermediateFilesInBuildDir: Property<Boolean>

  /**
   * Leaves the `<?xml ... ?>` tag out of SVG files, which helps when embedding them straight into
   * HTML. Only used when [fileFormat] is [FileFormat.Svg]. Defaults to false.
   *
   * Also controlled by the `atlas.d2.noXmlTag` Gradle property.
   */
  public val noXmlTag: Property<Boolean>

  /**
   * Leaves the D2 version out of the rendered chart, so upgrading D2 doesn't change every chart
   * file. Defaults to false.
   *
   * Also controlled by the `atlas.d2.omitVersion` Gradle property.
   */
  public val omitVersion: Property<Boolean>

  /**
   * Padding around the chart, in pixels. Unset by default, so D2 uses its own default of 100.
   *
   * Also controlled by the `atlas.d2.pad` Gradle property.
   */
  public val pad: Property<Int>

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

  /**
   * The maximum number of seconds D2 may run for before failing. Unset by default, so D2 uses its
   * own default of 120. Worth raising for very large charts, and 0 turns the limit off.
   *
   * Also controlled by the `atlas.d2.timeout` Gradle property.
   */
  public val timeout: Property<Int>

  /** Custom `.ttf` files to render the chart's text with. */
  public val fonts: D2FontsSpec

  public fun fonts(action: Action<D2FontsSpec>)

  /** Configure the layout engine used to arrange the chart. */
  public val layoutEngine: D2LayoutEngineSpec

  public fun layoutEngine(action: Action<D2LayoutEngineSpec>)

  /** Style properties applied to the chart itself, e.g. its background. */
  public val rootStyle: D2RootStyleSpec

  public fun rootStyle(action: Action<D2RootStyleSpec>)

  /** Replaces individual colors of [theme]. */
  public val themeOverrides: D2ThemeOverridesSpec

  public fun themeOverrides(action: Action<D2ThemeOverridesSpec>)

  /** Replaces individual colors of [themeDark]. Only works for SVGs. */
  public val themeDarkOverrides: D2ThemeOverridesSpec

  public fun themeDarkOverrides(action: Action<D2ThemeOverridesSpec>)

  /**
   * Style properties applied to all nodes and links, unless overridden by a project or link type.
   */
  public val globalProps: D2GlobalPropsSpec

  public fun globalProps(action: Action<D2GlobalPropsSpec>)
}

/** https://d2lang.com/tour/style/#root */
@AtlasDsl
public interface D2RootStyleSpec : PropertiesSpec {
  /**
   * The chart's background color, e.g. "transparent" or any CSS color or hex string.
   *
   * Also controlled by the `atlas.d2.rootStyle.fill` Gradle property.
   */
  public var fill: String?

  /**
   * A texture drawn over the chart's background.
   *
   * Also controlled by the `atlas.d2.rootStyle.fillPattern` Gradle property.
   */
  public var fillPattern: FillPattern?

  /**
   * The color of the chart's border.
   *
   * Also controlled by the `atlas.d2.rootStyle.stroke` Gradle property.
   */
  public var stroke: String?

  /**
   * The width of the chart's border.
   *
   * Also controlled by the `atlas.d2.rootStyle.strokeWidth` Gradle property.
   */
  public var strokeWidth: Int?

  /**
   * Draws the chart's border as a dashed line, with this as the gap between dashes.
   *
   * Also controlled by the `atlas.d2.rootStyle.strokeDash` Gradle property.
   */
  public var strokeDash: Int?

  /**
   * Draws a second border around the chart.
   *
   * Also controlled by the `atlas.d2.rootStyle.doubleBorder` Gradle property.
   */
  public var doubleBorder: Boolean?
}

/**
 * Custom `.ttf` files to render the chart's text with. Each one is optional, and any left unset
 * falls back to D2's bundled font for that style: Source Sans Pro for the regular fonts, Source
 * Code Pro for the mono ones. The mono fonts require D2 0.7.1 or newer.
 *
 * These can't be set through Gradle properties.
 */
@AtlasDsl
public interface D2FontsSpec {
  public val regular: RegularFileProperty
  public val italic: RegularFileProperty
  public val bold: RegularFileProperty
  public val semibold: RegularFileProperty
  public val mono: RegularFileProperty
  public val monoBold: RegularFileProperty
  public val monoItalic: RegularFileProperty
  public val monoSemibold: RegularFileProperty
}

/**
 * Replaces individual colors of a theme, each a named CSS color like "orange" or a hex code like
 * "#f0ff3a". Any left unset keep the theme's own color. See
 * [the D2 docs](https://d2lang.com/tour/themes/).
 */
@AtlasDsl
public interface D2ThemeOverridesSpec : PropertiesSpec {
  /**
   * Neutral colors, from darkest ([n1]) to lightest ([n7]) in the light themes.
   *
   * Also controlled by the `atlas.d2.themeOverrides.n1` and `atlas.d2.themeDarkOverrides.n1` Gradle
   * properties.
   */
  public var n1: String?

  /**
   * Also controlled by the `atlas.d2.themeOverrides.n2` and `atlas.d2.themeDarkOverrides.n2` Gradle
   * properties.
   */
  public var n2: String?

  /**
   * Also controlled by the `atlas.d2.themeOverrides.n3` and `atlas.d2.themeDarkOverrides.n3` Gradle
   * properties.
   */
  public var n3: String?

  /**
   * Also controlled by the `atlas.d2.themeOverrides.n4` and `atlas.d2.themeDarkOverrides.n4` Gradle
   * properties.
   */
  public var n4: String?

  /**
   * Also controlled by the `atlas.d2.themeOverrides.n5` and `atlas.d2.themeDarkOverrides.n5` Gradle
   * properties.
   */
  public var n5: String?

  /**
   * Also controlled by the `atlas.d2.themeOverrides.n6` and `atlas.d2.themeDarkOverrides.n6` Gradle
   * properties.
   */
  public var n6: String?

  /**
   * Also controlled by the `atlas.d2.themeOverrides.n7` and `atlas.d2.themeDarkOverrides.n7` Gradle
   * properties.
   */
  public var n7: String?

  /**
   * Base colors, used for containers.
   *
   * Also controlled by the `atlas.d2.themeOverrides.b1` and `atlas.d2.themeDarkOverrides.b1` Gradle
   * properties.
   */
  public var b1: String?

  /**
   * Also controlled by the `atlas.d2.themeOverrides.b2` and `atlas.d2.themeDarkOverrides.b2` Gradle
   * properties.
   */
  public var b2: String?

  /**
   * Also controlled by the `atlas.d2.themeOverrides.b3` and `atlas.d2.themeDarkOverrides.b3` Gradle
   * properties.
   */
  public var b3: String?

  /**
   * Also controlled by the `atlas.d2.themeOverrides.b4` and `atlas.d2.themeDarkOverrides.b4` Gradle
   * properties.
   */
  public var b4: String?

  /**
   * Also controlled by the `atlas.d2.themeOverrides.b5` and `atlas.d2.themeDarkOverrides.b5` Gradle
   * properties.
   */
  public var b5: String?

  /**
   * Also controlled by the `atlas.d2.themeOverrides.b6` and `atlas.d2.themeDarkOverrides.b6` Gradle
   * properties.
   */
  public var b6: String?

  /**
   * Alternative colors A.
   *
   * Also controlled by the `atlas.d2.themeOverrides.aa2` and `atlas.d2.themeDarkOverrides.aa2`
   * Gradle properties.
   */
  public var aa2: String?

  /**
   * Also controlled by the `atlas.d2.themeOverrides.aa4` and `atlas.d2.themeDarkOverrides.aa4`
   * Gradle properties.
   */
  public var aa4: String?

  /**
   * Also controlled by the `atlas.d2.themeOverrides.aa5` and `atlas.d2.themeDarkOverrides.aa5`
   * Gradle properties.
   */
  public var aa5: String?

  /**
   * Alternative colors B.
   *
   * Also controlled by the `atlas.d2.themeOverrides.ab4` and `atlas.d2.themeDarkOverrides.ab4`
   * Gradle properties.
   */
  public var ab4: String?

  /**
   * Also controlled by the `atlas.d2.themeOverrides.ab5` and `atlas.d2.themeDarkOverrides.ab5`
   * Gradle properties.
   */
  public var ab5: String?
}

/**
 * Use this for any arbitrary global properties that you want to apply to all charts. These
 * properties will be appended to the bottom of the chart. Chances are, you'll want to use some
 * [globs](https://d2lang.com/tour/globs/) in here, so make sure to read the D2 docs on those.
 */
@AtlasDsl
public interface D2GlobalPropsSpec : PropertiesSpec {
  /**
   * The shape of the arrowhead on every link.
   *
   * Also controlled by the `atlas.d2.globalProps.arrowType` Gradle property.
   */
  public var arrowType: ArrowType?

  /**
   * Whether arrowheads on every link are filled in.
   *
   * Also controlled by the `atlas.d2.globalProps.fillArrowHeads` Gradle property.
   */
  public var fillArrowHeads: Boolean?

  /**
   * The font of all text in the chart. [Font.Mono] is the only option, so leave this unset for D2's
   * default font.
   *
   * Also controlled by the `atlas.d2.globalProps.font` Gradle property.
   */
  public var font: Font?

  /**
   * The size of all text in the chart.
   *
   * Also controlled by the `atlas.d2.globalProps.fontSize` Gradle property.
   */
  public var fontSize: Int?
}
