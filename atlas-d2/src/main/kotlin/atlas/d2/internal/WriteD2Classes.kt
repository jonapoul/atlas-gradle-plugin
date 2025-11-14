@file:Suppress("NestedBlockDepth", "LongParameterList")

package atlas.d2.internal

import atlas.core.InternalAtlasApi
import atlas.core.LinkType
import atlas.core.ProjectType
import atlas.core.internal.IndentedStringBuilder
import atlas.core.internal.buildIndentedString
import atlas.core.internal.orderedLinkTypes
import atlas.core.internal.orderedProjectTypes
import atlas.core.internal.parseEnum
import atlas.core.internal.projectType
import atlas.core.internal.sortedByKeys
import atlas.d2.Direction
import atlas.d2.LayoutEngine
import atlas.d2.LinkStyle
import atlas.d2.Location
import atlas.d2.Position
import atlas.d2.Position.CenterLeft
import atlas.d2.Position.CenterRight
import atlas.d2.Theme
import java.io.Serializable as JSerializable
import kotlinx.serialization.Serializable as KSerializable

@InternalAtlasApi
public fun writeD2Classes(config: D2ClassesConfig): String = buildIndentedString {
  appendStyles(config)
  appendVars(config)

  appendLine("classes: {")
  indent {
    for (type in config.projectTypes) appendClass(type)
    for (type in config.linkTypes) appendLink(config, type)
    appendContainer(config)
    appendHidden()
  }
  appendLine("}")

  appendGlobs(config)
}

@KSerializable
public class D2ClassesConfig(
  public val animateLinks: Boolean? = null,
  public val center: Boolean? = null,
  public val darkTheme: Theme? = null,
  public val direction: Direction? = null,
  public val displayLinkLabels: Boolean? = null,
  public val globalProps: Map<String, String>? = null,
  public val layoutEngine: LayoutEngine? = null,
  public val linkTypes: List<LinkType> = emptyList(),
  public val location: Location? = null,
  public val projectTypes: List<ProjectType> = emptyList(),
  public val pad: Int? = null,
  public val position: Position? = null,
  public val rootStyle: Map<String, String> = emptyMap(),
  public val sketch: Boolean? = null,
  public val theme: Theme? = null,
) : JSerializable

internal fun D2AtlasExtensionImpl.toConfig() = D2ClassesConfig(
  animateLinks = d2.animateLinks.orNull,
  center = d2.center.orNull,
  darkTheme = d2.themeDark.orNull,
  direction = d2.direction.orNull,
  displayLinkLabels = displayLinkLabels.orNull,
  globalProps = d2.globalProps.properties.orNull,
  layoutEngine = d2.layoutEngine.layoutEngine.orNull,
  linkTypes = orderedLinkTypes(),
  location = d2.groupLabelLocation.orNull,
  projectTypes = orderedProjectTypes().map(::projectType),
  pad = d2.pad.orNull,
  position = d2.groupLabelPosition.orNull,
  rootStyle = d2.rootStyle.properties.getOrElse(mutableMapOf()),
  sketch = d2.sketch.orNull,
  theme = d2.theme.orNull,
)

internal const val CONTAINER_CLASS = "container"
internal const val HIDDEN_CLASS = "hidden"

internal val LinkType.classId get() = "link-$key"
internal val ProjectType.classId get() = "project-$key"

internal val LinkType.key: String get() = configuration.key
internal val ProjectType.key: String get() = name.key

private val String.key
  get() = this.filter { it.isLetter() || it.isDigit() }

private fun IndentedStringBuilder.appendClass(type: ProjectType) = with(type) {
  appendLine("$classId {")
  indent {
    val properties = type.properties + ("style.fill" to color)
    properties.sortedByKeys().forEach { (key, value) ->
      appendLine("$key: \"$value\"")
    }
  }
  appendLine("}")
}

