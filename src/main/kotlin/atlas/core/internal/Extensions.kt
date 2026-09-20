package atlas.core.internal

import atlas.core.Framework
import atlas.core.IntEnum
import atlas.core.LinkType
import atlas.core.LinkTypeSpec
import atlas.core.ProjectType
import atlas.core.ProjectTypeSpec
import atlas.core.StringEnum
import atlas.core.StyleSpec
import java.io.File
import org.gradle.api.Project
import org.gradle.api.file.Directory
import org.gradle.api.file.RegularFile
import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.Property
import org.gradle.api.provider.Provider
import org.gradle.api.provider.SetProperty

internal fun AtlasExtensionImpl.orderedProjectTypes(): List<ProjectTypeSpec> =
  projectTypes.getInOrder()

internal fun AtlasExtensionImpl.orderedLinkTypes(): List<LinkType> =
  linkTypes.getInOrder().map(::linkType)

internal fun projectType(type: ProjectTypeSpec): ProjectType =
  ProjectType(
    name = type.name,
    color = type.color.orNull,
    properties = type.propertiesByFramework(),
  )

internal fun linkType(type: LinkTypeSpec): LinkType =
  LinkType(
    configuration = type.configuration.get(),
    style = type.style.orNull,
    color = type.color.orNull,
    displayName = type.name,
    properties = type.propertiesByFramework(),
  )

/** Attributes for every framework which has at least one set, keyed by [Framework.string]. */
private fun StyleSpec.propertiesByFramework(): Map<String, Map<String, String>> =
  Framework.entries
    .associate { framework -> framework.string to properties(framework).getOrElse(emptyMap()) }
    .filterValues { it.isNotEmpty() }

internal fun ObjectFactory.bool(convention: Provider<Boolean>): Property<Boolean> =
  property(Boolean::class.java).convention(convention)

internal fun ObjectFactory.int(convention: Provider<Int>): Property<Int> =
  property(Int::class.java).convention(convention)

internal fun ObjectFactory.float(convention: Provider<Float>): Property<Float> =
  property(Float::class.java).convention(convention)

internal fun ObjectFactory.string(convention: Provider<String>): Property<String> =
  property(String::class.java).convention(convention)

internal fun ObjectFactory.string(convention: String?): Property<String> =
  property(String::class.java).convention(convention)

internal inline fun <reified E> ObjectFactory.enum(convention: E?): Property<E>
  where E : StringEnum, E : Enum<E> = property(E::class.java).convention(convention)

internal inline fun <reified E> ObjectFactory.enum(convention: Provider<E>): Property<E>
  where E : StringEnum, E : Enum<E> = property(E::class.java).convention(convention)

internal inline fun <reified E> ObjectFactory.intEnum(convention: Provider<E>): Property<E>
  where E : IntEnum, E : Enum<E> = property(E::class.java).convention(convention)

internal inline fun <reified T : Any> ObjectFactory.set(convention: Set<T>): SetProperty<T> =
  setProperty(T::class.java).convention(convention)

internal val Project.atlasBuildDirectory: Provider<Directory>
  get() = project.layout.buildDirectory.dir("atlas")

internal fun Project.fileInBuildDirectory(path: String): Provider<RegularFile> =
  atlasBuildDirectory.map {
    it.file(path)
  }

private const val DIR_NAME = "atlas"

/**
 * A project's chart sits in the project directory it describes, next to its README, e.g.
 * `chart-d2.svg`. Legends belong to the whole build rather than to any one project, so they go in
 * the root project's `atlas/` directory instead of cluttering the root alongside the user's own
 * files.
 *
 * The framework is part of the filename rather than a directory of its own, so enabling several at
 * once still never has two of them writing the same file.
 */
internal fun Project.outputFile(
  config: AtlasConfig,
  framework: Framework,
  variant: Variant,
  fileExtension: String,
  filename: String = defaultFilename(variant),
): File {
  val directory =
    when (variant) {
      Chart -> layout.projectDirectory.asFile
      Legend -> config.rootDir.resolve(DIR_NAME)
    }
  return directory.resolve(frameworkFilename(filename, framework, fileExtension))
}

/**
 * Like [outputFile], but for a file which is only an input to the framework's own renderer, e.g.
 * D2's `chart.d2`. These go in the build directory when [inBuildDir] is set, which comes from the
 * framework's `intermediateFilesInBuildDir` property.
 */
internal fun Project.intermediateFile(
  config: AtlasConfig,
  framework: Framework,
  variant: Variant,
  fileExtension: String,
  inBuildDir: Boolean,
  filename: String = defaultFilename(variant),
): File =
  if (inBuildDir) {
    atlasBuildDirectory.get().asFile.resolve(frameworkFilename(filename, framework, fileExtension))
  } else {
    outputFile(config, framework, variant, fileExtension, filename)
  }

private fun defaultFilename(variant: Variant) =
  when (variant) {
    Chart -> "chart"
    Legend -> "legend"
  }

/** e.g. `chart-d2.svg`, `legend-graphviz.dot`, `classes-d2.d2`. */
private fun frameworkFilename(filename: String, framework: Framework, fileExtension: String) =
  "$filename-${framework.string}.$fileExtension"
