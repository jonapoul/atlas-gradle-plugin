@file:Suppress("unused", "SpellCheckingInspection") // public API

package atlas.graphviz

import atlas.core.StringEnum

/**
 * The default style applied to every edge from [EdgeAttributes.linkStyle]. Individual link types
 * use [atlas.core.LinkStyle] instead, since that's shared with the other frameworks.
 *
 * See [https://graphviz.org/docs/attr-types/style/](https://graphviz.org/docs/attr-types/style/)
 */
public enum class EdgeStyle(override val value: String) : StringEnum {
  Bold("bold"),
  Dashed("dashed"),
  Dotted("dotted"),
  Invis("invis"),
  Solid("solid"),
  Tapered("tapered");

  override fun toString(): String = value
}

/**
 * See [https://graphviz.org/docs/attr-types/style/](https://graphviz.org/docs/attr-types/style/)
 */
public enum class NodeStyle(override val value: String) : StringEnum {
  Dashed("dashed"),
  Dotted("dotted"),
  Solid("solid"),
  Invis("invis"),
  Bold("bold"),
  Filled("filled"),
  Striped("striped"),
  Wedged("wedged"),
  Diagonals("diagonals"),
  Rounded("rounded"),
  Radial("radial");

  override fun toString(): String = value
}

/**
 * See
 * [https://graphviz.org/docs/attr-types/rankdir/](https://graphviz.org/docs/attr-types/rankdir/)
 */
public enum class RankDir(override val value: String) : StringEnum {
  TopToBottom("TB"),
  BottomToTop("BT"),
  LeftToRight("LR"),
  RightToLeft("RL");

  override fun toString(): String = value
}

/**
 * See
 * [https://graphviz.org/docs/attr-types/arrowType/](https://graphviz.org/docs/attr-types/arrowType/)
 */
public enum class ArrowType(override val value: String) : StringEnum {
  Box("box"),
  Crow("crow"),
  Diamond("diamond"),
  Dot("dot"),
  Ediamond("ediamond"),
  Empty("empty"),
  HalfOpen("halfopen"),
  Inv("inv"),
  InvEmpty("invempty"),
  Invdot("invdot"),
  Invodot("invodot"),
  None("none"),
  Normal("normal"),
  Obox("obox"),
  Odiamond("odiamond"),
  Odot("odot"),
  Open("open"),
  Tee("tee"),
  Vee("vee");

  override fun toString(): String = value
}

/**
 * See [https://graphviz.org/docs/layouts/](https://graphviz.org/docs/layouts/)
 *
 * [Dot] is the implicit default. Run `dot -v` and check under "layout" to see which of these you
 * have locally.
 */
public enum class LayoutEngine(override val value: String) : StringEnum {
  Dot("dot"),
  Neato("neato"),
  Fdp("fdp"),
  Sfdp("sfdp"),
  Circo("circo"),
  TwoPi("twopi"),
  Nop("nop"),
  Nop2("nop2"),
  Osage("osage"),
  Patchwork("patchwork");

  override fun toString(): String = value
}

/** See [https://graphviz.org/docs/attrs/dir/](https://graphviz.org/docs/attrs/dir/) */
public enum class Dir(override val value: String) : StringEnum {
  Forward("forward"),
  Back("back"),
  Both("both"),
  None("none");

  override fun toString(): String = value
}

/** See [https://graphviz.org/docs/attrs/imagepos/](https://graphviz.org/docs/attrs/imagepos/) */
public enum class ImagePos(override val value: String) : StringEnum {
  TopLeft("tl"),
  TopCenter("tc"),
  TopRight("tr"),
  MiddleLeft("ml"),
  MiddleCenter("mc"),
  MiddleRight("mr"),
  BottomLeft("bl"),
  BottomCenter("bc"),
  BottomRight("br");

  override fun toString(): String = value
}

/**
 * See
 * [https://graphviz.org/docs/attr-types/clusterMode/](https://graphviz.org/docs/attr-types/clusterMode/)
 */
public enum class ClusterMode(override val value: String) : StringEnum {
  Local("local"),
  Global("global"),
  None("none");

  override fun toString(): String = value
}

