package atlas.d2.internal

import atlas.core.internal.InternalPropertiesSpec
import atlas.core.internal.PropertiesSpecImpl
import atlas.core.internal.bool
import atlas.core.internal.child
import atlas.core.internal.enum
import atlas.core.internal.float
import atlas.core.internal.int
import atlas.core.internal.intEnum
import atlas.core.internal.longList
import atlas.core.internal.string
import atlas.d2.ArrowType
import atlas.d2.D2DagreSpec
import atlas.d2.D2ElkSpec
import atlas.d2.D2FontsSpec
import atlas.d2.D2GlobalPropsSpec
import atlas.d2.D2LayoutEngineSpec
import atlas.d2.D2RootStyleSpec
import atlas.d2.D2Spec
import atlas.d2.D2TalaSpec
import atlas.d2.D2ThemeOverridesSpec
import atlas.d2.ElkAlgorithm
import atlas.d2.FillPattern
import atlas.d2.Font
import atlas.d2.LayoutEngine.Dagre
import atlas.d2.LayoutEngine.Elk
import atlas.d2.LayoutEngine.Tala
import org.gradle.api.Action
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.ProviderFactory

internal class D2SpecImpl(
  objects: ObjectFactory,
  providers: ProviderFactory,
) : D2Spec {
  internal val properties = D2GradleProperties(providers)

  override val name = "D2"
  override val fileExtension = objects.string(convention = "d2")

  override val animateLinks = objects.bool(properties.animateLinks)
  override val animateInterval = objects.int(properties.animateInterval)
  override val asciiMode = objects.enum(properties.asciiMode)
  override val center = objects.bool(properties.center)
  override val direction = objects.enum(properties.direction)
  override val executable: RegularFileProperty = objects.fileProperty()
  override val executableSource = objects.enum(properties.executableSource)
  override val fileFormat = objects.enum(properties.fileFormat)
  override val groupLabelLocation = objects.enum(properties.groupLabelLocation)
  override val groupLabelPosition = objects.enum(properties.groupLabelPosition)
  override val intermediateFilesInBuildDir = objects.bool(properties.intermediateFilesInBuildDir)
  override val noXmlTag = objects.bool(properties.noXmlTag)
  override val omitVersion = objects.bool(properties.omitVersion)
  override val pad = objects.int(properties.pad)
  override val scale = objects.float(properties.scale)
  override val sketch = objects.bool(properties.sketch)
  override val theme = objects.intEnum(properties.theme)
  override val themeDark = objects.intEnum(properties.darkTheme)
  override val timeout = objects.int(properties.timeout)
  override val version = objects.string(properties.version)

  override val fonts: D2FontsSpec = objects.newInstance(D2FontsSpec::class.java)

  override fun fonts(action: Action<D2FontsSpec>) = action.execute(fonts)

  override val layoutEngine = D2LayoutEngineSpecImpl(objects, providers, properties)

  override fun layoutEngine(action: Action<D2LayoutEngineSpec>) = action.execute(layoutEngine)

  override val rootStyle = D2RootStyleSpecImpl(objects, providers)

  override fun rootStyle(action: Action<D2RootStyleSpec>) = action.execute(rootStyle)

  override val themeOverrides =
    D2ThemeOverridesSpecImpl(objects, providers, "atlas.d2.themeOverrides")

  override fun themeOverrides(action: Action<D2ThemeOverridesSpec>) = action.execute(themeOverrides)

  override val themeDarkOverrides =
    D2ThemeOverridesSpecImpl(objects, providers, "atlas.d2.themeDarkOverrides")

  override fun themeDarkOverrides(action: Action<D2ThemeOverridesSpec>) =
    action.execute(themeDarkOverrides)

  override val globalProps = D2GlobalPropsSpecImpl(objects, providers)

  override fun globalProps(action: Action<D2GlobalPropsSpec>) = action.execute(globalProps)
}

