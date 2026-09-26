// warnAboutConfig runs once from settingsEvaluated, at the same moment AtlasConfig snapshots these
// same containers. Walking every declared type is the whole point, so the eager operators here
// realize nothing that wasn't already being realized.
@file:Suppress("LazyCollectionOperators")

package atlas.core.internal

import atlas.core.Framework
import atlas.core.internal.AtlasWarnings.Companion.NO_FRAMEWORKS
import atlas.core.internal.AtlasWarnings.Companion.PROJECT_TYPE_NO_MATCHER
import atlas.core.internal.AtlasWarnings.Companion.UNSUPPORTED_LINK_STYLE
import atlas.core.internal.AtlasWarnings.Companion.UNUSED_STYLE_PROPERTY

/**
 * Config problems are reported once, from settings, rather than once per project. Everything here
 * is advisory - Atlas still generates whatever it can.
 */
internal fun AtlasExtensionImpl.warnAboutConfig(warnings: AtlasWarnings) {
  if (frameworks.isEmpty()) {
    warnings.warn(
      NO_FRAMEWORKS,
      "No Atlas diagram frameworks are configured, so no charts will be generated. " +
        "Add a d2 { }, graphviz { } or mermaid { } block to your atlas { } config.",
    )
  }

  warnIfProjectTypesSpecifyNothing(warnings)
  warnAboutUnusedProperties(warnings)
  warnAboutUnsupportedLinkStyles(warnings)
}

private fun AtlasExtensionImpl.warnIfProjectTypesSpecifyNothing(warnings: AtlasWarnings) {
  projectTypes.forEach { type ->
    if (
      !type.pathContains.isPresent && !type.pathMatches.isPresent && !type.hasPluginId.isPresent
    ) {
      warnings.warn(
        PROJECT_TYPE_NO_MATCHER,
        "Project type '${type.name}' will be ignored - you need to set one of " +
          "pathContains, pathMatches or hasPluginId.",
      )
    }
  }
}

/**
 * Every framework's style properties are available on every project and link type, so it's easy to
 * configure one that nothing will read. Point them out rather than silently dropping them.
 */
private fun AtlasExtensionImpl.warnAboutUnusedProperties(warnings: AtlasWarnings) {
  val configured = frameworks

  projectTypes.forEach { type ->
    warnAboutUnusedProperties(
      warnings = warnings,
      description = "Project type '${type.name}'",
      properties = (type as ProjectTypeSpecImpl).styleProperties,
      configured = configured,
    )
  }

  linkTypes.forEach { type ->
    warnAboutUnusedProperties(
      warnings = warnings,
      description = "Link type '${type.name}'",
      properties = (type as LinkTypeSpecImpl).styleProperties,
      configured = configured,
    )
  }
}

private fun warnAboutUnusedProperties(
  warnings: AtlasWarnings,
  description: String,
  properties: StyleProperties,
  configured: Set<Framework>,
) {
  properties.usages
    .filter { usage -> usage.frameworks.none { it in configured } }
    .groupBy({ it.frameworks.sorted() }, { it.name })
    .forEach { (frameworks, names) ->
      val unused = names.distinct()
      val blocks = frameworks.joinToString(separator = " or ") { f -> "$f { }" }
      warnings.warn(
        UNUSED_STYLE_PROPERTY,
        "$description sets ${unused.joinAnd()}, which only " +
          "${frameworks.map { it.displayName }.joinAnd()} " +
          "${if (frameworks.size == 1) "uses" else "use"}. Configure the $blocks block to use " +
          "${if (unused.size == 1) "it" else "them"}, or remove the config.",
      )
    }
}

private fun AtlasExtensionImpl.warnAboutUnsupportedLinkStyles(warnings: AtlasWarnings) {
  val configured = frameworks
  linkTypes.forEach { type ->
    val style = type.style.orNull ?: return@forEach
    val unsupported = configured.filterNot { it in style.supportedBy }
    if (unsupported.isNotEmpty()) {
      warnings.warn(
        UNSUPPORTED_LINK_STYLE,
        "Link type '${type.name}' uses the $style style, which " +
          "${unsupported.map { it.displayName }.joinAnd()} can't draw - " +
          "Atlas will fall back to the closest style it has.",
      )
    }
  }
}

private fun List<String>.joinAnd(separator: String = "and"): String =
  when (size) {
    0 -> ""
    1 -> single()
    else -> dropLast(1).joinToString(separator = ", ") + " $separator " + last()
  }
