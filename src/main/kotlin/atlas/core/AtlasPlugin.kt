package atlas.core

import atlas.core.internal.AtlasExtensionImpl
import atlas.core.internal.AtlasWiring
import atlas.core.internal.snapshot
import atlas.core.internal.warnAboutConfig
import atlas.core.internal.wireProject
import atlas.d2.internal.d2Releases
import javax.inject.Inject
import org.gradle.api.Plugin
import org.gradle.api.initialization.ProjectDescriptor
import org.gradle.api.initialization.Settings
import org.gradle.api.logging.Logger
import org.gradle.api.logging.Logging
import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.ProviderFactory

/**
 * Applied to `settings.gradle.kts`, where the whole build's diagrams are configured:
 * ```kotlin
 * plugins {
 *   id("dev.jonpoulton.atlas")
 * }
 *
 * atlas {
 *   projectTypes { useDefaults() }
 *   graphviz { }
 * }
 * ```
 *
 * Atlas lives in settings rather than in the root build script because every project needs the same
 * config, and under isolated projects a subproject may not read the root project's extension. From
 * settings the config is snapshotted once and handed to each project as it is configured.
 *
 * Which diagrams get generated is decided by the framework blocks configured in the extension - see
 * [AtlasExtension.d2], [AtlasExtension.graphviz] and [AtlasExtension.mermaid]. Any number of them
 * can be used at once.
 */
public class AtlasPlugin
@Inject
constructor(
  private val objects: ObjectFactory,
  private val providers: ProviderFactory,
) : Plugin<Settings> {
  override fun apply(target: Settings) {
    val extension =
      objects.newInstance(AtlasExtensionImpl::class.java, objects, providers).also { impl ->
        target.extensions.add(AtlasExtension::class.java, AtlasExtensionImpl.NAME, impl)
      }

    // The specs are captured live: they hold nothing but managed Property instances, which survive
    // the isolation that GradleLifecycle applies to the callback below. The containers do not, so
    // everything else is flattened into a value snapshot first.
    val wiring =
      AtlasWiring(d2 = extension.d2, graphviz = extension.graphviz, mermaid = extension.mermaid)

    target.gradle.settingsEvaluated { settings ->
      val repositories = settings.dependencyResolutionManagement
      wiring.config =
        extension.snapshot(
          rootDir = settings.rootDir,
          subprojectPaths = chartedSubprojectPaths(settings.rootProject),
          preferProjectRepositories = repositories.repositoriesMode.get() == PREFER_PROJECT,
        )

      // Read here rather than on apply, since the settings script sets it after `plugins { }`
      if (Framework.D2 in extension.frameworks) repositories.repositories.d2Releases()
      extension.warnAboutConfig(LOGGER)
    }

    target.gradle.lifecycle.beforeProject { project -> wireProject(project, wiring) }
  }

  /**
   * Projects without a build file are only there to group their children, so they get no node of
   * their own in the chart and publish nothing for the root to collate.
   */
  private fun chartedSubprojectPaths(project: ProjectDescriptor): List<String> = buildList {
    project.children.forEach { child ->
      if (child.buildFile.exists()) add(child.path)
      addAll(chartedSubprojectPaths(child))
    }
  }

  private companion object {
    val LOGGER: Logger = Logging.getLogger(AtlasPlugin::class.java)
  }
}
