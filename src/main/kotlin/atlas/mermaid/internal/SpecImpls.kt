package atlas.mermaid.internal

import atlas.core.internal.InternalPropertiesSpec
import atlas.core.internal.PropertiesSpecImpl
import atlas.core.internal.bool
import atlas.core.internal.enum
import atlas.core.internal.string
import atlas.mermaid.ConsiderModelOrder
import atlas.mermaid.CycleBreakingStrategy
import atlas.mermaid.ElkLayoutSpec
import atlas.mermaid.MermaidLayoutSpec
import atlas.mermaid.MermaidSpec
import atlas.mermaid.MermaidThemeVariablesSpec
import atlas.mermaid.NodePlacementStrategy
import kotlin.jvm.java
import org.gradle.api.Action
import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.ProviderFactory

internal class MermaidSpecImpl(
  private val objects: ObjectFactory,
  private val providers: ProviderFactory,
) : MermaidSpec {
  private val properties = MermaidGradleProperties(providers)
  private var mutableLayout = MermaidLayoutSpecImpl(objects, providers, "atlas.mermaid.layout")

  override val name = "Mermaid"
  override val fileExtension = objects.string(convention = "mmd")

  override val layout
    get() = mutableLayout

  override fun layout(action: Action<MermaidLayoutSpec>) = action.execute(mutableLayout)

  override val themeVariables = MermaidThemeVariablesSpecImpl(objects, providers)

  override fun themeVariables(action: Action<MermaidThemeVariablesSpec>) =
    action.execute(themeVariables)

  override fun elk(action: Action<ElkLayoutSpec>?) {
    mutableLayout = ElkLayoutSpecImpl(objects, providers).also { action?.execute(it) }
  }

  override val animateLinks = objects.bool(properties.animateLinks)
  override val look = objects.enum(properties.look)
  override val theme = objects.enum(properties.theme)
}

internal open class MermaidLayoutSpecImpl(
  objects: ObjectFactory,
  providers: ProviderFactory,
  prefix: String,
) : MermaidLayoutSpec, InternalPropertiesSpec by PropertiesSpecImpl(objects, providers, prefix) {
  override val name = objects.property(String::class.java).unsetConvention()
}

internal class ElkLayoutSpecImpl(objects: ObjectFactory, providers: ProviderFactory) :
  MermaidLayoutSpecImpl(objects, providers, "atlas.mermaid.elk"), ElkLayoutSpec {
  init {
    name.set("elk")
    name.finalizeValue()
  }

  override var considerModelOrder by enum<ConsiderModelOrder>("considerModelOrder")
  override var cycleBreakingStrategy by enum<CycleBreakingStrategy>("cycleBreakingStrategy")
  override var forceNodeModelOrder by bool("forceNodeModelOrder")
  override var mergeEdges by bool("mergeEdges")
  override var nodePlacementStrategy by enum<NodePlacementStrategy>("nodePlacementStrategy")
}

internal class MermaidThemeVariablesSpecImpl(objects: ObjectFactory, providers: ProviderFactory) :
  MermaidThemeVariablesSpec,
  InternalPropertiesSpec by PropertiesSpecImpl(objects, providers, "atlas.mermaid.themeVariables") {
  override var background by string(key = "background")
  override var darkMode by bool(key = "darkMode")
  override var fontFamily by string(key = "fontFamily")
  override var fontSize by string(key = "fontSize")
  override var lineColor by string(key = "lineColor")
  override var primaryBorderColor by string(key = "primaryBorderColor")
  override var primaryColor by string(key = "primaryColor")
  override var primaryTextColor by string(key = "primaryTextColor")
  override var secondaryColor by string(key = "secondaryColor")
  override var tertiaryColor by string(key = "tertiaryColor")
}
