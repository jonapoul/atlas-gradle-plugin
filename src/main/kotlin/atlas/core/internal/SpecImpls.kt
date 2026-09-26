package atlas.core.internal

import atlas.core.AtlasExtension
import atlas.core.Framework
import atlas.core.Framework.D2
import atlas.core.Framework.Graphviz
import atlas.core.Framework.Mermaid
import atlas.core.LinkStyle
import atlas.core.LinkTypeSpec
import atlas.core.NamedLinkTypeContainer
import atlas.core.NamedProjectTypeContainer
import atlas.core.PathTransformSpec
import atlas.core.ProjectTypeSpec
import atlas.core.Replacement
import atlas.d2.D2Spec
import atlas.d2.FillPattern
import atlas.d2.Font
import atlas.d2.Shape as D2Shape
import atlas.d2.TextTransform
import atlas.d2.internal.D2SpecImpl
import atlas.graphviz.ArrowType
import atlas.graphviz.Dir
import atlas.graphviz.GraphvizSpec
import atlas.graphviz.ImagePos
import atlas.graphviz.NodeStyle
import atlas.graphviz.Shape as GraphvizShape
import atlas.graphviz.internal.GraphvizSpecImpl
import atlas.mermaid.MermaidSpec
import atlas.mermaid.internal.MermaidSpecImpl
import javax.inject.Inject
import org.gradle.api.Action
import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.MapProperty
import org.gradle.api.provider.Property
import org.gradle.api.provider.ProviderFactory
import org.gradle.api.provider.SetProperty

internal open class AtlasExtensionImpl
@Inject
constructor(
  objects: ObjectFactory,
  providers: ProviderFactory,
) : AtlasExtension {
  private val coreProperties = CoreGradleProperties(providers)
  private val mutableFrameworks = linkedSetOf<Framework>()

  override val alsoTraverseUpwards: Property<Boolean> =
    objects.bool(coreProperties.alsoTraverseUpwards)
  override val checkOutputs: Property<Boolean> = objects.bool(coreProperties.checkOutputs)
  override val displayLinkLabels: Property<Boolean> = objects.bool(coreProperties.displayLinkLabels)
  override val generateOnSync: Property<Boolean> = objects.bool(coreProperties.generateOnSync)
  override val groupProjects: Property<Boolean> = objects.bool(coreProperties.groupProjects)
  override val ignoredConfigs: SetProperty<String> =
    objects.set(convention = setOf("debug", "kover", "ksp", "test"))
  override val ignoredProjects: SetProperty<Regex> = objects.set(convention = emptySet())
  override val printFilesToConsole: Property<Boolean> =
    objects.bool(coreProperties.printFilesToConsole)

  override val pathTransforms: PathTransformSpecImpl = PathTransformSpecImpl(objects)

  override fun pathTransforms(action: Action<PathTransformSpec>): Unit =
    action.execute(pathTransforms)

  override val projectTypes: ProjectTypeContainer = ProjectTypeContainer(objects)

  override fun projectTypes(action: Action<NamedProjectTypeContainer>): Unit =
    action.execute(projectTypes)

  override val linkTypes: LinkTypeContainer = LinkTypeContainer(objects)

  override fun linkTypes(action: Action<NamedLinkTypeContainer>): Unit = action.execute(linkTypes)

  override val frameworks: Set<Framework>
    get() = mutableFrameworks.toSet()

  // Atlas reads these rather than the public getters, which switch the framework on
  internal val d2Spec = D2SpecImpl(objects, providers)
  internal val graphvizSpec = GraphvizSpecImpl(objects, providers)
  internal val mermaidSpec = MermaidSpecImpl(objects, providers)

  override val d2: D2SpecImpl
    get() = d2Spec.also { d2() }

  override fun d2(action: Action<D2Spec>) {
    d2()
    action.execute(d2Spec)
  }

  override fun d2() {
    mutableFrameworks += D2
  }

  override val graphviz: GraphvizSpecImpl
    get() = graphvizSpec.also { graphviz() }

  override fun graphviz(action: Action<GraphvizSpec>) {
    graphviz()
    action.execute(graphvizSpec)
  }

  override fun graphviz() {
    mutableFrameworks += Graphviz
  }

  override val mermaid: MermaidSpecImpl
    get() = mermaidSpec.also { mermaid() }

  override fun mermaid(action: Action<MermaidSpec>) {
    mermaid()
    action.execute(mermaidSpec)
  }

  override fun mermaid() {
    mutableFrameworks += Mermaid
  }

  internal fun enableFrameworksFromGradleProperties(providers: ProviderFactory) {
    mutableFrameworks +=
      Framework.entries.filter { framework ->
        providers.gradlePropertiesPrefixedBy("atlas.$framework.").get().isNotEmpty()
      }
  }

  internal companion object {
    internal const val NAME: String = "atlas"
  }
}

