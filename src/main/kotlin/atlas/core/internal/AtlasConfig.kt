@file:Suppress("LongParameterList", "SerialVersionUIDInSerializableClass")

package atlas.core.internal

import atlas.core.Framework
import atlas.core.LinkType
import atlas.core.ProjectType
import atlas.core.ProjectTypeSpec
import atlas.core.Replacement
import atlas.d2.internal.D2SpecImpl
import atlas.graphviz.internal.GraphvizSpecImpl
import atlas.mermaid.internal.MermaidSpecImpl
import java.io.File
import java.io.Serializable

/**
 * An immutable snapshot of the settings-level `atlas { }` config.
 *
 * `gradle.lifecycle.beforeProject` actions are isolated (i.e. serialized) before they run, so
 * everything the per-project wiring captures has to survive that. Gradle's managed `Property` types
 * do, but [org.gradle.api.NamedDomainObjectContainer] does not, so the two containers are flattened
 * into plain value types here.
 */
internal class AtlasConfig(
  val rootDir: File,
  val subprojectPaths: List<String>,
  val frameworks: List<Framework>,
  val alsoTraverseUpwards: Boolean,
  val checkOutputs: Boolean,
  val displayLinkLabels: Boolean,
  val generateOnSync: Boolean,
  val groupProjects: Boolean,
  val printFilesToConsole: Boolean,
  val ignoredConfigs: Set<String>,
  val ignoredProjects: Set<Regex>,
  val replacements: Set<Replacement>,
  val projectTypes: List<ProjectTypeMatcher>,
  val linkTypes: List<LinkType>,
  val preferProjectRepositories: Boolean,
) : Serializable

/**
 * A [ProjectTypeSpec] reduced to the rules needed to decide whether a project is of this type, plus
 * the [ProjectType] value the charts consume once it matches.
 */
internal class ProjectTypeMatcher(
  val type: ProjectType,
  val pathContains: String?,
  val pathMatches: String?,
  val regexOptions: Set<RegexOption>,
  val hasPluginId: String?,
) : Serializable {
  val isEmpty: Boolean
    get() = pathContains == null && pathMatches == null && hasPluginId == null
}

/**
 * The state the per-project wiring captures. [config] is a value snapshot taken once settings have
 * been evaluated; the framework specs are live Gradle objects, which isolate cleanly because they
 * are built out of managed `Property` instances only.
 */
internal class AtlasWiring(
  val d2: D2SpecImpl,
  val graphviz: GraphvizSpecImpl,
  val mermaid: MermaidSpecImpl,
) : Serializable {
  private var snapshot: AtlasConfig? = null

  var config: AtlasConfig
    get() = checkNotNull(snapshot) { "Atlas config was read before settings were evaluated" }
    set(value) {
      snapshot = value
    }
}

internal fun AtlasExtensionImpl.snapshot(
  rootDir: File,
  subprojectPaths: List<String>,
  preferProjectRepositories: Boolean,
) =
  AtlasConfig(
    rootDir = rootDir,
    subprojectPaths = subprojectPaths,
    frameworks = frameworks.sorted(),
    alsoTraverseUpwards = alsoTraverseUpwards.get(),
    checkOutputs = checkOutputs.get(),
    displayLinkLabels = displayLinkLabels.get(),
    generateOnSync = generateOnSync.get(),
    groupProjects = groupProjects.get(),
    printFilesToConsole = printFilesToConsole.get(),
    ignoredConfigs = ignoredConfigs.get(),
    ignoredProjects = ignoredProjects.get(),
    replacements = pathTransforms.replacements.get(),
    projectTypes = orderedProjectTypes().map(::projectTypeMatcher),
    linkTypes = orderedLinkTypes(),
    preferProjectRepositories = preferProjectRepositories,
  )

private fun projectTypeMatcher(spec: ProjectTypeSpec) =
  ProjectTypeMatcher(
    type = projectType(spec),
    pathContains = spec.pathContains.orNull,
    pathMatches = spec.pathMatches.orNull,
    regexOptions = spec.regexOptions.getOrElse(emptySet()),
    hasPluginId = spec.hasPluginId.orNull,
  )