/**
 * See
 * [https://graphviz.org/docs/attr-types/smoothType/](https://graphviz.org/docs/attr-types/smoothType/)
 */
public enum class SmoothType(override val value: String) : StringEnum {
  None("none"),
  AvgDist("avg_dist"),
  GraphDist("graph_dist"),
  PowerDist("power_dist"),
  Rng("rng"),
  Spring("spring"),
  Triangle("triangle");

  override fun toString(): String = value
}

/**
 * The formats supported on your machine will depend on the version of Graphviz you use. See
 * [https://graphviz.org/docs/outputs/](https://graphviz.org/docs/outputs/)
 */
public enum class FileFormat(override val value: String) : StringEnum {
  Canon("canon"),
  Cmap("cmap"),
  Cmapx("cmapx"),
  CmapxNp("cmapx_np"),
  Dot("dot"),
  DotJson("dot_json"),
  Eps("eps"),
  Fig("fig"),
  Gd("gd"),
  Gd2("gd2"),
  Gif("gif"),
  Gv("gv"),
  Imap("imap"),
  ImapNp("imap_np"),
  Ismap("ismap"),
  Jpe("jpe"),
  Jpeg("jpeg"),
  Jpg("jpg"),
  Json("json"),
  Json0("json0"),
  Mp("mp"),
  Pdf("pdf"),
  Pic("pic"),
  Plain("plain"),
  PlainExt("plain-ext"),
  Png("png"),
  Pov("pov"),
  Ps("ps"),
  Ps2("ps2"),
  Svg("svg"),
  Svgz("svgz"),
  Tk("tk"),
  Vdx("vdx"),
  Vml("vml"),
  Vmlz("vmlz"),
  Vrml("vrml"),
  Wbmp("wbmp"),
  Webp("webp"),
  X11("x11"),
  Xdot("xdot"),
  Xdot12("xdot1.2"),
  Xdot14("xdot1.4"),
  XdotJson("xdot_json"),
  Xlib("xlib");

  override fun toString(): String = value
}

/** See [https://graphviz.org/doc/info/shapes.html](https://graphviz.org/doc/info/shapes.html) */
public enum class Shape(override val value: String) : StringEnum {
  Box("box"),
  Polygon("polygon"),
  Ellipse("ellipse"),
  Oval("oval"),
  Circle("circle"),
  Point("point"),
  Egg("egg"),
  Triangle("triangle"),
  Plaintext("plaintext"),
  Plain("plain"),
  Diamond("diamond"),
  Trapezium("trapezium"),
  Parallelogram("parallelogram"),
  House("house"),
  Pentagon("pentagon"),
  Hexagon("hexagon"),
  Septagon("septagon"),
  Octagon("octagon"),
  Doublecircle("doublecircle"),
  Doubleoctagon("doubleoctagon"),
  Tripleoctagon("tripleoctagon"),
  Invtriangle("invtriangle"),
  Invtrapezium("invtrapezium"),
  Invhouse("invhouse"),
  Mdiamond("mdiamond"),
  Msquare("msquare"),
  Mcircle("mcircle"),
  Rect("rect"),
  Rectangle("rectangle"),
  Square("square"),
  Star("star"),
  None("none"),
  Underline("underline"),
  Cylinder("cylinder"),
  Note("note"),
  Tab("tab"),
  Folder("folder"),
  Box3d("box3d"),
  Component("component"),
  Promoter("promoter"),
  Cds("cds"),
  Terminator("terminator"),
  Utr("utr"),
  Primersite("primersite"),
  Restrictionsite("restrictionsite"),
  Fivepoverhang("fivepoverhang"),
  Threepoverhang("threepoverhang"),
  Noverhang("noverhang"),
  Assembly("assembly"),
  Signature("signature"),
  Insulator("insulator"),
  Ribosite("ribosite"),
  Rnastab("rnastab"),
  Proteasesite("proteasesite"),
  Proteinstab("proteinstab"),
  Rpromoter("rpromoter"),
  Rarrow("rarrow"),
  Larrow("larrow"),
  Lpromoter("lpromoter");

  override fun toString(): String = value
}
