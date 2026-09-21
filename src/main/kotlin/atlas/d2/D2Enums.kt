@file:Suppress("unused", "MagicNumber") // public API

package atlas.d2

import atlas.core.IntEnum
import atlas.core.StringEnum

/** See [https://d2lang.com/tour/shapes/](https://d2lang.com/tour/shapes/) */
public enum class Shape(override val value: String) : StringEnum {
  Rectangle("rectangle"),
  Square("square"),
  Page("page"),
  Parallelogram("parallelogram"),
  Document("document"),
  Cylinder("cylinder"),
  Queue("queue"),
  Package("package"),
  Step("step"),
  Callout("callout"),
  StoredData("stored_data"),
  Person("person"),
  Diamond("diamond"),
  Oval("oval"),
  Circle("circle"),
  Hexagon("hexagon"),
  Cloud("cloud"),
  C4Person("c4-person");

  override fun toString(): String = value
}

/** See [https://d2lang.com/tour/layouts/](https://d2lang.com/tour/layouts/) */
public enum class LayoutEngine(override val value: String) : StringEnum {
  /**
   * See [the D2 docs](https://d2lang.com/tour/dagre/) for official documentation, and [D2DagreSpec]
   * for Atlas configuration of it.
   */
  Dagre("dagre"),

  /**
   * See [the D2 docs](https://d2lang.com/tour/elk/) for official documentation, and [D2ElkSpec] for
   * Atlas configuration of it.
   */
  Elk("elk"),

  /**
   * See [the D2 docs](https://d2lang.com/tour/tala/) for official documentation, and [D2TalaSpec]
   * for Atlas configuration of it.
   */
  Tala("tala");

  override fun toString(): String = value
}

/** [https://d2lang.com/tour/themes/](https://d2lang.com/tour/themes/) */
public enum class Theme(override val value: Int) : IntEnum {
  Default(0),
  NeutralGrey(1),
  FlagshipTerrastruct(3),
  CoolClassics(4),
  MixedBerryBlue(5),
  GrapeSoda(6),
  Aubergine(7),
  ColorblindClear(8),
  VanillaNitroCola(100),
  OrangeCreamsicle(101),
  ShirleyTemple(102),
  EarthTones(103),
  EvergladeGreen(104),
  ButteredToast(105),
  Terminal(300),
  TerminalGrayscale(301),
  Origami(302),
  C4(303),

  // These two are intended for configuration as dark themes
  DarkMauve(200),
  DarkFlagshipTerrastruct(201),
}

/** [https://d2lang.com/tour/exports/](https://d2lang.com/tour/exports/) */
public enum class FileFormat(override val value: String) : StringEnum {
  Svg("svg"),
  Png("png"),
  Pdf("pdf"),
  Pptx("pptx"),
  Gif("gif"),
  Ascii("txt");

  override fun toString(): String = value
}

/** [https://d2lang.com/tour/layouts/#direction](https://d2lang.com/tour/layouts/#direction) */
public enum class Direction(override val value: String) : StringEnum {
  Up("up"),
  Down("down"),
  Right("right"),
  Left("left");

  override fun toString(): String = value
}

/** [https://d2lang.com/tour/style/#fill-pattern](https://d2lang.com/tour/style/#fill-pattern) */
public enum class FillPattern(override val value: String) : StringEnum {
  Dots("dots"),
  Lines("lines"),
  Grain("grain"),
  None("none");

  override fun toString(): String = value
}

/**
 * [https://d2lang.com/tour/connections/#arrowheads](https://d2lang.com/tour/connections/#arrowheads)
 */
public enum class ArrowType(override val value: String) : StringEnum {
  Triangle("triangle"),
  Arrow("arrow"),
  Diamond("diamond"),
  Circle("circle"),
  Box("box"),
  CrowsFootOne("cf-one"),
  CrowsFootOneRequired("cf-one-required"),
  CrowsFootMany("cf-many"),
  CrowsFootManyRequired("cf-many-required"),
  Cross("cross");

  override fun toString(): String = value
}

/** [https://d2lang.com/tour/positions/](https://d2lang.com/tour/positions/) */
public enum class Position(override val value: String) : StringEnum {
  TopLeft("top-left"),
  TopCenter("top-center"),
  TopRight("top-right"),
  CenterLeft("center-left"),
  CenterRight("center-right"),
  BottomLeft("bottom-left"),
  BottomCenter("bottom-center"),
  BottomRight("bottom-right");

  override fun toString(): String = value
}

/**
 * [https://d2lang.com/tour/positions/#outside-and-border](https://d2lang.com/tour/positions/#outside-and-border)
 */
public enum class Location(override val value: String) : StringEnum {
  Border("border"),
  Inside("inside"),
  Outside("outside");

  override fun toString(): String = value
}

/** [https://d2lang.com/tour/style/#font](https://d2lang.com/tour/style/#font) */
public enum class Font(override val value: String) : StringEnum {
  Mono("mono");

  override fun toString(): String = value
}

/**
 * [https://d2lang.com/tour/style/#text-transform](https://d2lang.com/tour/style/#text-transform)
 */
public enum class TextTransform(override val value: String) : StringEnum {
  Uppercase("uppercase"),
  Lowercase("lowercase"),
  Title("title"),
  None("none");

  override fun toString(): String = value
}

/**
 * The subset of [ELK's algorithms](https://eclipse.dev/elk/reference/algorithms.html) that D2's
 * bundled ELK can actually lay out a dependency chart with.
 *
 * The rest of ELK's catalogue is left out because it doesn't work here: `disco` was dropped when D2
 * 0.8.2 replaced ELK.js with the native elk-go port, `topdownpacking`, `vertiflex` and the
 * `graphviz.*` family have never been bundled, and the packing algorithms (`box`, `fixed`,
 * `rectpacking`) crash on any graph containing connections.
 */
public enum class ElkAlgorithm(override val value: String) : StringEnum {
  Force("force"),
  Layered("layered"),
  MrTree("mrtree"),
  Radial("radial"),
  Random("random"),
  SporeCompaction("sporeCompaction"),
  SporeOverlap("sporeOverlap"),
  Stress("stress");

  override fun toString(): String = value
}

/** Which characters D2 draws [FileFormat.Ascii] charts with. */
public enum class AsciiMode(override val value: String) : StringEnum {
  /** Basic ASCII characters only, e.g. `+`, `-` and `|`. */
  Standard("standard"),

  /** Unicode box-drawing characters. D2's default. */
  Extended("extended");

  override fun toString(): String = value
}

/** Where Atlas finds the `d2` executable when [D2Spec.d2Executable] isn't set. */
public enum class ExecutableSource(override val value: String) : StringEnum {
  /**
   * Use `d2` from the system PATH if it's there, otherwise download it. If [D2Spec.d2Version] is
   * set, always download that version instead.
   */
  Auto("auto"),

  /** Only ever use `d2` from the system PATH, and never touch the network. */
  System("system"),

  /**
   * Always download D2, ignoring the system PATH. Uses [D2Spec.d2Version] if set. Best for CI, or
   * anywhere the output is diffed, since every machine then renders with the same version.
   */
  Download("download");

  override fun toString(): String = value
}