internal class PathTransformSpecImpl(objects: ObjectFactory) : PathTransformSpec {
  override val replacements: SetProperty<Replacement> = objects.setProperty(Replacement::class.java)

  override fun replace(pattern: Regex, replacement: String): Unit =
    replacements.add(Replacement(pattern, replacement))

  override fun replace(pattern: String, replacement: String): Unit =
    replace(pattern.toRegex(), replacement)

  override fun remove(pattern: Regex): Unit = replace(pattern, replacement = "")

  override fun remove(pattern: String): Unit = remove(pattern.toRegex())
}

@Suppress("VariableNaming")
internal abstract class ProjectTypeSpecImpl
@Inject
constructor(
  override val name: String,
  objects: ObjectFactory,
) : ProjectTypeSpec {
  internal val styleProperties: StyleProperties = StyleProperties(objects)

  abstract override val color: Property<String>
  abstract override val pathContains: Property<String>
  abstract override val pathMatches: Property<String>
  abstract override val hasPluginId: Property<String>

  init {
    color.unsetConvention()
    pathContains.unsetConvention()
    pathMatches.unsetConvention()
    regexOptions.unsetConvention()
    hasPluginId.unsetConvention()
  }

  override fun properties(framework: Framework): MapProperty<String, String> =
    styleProperties.properties(framework)

  override fun clear(): Unit = styleProperties.clear()

  override fun clear(framework: Framework): Unit = styleProperties.clear(framework)

  // Read by every framework
  override var fill by
    styleProperties.string(D2 to "style.fill", Graphviz to "fillcolor", Mermaid to "fill")
  override var stroke by
    styleProperties.string(D2 to "style.stroke", Graphviz to "color", Mermaid to "stroke")
  override var strokeWidth by
    styleProperties.string(
      D2 to "style.stroke-width",
      Graphviz to "penwidth",
      Mermaid to "stroke-width",
    )
  override var fontColor by
    styleProperties.string(
      D2 to "style.font-color",
      Graphviz to "fontcolor",
      Mermaid to "color",
    )
  override var fontSize by
    styleProperties.string(
      D2 to "style.font-size",
      Graphviz to "fontsize",
      Mermaid to "font-size",
    )

  // D2 and Mermaid
  override var opacity by styleProperties.float(D2 to "style.opacity", Mermaid to "opacity")

  // D2 only
  override var animated by styleProperties.bool(D2 to "style.animated")
  override var bold by styleProperties.bool(D2 to "style.bold")
  override var borderRadius by styleProperties.int(D2 to "style.border-radius")
  override var doubleBorder by styleProperties.bool(D2 to "style.double-border")
  override var fillPattern by styleProperties.enum<FillPattern>(D2 to "style.fill-pattern")
  override var font by styleProperties.enum<Font>(D2 to "style.font")
  override var italic by styleProperties.bool(D2 to "style.italic")
  override var multiple by styleProperties.bool(D2 to "style.multiple")
  override var render3D by styleProperties.bool(D2 to "style.3d")
  override var shadow by styleProperties.bool(D2 to "style.shadow")
  override var d2Shape by styleProperties.enum<D2Shape>(D2 to "shape")
  override var strokeDash by styleProperties.int(D2 to "style.stroke-dash")
  override var textTransform by styleProperties.enum<TextTransform>(D2 to "style.text-transform")
  override var underline by styleProperties.bool(D2 to "style.underline")

  // Mermaid only
  override var strokeDashArray by styleProperties.string(Mermaid to "stroke-dasharray")

  // Graphviz only
  override var graphvizShape by styleProperties.enum<GraphvizShape>(Graphviz to "shape")
  override var colorScheme by styleProperties.string(Graphviz to "colorscheme")
  override var comment by styleProperties.string(Graphviz to "comment")
  override var distortion by styleProperties.string(Graphviz to "distortion")
  override var fixedSize by styleProperties.string(Graphviz to "fixedsize")
  override var fontName by styleProperties.string(Graphviz to "fontname")
  override var gradientAngle by styleProperties.int(Graphviz to "gradientangle")
  override var group by styleProperties.string(Graphviz to "group")
  override var height by styleProperties.number(Graphviz to "height")
  override var href by styleProperties.string(Graphviz to "href")
  override var id by styleProperties.string(Graphviz to "id")
  override var image by styleProperties.string(Graphviz to "image")
  override var imagePos by styleProperties.enum<ImagePos>(Graphviz to "imagepos")
  override var imageScale by styleProperties.string(Graphviz to "imagescale")
  override var label by styleProperties.string(Graphviz to "label")
  override var labelLoc by styleProperties.string(Graphviz to "labelloc")
  override var layer by styleProperties.string(Graphviz to "layer")
  override var margin by styleProperties.string(Graphviz to "margin")
  override var noJustify by styleProperties.bool(Graphviz to "nojustify")
  override var ordering by styleProperties.string(Graphviz to "ordering")
  override var orientation by styleProperties.number(Graphviz to "orientation")
  override var peripheries by styleProperties.int(Graphviz to "peripheries")
  override var pin by styleProperties.bool(Graphviz to "pin")
  override var pos by styleProperties.string(Graphviz to "pos")
  override var rects by styleProperties.string(Graphviz to "rects")
  override var regular by styleProperties.bool(Graphviz to "regular")
  override var root by styleProperties.string(Graphviz to "root")
  override var samplePoints by styleProperties.int(Graphviz to "samplepoints")
  override var shapeFile by styleProperties.string(Graphviz to "shapefile")
  override var showBoxes by styleProperties.int(Graphviz to "showboxes")
  override var sides by styleProperties.int(Graphviz to "sides")
  override var skew by styleProperties.number(Graphviz to "skew")
  override var sortv by styleProperties.int(Graphviz to "sortv")
  override var style by styleProperties.enum<NodeStyle>(Graphviz to "style")
  override var target by styleProperties.string(Graphviz to "target")
  override var tooltip by styleProperties.string(Graphviz to "tooltip")
  override var url by styleProperties.string(Graphviz to "URL")
  override var vertices by styleProperties.string(Graphviz to "vertices")
  override var width by styleProperties.number(Graphviz to "width")
  override var xlabel by styleProperties.string(Graphviz to "xlabel")
  override var xlp by styleProperties.string(Graphviz to "xlp")
  override var z by styleProperties.number(Graphviz to "z")
}

