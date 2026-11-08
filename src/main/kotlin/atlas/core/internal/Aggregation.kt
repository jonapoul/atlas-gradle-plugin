package atlas.core.internal

import atlas.core.Framework
import java.io.File
import org.gradle.api.Project
import org.gradle.api.artifacts.ArtifactCollection
import org.gradle.api.artifacts.Configuration
import org.gradle.api.attributes.Attribute
import org.gradle.api.file.FileCollection
import org.gradle.api.file.RegularFile
import org.gradle.api.provider.Provider
import org.gradle.api.tasks.TaskProvider

/**
 * Every configuration Atlas creates starts with this, so that the project dependencies it declares
 * on itself never show up as links in the charts.
 */
internal const val ATLAS_CONFIGURATION_PREFIX = "atlas"

internal val ATLAS_ARTIFACT_ATTRIBUTE =
  Attribute.of("dev.jonpoulton.atlas.artifact", String::class.java)

/**
 * Isolated projects forbids reading another project's tasks or extensions, so every file Atlas
 * passes between projects travels as a dependency-resolution artifact instead. Each kind gets its
 * own attribute value, so one project dependency can carry any number of them.
 */
@JvmInline
internal value class AtlasArtifact(private val id: String) {
  val attributeValue: String
    get() = id

  val elementsConfigurationName: String
    get() = "$ATLAS_CONFIGURATION_PREFIX${suffix}Elements"

  private val suffix: String
    get() = id.split("-").joinToString(separator = "") { it.replaceFirstChar(Char::uppercaseChar) }

  companion object {
    // One project's own type, produced by [atlas.core.tasks.WriteProjectType]
    val ProjectType = AtlasArtifact("project-type")

    // One project's direct links, produced by [atlas.core.tasks.WriteProjectLinks]
    val ProjectLinks = AtlasArtifact("project-links")

    // Every project's type, produced by [atlas.core.tasks.CollateProjectTypes] on the root
    val CollatedTypes = AtlasArtifact("collated-types")

    // Every project's links, produced by [atlas.core.tasks.CollateProjectLinks] on the root
    val CollatedLinks = AtlasArtifact("collated-links")

    // The class definitions every D2 chart shares, written once on the root
    val D2Classes = AtlasArtifact("d2-classes")

    // The shared legend a framework draws once on the root, and every README links to
    fun legend(framework: Framework) = AtlasArtifact("legend-$framework")

    // One project's chart for a framework, which the root links to from its index page
    fun chart(framework: Framework) = AtlasArtifact("chart-$framework")
  }
}

// Publishes [file] so that other projects can resolve it as [artifact]
internal fun Project.publishAtlasArtifact(
  artifact: AtlasArtifact,
  file: Provider<RegularFile>,
  builtBy: TaskProvider<*>? = null,
) {
  configurations.consumable(artifact.elementsConfigurationName) { configuration ->
    configuration.attributes.attribute(ATLAS_ARTIFACT_ATTRIBUTE, artifact.attributeValue)
  }
  artifacts.add(artifact.elementsConfigurationName, file) { a -> builtBy?.let(a::builtBy) }
}

/**
 * One resolvable configuration depending on every project in [fromPaths], shared by every kind of
 * [AtlasArtifact] resolved from them. Each kind is picked out by its own view, see
 * [atlasArtifacts]. [default] only matters if something resolves the configuration itself, like the
 * `dependencies` task, which would otherwise find every Atlas variant ambiguous.
 */
internal fun Project.atlasDependencies(
  name: String,
  fromPaths: List<String>,
  default: AtlasArtifact,
): Configuration {
  val scope = configurations.dependencyScope("${name}Dependencies").get()
  fromPaths.forEach { path ->
    dependencies.add(scope.name, dependencies.project(mapOf("path" to path)))
  }

  return configurations
    .resolvable(name) { configuration ->
      configuration.extendsFrom(scope)
      configuration.attributes.attribute(ATLAS_ARTIFACT_ATTRIBUTE, default.attributeValue)
    }
    .get()
}

/**
 * Resolves [artifact] from every project this configuration depends on. Each
 * [org.gradle.api.artifacts.result.ResolvedArtifactResult]'s component is the publishing project.
 *
 * [lenient] is for the root collating from every subproject, where a project may legitimately
 * publish nothing - for example one with no build file, which Atlas leaves out of the graph
 * entirely. Resolving from the root is the other way around: it always publishes, so a failure
 * there is a real one and should be reported as itself rather than silently resolving to no files.
 */
internal fun Configuration.atlasArtifacts(
  artifact: AtlasArtifact,
  lenient: Boolean,
): ArtifactCollection =
  incoming
    .artifactView { view ->
      view.withVariantReselection()
      view.attributes { it.attribute(ATLAS_ARTIFACT_ATTRIBUTE, artifact.attributeValue) }
      view.lenient(lenient)
    }
    .artifacts

/**
 * The single file this collection resolves to. Atlas publishes at most one artifact per project per
 * kind, so anything resolved from the root project is always exactly one file.
 *
 * Anything else means the root project didn't publish what this project is asking it for, which
 * surfaces a long way from its cause - usually as a configuration cache serialization failure while
 * writing the task property this ends up in. So say what actually went wrong instead.
 */
internal fun FileCollection.singleFile(artifact: AtlasArtifact): Provider<File> =
  elements.map { locations ->
    val file = locations.singleOrNull()?.asFile
    checkNotNull(file) {
      "Expected the root project to publish exactly one '${artifact.attributeValue}' artifact, but " +
        "resolving it found ${locations.size}. This is a bug " +
        "in Atlas - please report it at $ISSUES_URL."
    }
  }

private const val ISSUES_URL = "https://github.com/jonapoul/atlas/issues"
