package atlas.core

import atlas.d2.D2Spec
import atlas.graphviz.GraphvizSpec
import atlas.mermaid.MermaidSpec
import org.gradle.api.Action
import org.gradle.api.provider.Property
import org.gradle.api.provider.SetProperty

/**
 * Main entry point for configuring the plugin from your Gradle script.
 *
 * Configure one block per diagram framework you want generated - each one you touch registers that
 * framework's tasks, and writes each project's chart to `chart-<framework>.<ext>` in that project's
 * directory:
 * ```kotlin
 * atlas {
 *   projectTypes { useDefaults() }
 *
 *   d2 {
 *     fileFormat = FileFormat.Svg
 *   }
 *
 *   mermaid()
 * }
 * ```
 */
@AtlasDsl
public interface AtlasExtension {
  /**
   * When enabled, syncing your IntelliJ IDE (including Android Studio) will automatically trigger
   * regeneration of your project diagrams. Defaults to false.
   *
   * Be careful enabling this on large projects - sync time might extend quite a lot.
   *
   * Also controlled by the `atlas.generateOnSync` Gradle property.
   */
  public val generateOnSync: Property<Boolean>

  /**
   * Set to true if you want project charts to gather together groups of projects into bordered
   * containers. E.g. a graph with ":a", ":b" and ":c" won't be grouped at all because they don't
   * share any path segments, but ":a:b" and ":a:c" will be grouped together. A grouped project is
   * labelled with only its last path segment, since its container already shows the rest.
   *
   * Also controlled by the `atlas.groupProjects` Gradle property.
   */
  public val groupProjects: Property<Boolean>

  /**
   * Use this to configure Gradle [org.gradle.api.artifacts.Configuration]s to block from
   * consideration when collating project diagrams. A configuration is ignored if its name contains
   * any of these strings, ignoring case. Defaults to ["debug", "kover", "ksp", "test"].
   */
  public val ignoredConfigs: SetProperty<String>

  /**
   * Use this to block projects from inclusion in your project charts, based on their path string.
   * E.g. a project at ":path:to:my:project" will be ignored if I add `".*:to:my:.*".toRegex()` to
   * this property. Each pattern has to match the whole path. Defaults to an empty set.
   */
  public val ignoredProjects: SetProperty<Regex>

  /**
   * Set to true if you want project charts to also show projects that depend on the one in
   * question. This will traverse the graph both directions and show all upstream and downstream
   * projects. Defaults to false.
   *
   * Also controlled by the `atlas.alsoTraverseUpwards` Gradle property.
   */
  public val alsoTraverseUpwards: Property<Boolean>

  /**
   * Set to true to print the absolute path of any generated files to the Gradle console output.
   * Defaults to false.
   *
   * Also controlled by the `atlas.printFilesToConsole` Gradle property.
   */
  public val printFilesToConsole: Property<Boolean>

  /**
   * Set to true to attach a diffing task to `gradle check`. It will verify that your generated
   * charts match the current state of the project layout, failing if not with a useful error
   * message. Defaults to true.
   *
   * D2 and Graphviz only register these tasks when their `intermediateFilesInBuildDir` is false.
   *
   * Also controlled by the `atlas.checkOutputs` Gradle property.
   */
  public val checkOutputs: Property<Boolean>

  /**
   * Set to true to attach a string label on each project link, showing which configuration caused
   * the link to be created. Defaults to false. When true, the [LinkTypeSpec.name] property will be
   * shown.
   *
   * Also controlled by the `atlas.displayLinkLabels` Gradle property.
   */
  public val displayLinkLabels: Property<Boolean>

  /**
   * Configures any string transformations to apply to project paths when displaying them in the
   * generated charts.
   */
  public val pathTransforms: PathTransformSpec

  public fun pathTransforms(action: Action<PathTransformSpec>)

  /** Configure the set of [ProjectTypeSpec]s to use when identifying projects in your project. */
  public val projectTypes: NamedProjectTypeContainer

  public fun projectTypes(action: Action<NamedProjectTypeContainer>)

  /** Configure the set of [LinkTypeSpec]s to use when identifying links between your projects. */
  public val linkTypes: NamedLinkTypeContainer

  public fun linkTypes(action: Action<NamedLinkTypeContainer>)

  /** The frameworks which have been switched on in this build. */
  public val frameworks: Set<Framework>

  /**
   * Configuration for D2 charts. Any use of it registers the D2 generation tasks, which write
   * `chart-d2.<ext>` into each project. That includes `d2()`, an empty `d2 { }` block, or setting a
   * property directly like `d2.sketch = true`. Any `atlas.d2.*` Gradle property does the same.
   */
  public val d2: D2Spec

  /** Configures [d2], registering D2 chart generation. */
  public fun d2(action: Action<D2Spec>)

  /** Registers D2 chart generation with default configuration. */
  public fun d2()

  /**
   * Configuration for Graphviz charts. Any use of it registers the Graphviz generation tasks, which
   * write `chart-graphviz.<ext>` into each project. That includes `graphviz()`, an empty `graphviz
   * { }` block, or setting a property directly like `graphviz.pathToDotCommand = "..."`. Any
   * `atlas.graphviz.*` Gradle property does the same.
   */
  public val graphviz: GraphvizSpec

  /** Configures [graphviz], registering Graphviz chart generation. */
  public fun graphviz(action: Action<GraphvizSpec>)

  /** Registers Graphviz chart generation with default configuration. */
  public fun graphviz()

  /**
   * Configuration for Mermaid charts. Any use of it registers the Mermaid generation tasks, which
   * write `chart-mermaid.mmd` into each project. That includes `mermaid()`, an empty `mermaid { }`
   * block, or setting a property directly like `mermaid.theme = Theme.Forest`. Any
   * `atlas.mermaid.*` Gradle property does the same.
   */
  public val mermaid: MermaidSpec

  /** Configures [mermaid], registering Mermaid chart generation. */
  public fun mermaid(action: Action<MermaidSpec>)

  /** Registers Mermaid chart generation with default configuration. */
  public fun mermaid()
}