@Suppress("VariableNaming")
internal abstract class LinkTypeSpecImpl
@Inject
constructor(
  override val name: String,
  objects: ObjectFactory,
) : LinkTypeSpec {
  internal val styleProperties: StyleProperties = StyleProperties(objects)

  abstract override val configuration: Property<String>
  abstract override val style: Property<LinkStyle>
  abstract override val color: Property<String>

  init {
    configuration.convention(name)
    style.unsetConvention()
    color.unsetConvention()
  }

  override fun properties(framework: Framework): MapProperty<String, String> =
    styleProperties.properties(framework)

  override fun clear(): Unit = styleProperties.clear()

  override fun clear(framework: Framework): Unit = styleProperties.clear(framework)

  // Read by every framework
  override var stroke by
    styleProperties.string(D2 to "style.stroke", Graphviz to "color", Mermaid to "stroke")
  override var strokeWidth by
    styleProperties.string(
      D2 to "style.stroke-width",
      Graphviz to "penwidth",
      Mermaid to "stroke-width",
    )
  override var fontColor by
    styleProperties.string(
      D2 to "style.font-color",
      Graphviz to "fontcolor",
      Mermaid to "color",
    )

  // D2 and Graphviz
  override var fontSize by styleProperties.string(D2 to "style.font-size", Graphviz to "fontsize")

  // D2 and Mermaid
  override var opacity by styleProperties.float(D2 to "style.opacity", Mermaid to "opacity")

  // D2 only
  override var animated by styleProperties.bool(D2 to "style.animated")
  override var bold by styleProperties.bool(D2 to "style.bold")
  override var borderRadius by styleProperties.int(D2 to "style.border-radius")
  override var font by styleProperties.enum<Font>(D2 to "style.font")
  override var italic by styleProperties.bool(D2 to "style.italic")
  override var strokeDash by styleProperties.int(D2 to "style.stroke-dash")
  override var textTransform by styleProperties.enum<TextTransform>(D2 to "style.text-transform")
  override var underline by styleProperties.bool(D2 to "style.underline")

  // Mermaid only
  override var strokeDashArray by styleProperties.string(Mermaid to "stroke-dasharray")

  // Graphviz only
  override var arrowHead by styleProperties.enum<ArrowType>(Graphviz to "arrowhead")
  override var arrowSize by styleProperties.number(Graphviz to "arrowsize")
  override var arrowTail by styleProperties.enum<ArrowType>(Graphviz to "arrowtail")
  override var colorScheme by styleProperties.string(Graphviz to "colorscheme")
  override var comment by styleProperties.string(Graphviz to "comment")
  override var constraint by styleProperties.bool(Graphviz to "constraint")
  override var decorate by styleProperties.bool(Graphviz to "decorate")
  override var dir by styleProperties.enum<Dir>(Graphviz to "dir")
  override var edgeHref by styleProperties.string(Graphviz to "edgehref")
  override var edgeTarget by styleProperties.string(Graphviz to "edgetarget")
  override var edgeTooltip by styleProperties.string(Graphviz to "edgetooltip")
  override var edgeUrl by styleProperties.string(Graphviz to "edgeURL")
  override var fillColor by styleProperties.string(Graphviz to "fillcolor")
  override var fontName by styleProperties.string(Graphviz to "fontname")
  override var headLp by styleProperties.string(Graphviz to "head_lp")
  override var headClip by styleProperties.bool(Graphviz to "headclip")
  override var headHref by styleProperties.string(Graphviz to "headhref")
  override var headLabel by styleProperties.string(Graphviz to "headlabel")
  override var headPort by styleProperties.string(Graphviz to "headport")
  override var headTarget by styleProperties.string(Graphviz to "headtarget")
  override var headTooltip by styleProperties.string(Graphviz to "headtooltip")
  override var headUrl by styleProperties.string(Graphviz to "headURL")
  override var href by styleProperties.string(Graphviz to "href")
  override var id by styleProperties.string(Graphviz to "id")
  override var label by styleProperties.string(Graphviz to "label")
  override var labelAngle by styleProperties.number(Graphviz to "labelangle")
  override var labelDistance by styleProperties.number(Graphviz to "labeldistance")
  override var labelFloat by styleProperties.bool(Graphviz to "labelfloat")
  override var labelFontColor by styleProperties.string(Graphviz to "labelfontcolor")
  override var labelFontName by styleProperties.string(Graphviz to "labelfontname")
  override var labelFontSize by styleProperties.string(Graphviz to "labelfontsize")
  override var labelHref by styleProperties.string(Graphviz to "labelhref")
  override var labelTarget by styleProperties.string(Graphviz to "labeltarget")
  override var labelTooltip by styleProperties.string(Graphviz to "labeltooltip")
  override var labelUrl by styleProperties.string(Graphviz to "labelURL")
  override var layer by styleProperties.string(Graphviz to "layer")
  override var len by styleProperties.number(Graphviz to "len")
  override var lhead by styleProperties.string(Graphviz to "lhead")
  override var lp by styleProperties.string(Graphviz to "lp")
  override var ltail by styleProperties.string(Graphviz to "ltail")
  override var minLen by styleProperties.int(Graphviz to "minlen")
  override var noJustify by styleProperties.bool(Graphviz to "nojustify")
  override var pos by styleProperties.string(Graphviz to "pos")
  override var sameHead by styleProperties.string(Graphviz to "samehead")
  override var sameTail by styleProperties.string(Graphviz to "sametail")
  override var showBoxes by styleProperties.int(Graphviz to "showboxes")
  override var tailLp by styleProperties.string(Graphviz to "tail_lp")
  override var tailClip by styleProperties.bool(Graphviz to "tailclip")
  override var tailHref by styleProperties.string(Graphviz to "tailhref")
  override var tailLabel by styleProperties.string(Graphviz to "taillabel")
  override var tailPort by styleProperties.string(Graphviz to "tailport")
  override var tailTarget by styleProperties.string(Graphviz to "tailtarget")
  override var tailTooltip by styleProperties.string(Graphviz to "tailtooltip")
  override var tailUrl by styleProperties.string(Graphviz to "tailURL")
  override var target by styleProperties.string(Graphviz to "target")
  override var tooltip by styleProperties.string(Graphviz to "tooltip")
  override var url by styleProperties.string(Graphviz to "URL")
  override var weight by styleProperties.number(Graphviz to "weight")
  override var xLabel by styleProperties.string(Graphviz to "xlabel")
  override var xlp by styleProperties.string(Graphviz to "xlp")
}