internal open class D2RootStyleSpecImpl(objects: ObjectFactory, providers: ProviderFactory) :
  D2RootStyleSpec,
  InternalPropertiesSpec by PropertiesSpecImpl(objects, providers, "atlas.d2.rootStyle") {
  override var fill by string("fill")
  override var fillPattern by enum<FillPattern>("fill-pattern")
  override var stroke by string("stroke")
  override var strokeWidth by int("stroke-width")
  override var strokeDash by int("stroke-dash")
  override var doubleBorder by bool("double-border")
}

internal open class D2ThemeOverridesSpecImpl(
  objects: ObjectFactory,
  providers: ProviderFactory,
  prefix: String,
) : D2ThemeOverridesSpec, InternalPropertiesSpec by PropertiesSpecImpl(objects, providers, prefix) {
  override var n1 by string("N1")
  override var n2 by string("N2")
  override var n3 by string("N3")
  override var n4 by string("N4")
  override var n5 by string("N5")
  override var n6 by string("N6")
  override var n7 by string("N7")
  override var b1 by string("B1")
  override var b2 by string("B2")
  override var b3 by string("B3")
  override var b4 by string("B4")
  override var b5 by string("B5")
  override var b6 by string("B6")
  override var aa2 by string("AA2")
  override var aa4 by string("AA4")
  override var aa5 by string("AA5")
  override var ab4 by string("AB4")
  override var ab5 by string("AB5")
}

internal open class D2GlobalPropsSpecImpl(objects: ObjectFactory, providers: ProviderFactory) :
  D2GlobalPropsSpec,
  InternalPropertiesSpec by PropertiesSpecImpl(objects, providers, "atlas.d2.globalProps") {
  override var arrowType by enum<ArrowType>("(** -> **)[*].target-arrowhead.shape")
  override var fillArrowHeads by bool("(** -> **)[*].target-arrowhead.style.filled")
  override var font by enum<Font>("***.style.font")
  override var fontSize by int("***.style.font-size")
}

internal class D2LayoutEngineSpecImpl(
  objects: ObjectFactory,
  providers: ProviderFactory,
  gradleProperties: D2GradleProperties,
) :
  D2LayoutEngineSpec,
  InternalPropertiesSpec by PropertiesSpecImpl(objects, providers, "atlas.d2.layoutEngine") {
  override val layoutEngine = objects.enum(gradleProperties.layoutEngine)
  override val dagre = D2DagreSpecImpl(this)
  override val elk = D2ElkSpecImpl(this)
  override val tala = D2TalaSpecImpl(this)

  override fun dagre(config: Action<D2DagreSpec>?) {
    layoutEngine.set(Dagre)
    config?.execute(dagre)
  }

  override fun elk(config: Action<D2ElkSpec>?) {
    layoutEngine.set(Elk)
    config?.execute(elk)
  }

  override fun tala(config: Action<D2TalaSpec>?) {
    layoutEngine.set(Tala)
    config?.execute(tala)
  }
}

internal class D2ElkSpecImpl(parent: InternalPropertiesSpec) :
  D2ElkSpec, InternalPropertiesSpec by parent.child("elk") {
  override var algorithm by enum<ElkAlgorithm>("elk-algorithm")
  override var edgeNodeBetweenLayers by int("elk-edgeNodeBetweenLayers")
  override var nodeNodeBetweenLayers by int("elk-nodeNodeBetweenLayers")
  override var nodeSelfLoop by int("elk-nodeSelfLoop")
  override var padding by string("elk-padding")

  override fun padding(all: Int): Unit = padding(top = all, left = all, bottom = all, right = all)

  override fun padding(horizontal: Int, vertical: Int): Unit =
    padding(top = vertical, left = horizontal, bottom = vertical, right = horizontal)

  override fun padding(top: Int, left: Int, bottom: Int, right: Int) {
    padding = "[top=$top,left=$left,bottom=$bottom,right=$right]"
  }
}

internal class D2DagreSpecImpl(parent: InternalPropertiesSpec) :
  D2DagreSpec, InternalPropertiesSpec by parent.child("dagre") {
  override var nodeSep by int("dagre-nodesep")
  override var edgeSep by int("dagre-edgesep")
}

internal class D2TalaSpecImpl(parent: InternalPropertiesSpec) :
  D2TalaSpec, InternalPropertiesSpec by parent.child("tala") {
  override var seeds by longList("tala-seeds")
}