private fun IndentedStringBuilder.appendLink(config: D2ClassesConfig, type: LinkType) {
  appendLine("${type.classId} {")
  indent {
    for ((key, value) in linkAttributes(config, type)) {
      appendLine("$key: \"$value\"")
    }
  }
  appendLine("}")
}

// Remove suppression once https://github.com/pinterest/ktlint/pull/3177 is merged
@Suppress("ktlint:standard:blank-line-between-when-conditions")
private fun linkAttributes(config: D2ClassesConfig, link: LinkType): List<Pair<String, String>> {
  val attrs = mutableMapOf<String, String>()
  link.color?.let { attrs["style.stroke"] = it }

  val style = link.style?.let { parseEnum<LinkStyle>(it) }
  when (style) {
    LinkStyle.Dashed -> attrs["style.stroke-dash"] = "4"
    LinkStyle.Dotted -> attrs["style.stroke-dash"] = "2"
    LinkStyle.Basic -> Unit
    LinkStyle.Invisible -> attrs["style.opacity"] = "0"
    LinkStyle.Bold -> attrs["style.stroke-width"] = "3" // default 2
    null -> Unit
  }

  if (config.animateLinks == true && style in ANIMATABLE_LINK_TYPES) {
    attrs["style.animated"] = "true"
  }

  if (config.displayLinkLabels == true) {
    attrs["label"] = link.displayName
  }

  attrs.putAll(link.properties)

  return attrs.sortedByKeys()
}

private fun IndentedStringBuilder.appendContainer(config: D2ClassesConfig) {
  appendLine("$CONTAINER_CLASS {")
  indent { appendGroupLabelSpecifier(config) }
  appendLine("}")
}

private fun IndentedStringBuilder.appendHidden() {
  appendLine("$HIDDEN_CLASS {")
  indent { appendLine("style.opacity: 0") }
  appendLine("}")
}

private fun IndentedStringBuilder.appendGroupLabelSpecifier(config: D2ClassesConfig) = with(config) {
  position ?: return@with // e.g. top-right
  if (location == null) {
    appendLine("label.near: $position")
  } else {
    // need to swap the order to "left-center" if we have a location specifier prefix
    val rejiggedPosition = if (position in setOf(CenterLeft, CenterRight)) {
      position.toString().split("-").let { str -> "${str[1]}-${str[0]}" }
    } else {
      position.toString()
    }
    val position = when (location) {
      Location.Inside -> ""
      Location.Border -> "$location-"
      Location.Outside -> "$location-"
    }
    appendLine("label.near: $position$rejiggedPosition")
  }
}

private fun IndentedStringBuilder.appendStyles(config: D2ClassesConfig) = with(config) {
  direction?.let { appendLine("direction: $it") }

  if (rootStyle.isEmpty()) return@with
  appendLine("style: {")
  indent {
    rootStyle.forEach { (key, value) ->
      appendLine("$key: \"$value\"")
    }
  }
  appendLine("}")
}

private fun IndentedStringBuilder.appendVars(config: D2ClassesConfig) = with(config) {
  val attrs = mapOf<String, Any?>(
    "theme-id" to theme?.value,
    "dark-theme-id" to darkTheme?.value,
    "layout-engine" to layoutEngine?.string,
    "pad" to pad,
    "sketch" to sketch,
    "center" to center,
  )
  if (attrs.count { it.value != null } == 0) {
    return@with
  }

  appendLine("vars: {")
  indent {
    appendLine("d2-config: {")
    indent {
      attrs.sortedByKeys().forEach { (key, value) ->
        if (value != null) {
          appendLine("$key: $value")
        }
      }
    }
    appendLine("}")
  }
  appendLine("}")
}

private fun IndentedStringBuilder.appendGlobs(config: D2ClassesConfig) = config.globalProps?.let { props ->
  props.sortedByKeys().forEach { (key, value) ->
    appendLine("$key: $value")
  }
}

private val ANIMATABLE_LINK_TYPES = setOf(LinkStyle.Dashed, LinkStyle.Dotted)
