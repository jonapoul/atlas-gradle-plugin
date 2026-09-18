package atlas.d2.internal

import atlas.core.PropertiesSpec
import atlas.core.internal.PropertiesSpecImpl
import atlas.core.internal.bool
import atlas.core.internal.enum
import atlas.core.internal.float
import atlas.core.internal.int
import atlas.core.internal.intEnum
import atlas.core.internal.longList
import atlas.core.internal.string
import atlas.d2.ArrowType
import atlas.d2.D2DagreSpec
import atlas.d2.D2ElkSpec
import atlas.d2.D2GlobalPropsSpec
import atlas.d2.D2LayoutEngineSpec
import atlas.d2.D2RootStyleSpec
import atlas.d2.D2Spec
import atlas.d2.D2TalaSpec
import atlas.d2.ElkAlgorithm
import atlas.d2.FillPattern
import atlas.d2.Font
import atlas.d2.LayoutEngine
import atlas.d2.LayoutEngine.Dagre
import atlas.d2.LayoutEngine.Elk
import atlas.d2.LayoutEngine.Tala
import org.gradle.api.Action
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
  override val center = objects.bool(properties.center)
  override val direction = objects.enum(properties.direction)
  override val fileFormat = objects.enum(properties.fileFormat)
  override val groupLabelLocation = objects.enum(properties.groupLabelLocation)
  override val groupLabelPosition = objects.enum(properties.groupLabelPosition)
  override val pad = objects.int(properties.pad)
  override val pathToD2Command = objects.string(properties.pathToD2Command)
  override val scale = objects.float(properties.scale)
  override val sketch = objects.bool(properties.sketch)
  override val theme = objects.intEnum(properties.theme)
  override val themeDark = objects.intEnum(properties.darkTheme)

  override val layoutEngine = D2LayoutEngineSpecImpl(objects)

  override fun layoutEngine(action: Action<D2LayoutEngineSpec>) = action.execute(layoutEngine)

  override val rootStyle = D2RootStyleSpecImpl(objects)

  override fun rootStyle(action: Action<D2RootStyleSpec>) = action.execute(rootStyle)

  override val globalProps = D2GlobalPropsSpecImpl(objects)

  override fun globalProps(action: Action<D2GlobalPropsSpec>) = action.execute(globalProps)
}

internal open class D2RootStyleSpecImpl(objects: ObjectFactory) :
  D2RootStyleSpec, PropertiesSpec by PropertiesSpecImpl(objects) {
  override var fill by string("fill")
  override var fillPattern by enum<FillPattern>("fill-pattern")
  override var stroke by string("stroke")
  override var strokeWidth by int("stroke-width")
  override var strokeDash by int("stroke-dash")
  override var doubleBorder by bool("double-border")
}

internal open class D2GlobalPropsSpecImpl(objects: ObjectFactory) :
  D2GlobalPropsSpec, PropertiesSpec by PropertiesSpecImpl(objects) {
  override var arrowType by enum<ArrowType>("(** -> **)[*].target-arrowhead.shape")
  override var fillArrowHeads by bool("(** -> **)[*].target-arrowhead.style.filled")
  override var font by enum<Font>("***.style.font")
  override var fontSize by int("***.style.font-size")
}

internal class D2LayoutEngineSpecImpl(objects: ObjectFactory) :
  D2LayoutEngineSpec, PropertiesSpec by PropertiesSpecImpl(objects) {
  override val layoutEngine = objects.enum<LayoutEngine>(convention = null)
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

internal class D2ElkSpecImpl(parent: PropertiesSpec) : D2ElkSpec, PropertiesSpec by parent {
  override var algorithm by enum<ElkAlgorithm>("elk-algorithm")
  override var edgeEdgeBetweenLayers by int("elk-edgeEdgeBetweenLayers")
  override var edgeNodeBetweenLayers by int("elk-edgeNodeBetweenLayers")
  override var nodeNodeBetweenLayers by int("elk-nodeNodeBetweenLayers")
  override var nodeSelfLoop by int("elk-nodeSelfLoop")
  override var padding by string("elk-padding")
}

internal class D2DagreSpecImpl(parent: PropertiesSpec) : D2DagreSpec, PropertiesSpec by parent {
  override var nodeSep by int("dagre-nodesep")
  override var edgeSep by int("dagre-edgesep")
}

internal class D2TalaSpecImpl(parent: PropertiesSpec) : D2TalaSpec, PropertiesSpec by parent {
  override var seeds by longList("tala-seeds")
}
