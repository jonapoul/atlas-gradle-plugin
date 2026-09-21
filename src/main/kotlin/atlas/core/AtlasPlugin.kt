package atlas.core

import atlas.core.internal.AtlasExtensionImpl
import atlas.core.internal.AtlasWiring
import atlas.core.internal.onSettingsEvaluated
import atlas.core.internal.wireProject
import javax.inject.Inject
import org.gradle.api.Plugin
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
      onSettingsEvaluated(settings, wiring, extension)
    }

    target.gradle.lifecycle.beforeProject { project -> wireProject(project, wiring) }
  }

  internal companion object {
    val LOGGER: Logger = Logging.getLogger(AtlasPlugin::class.java)
  }
}
