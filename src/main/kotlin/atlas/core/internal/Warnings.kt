// warnAboutConfig runs once from settingsEvaluated, at the same moment AtlasConfig snapshots these
// same containers. Walking every declared type is the whole point, so the eager operators here
// realize nothing that wasn't already being realized.
@file:Suppress("LazyCollectionOperators")

package atlas.core.internal

import atlas.core.Framework
import org.gradle.api.logging.Logger

/**
 * Config problems are reported once, from settings, rather than once per project. Everything here
 * is advisory - Atlas still generates whatever it can.
 */
internal fun AtlasExtensionImpl.warnAboutConfig(logger: Logger) {
  if (frameworks.isEmpty()) {
    logger.warn(
      "Warning: no Atlas diagram frameworks are configured, so no charts will be generated. " +
        "Add a d2 { }, graphviz { } or mermaid { } block to your atlas { } config."
    )
  }

  warnIfProjectTypesSpecifyNothing(logger)
  warnAboutUnusedProperties(logger)
  warnAboutUnsupportedLinkStyles(logger)
}

private fun AtlasExtensionImpl.warnIfProjectTypesSpecifyNothing(logger: Logger) {
  projectTypes.forEach { type ->
    if (
      !type.pathContains.isPresent && !type.pathMatches.isPresent && !type.hasPluginId.isPresent
    ) {
      logger.warn(
        "Warning: Project type '${type.name}' will be ignored - you need to set one of " +
          "pathContains, pathMatches or hasPluginId."
      )
    }
  }
}

/**
 * Every framework's style properties are available on every project and link type, so it's easy to
 * configure one that nothing will read. Point them out rather than silently dropping them.
 */
private fun AtlasExtensionImpl.warnAboutUnusedProperties(logger: Logger) {
  val configured = frameworks

  projectTypes.forEach { type ->
    warnAboutUnusedProperties(
      logger = logger,
      description = "project type '${type.name}'",
      properties = (type as ProjectTypeSpecImpl).styleProperties,
      configured = configured,
    )
  }

  linkTypes.forEach { type ->
    warnAboutUnusedProperties(
      logger = logger,
      description = "link type '${type.name}'",
      properties = (type as LinkTypeSpecImpl).styleProperties,
      configured = configured,
    )
  }
}

private fun warnAboutUnusedProperties(
  logger: Logger,
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
      logger.warn(
        "Warning: $description sets ${unused.joinAnd()}, which only " +
          "${frameworks.map { it.displayName }.joinAnd()} " +
          "${if (frameworks.size == 1) "uses" else "use"}. Configure the $blocks block to use " +
          "${if (unused.size == 1) "it" else "them"}, or remove the config."
      )
    }
}

private fun AtlasExtensionImpl.warnAboutUnsupportedLinkStyles(logger: Logger) {
  val configured = frameworks
  linkTypes.forEach { type ->
    val style = type.style.orNull ?: return@forEach
    val unsupported = configured.filterNot { it in style.supportedBy }
    if (unsupported.isNotEmpty()) {
      logger.warn(
        "Warning: link type '${type.name}' uses the $style style, which " +
          "${unsupported.map { it.displayName }.joinAnd()} can't draw - " +
          "Atlas will fall back to the closest style it has."
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
