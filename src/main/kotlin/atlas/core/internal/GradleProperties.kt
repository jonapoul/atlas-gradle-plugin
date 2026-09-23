package atlas.core.internal

import atlas.core.IntEnum
import atlas.core.StringEnum
import blueprint.core.floatProperty
import blueprint.core.intProperty
import org.gradle.api.provider.Provider
import org.gradle.api.provider.ProviderFactory

internal class CoreGradleProperties(override val providers: ProviderFactory) : IGradleProperties {
  val alsoTraverseUpwards = bool("atlas.alsoTraverseUpwards", default = false)
  val checkOutputs = bool(key = "atlas.checkOutputs", default = true)
  val displayLinkLabels = bool(key = "atlas.displayLinkLabels", default = false)
  val generateOnSync = bool(key = "atlas.generateOnSync", default = false)
  val groupProjects = bool(key = "atlas.groupProjects", default = false)
  val printFilesToConsole = bool(key = "atlas.printFilesToConsole", default = false)
}

internal interface IGradleProperties {
  val providers: ProviderFactory
}

internal fun IGradleProperties.bool(key: String, default: Boolean? = null): Provider<Boolean> =
  prop(key, default, String::toBooleanStrict)

internal fun IGradleProperties.float(key: String, default: Float? = null): Provider<Float> =
  providers.floatProperty(key).orElse(providers.provider { default })

internal fun IGradleProperties.int(key: String, default: Int? = null): Provider<Int> =
  providers.intProperty(key).orElse(providers.provider { default })

internal fun IGradleProperties.string(key: String, default: String? = null): Provider<String> =
  prop(key, default) { it }

internal inline fun <reified E> IGradleProperties.enum(
  key: String,
  default: E? = null,
): Provider<E> where E : StringEnum, E : Enum<E> = string(key, default?.value).map { parseEnum(it) }

internal inline fun <reified E> IGradleProperties.intEnum(
  key: String,
  default: E? = null,
): Provider<E> where E : IntEnum, E : Enum<E> =
  string(key, default?.value?.toString()).map { parseIntEnum(it) }

private inline fun <reified T : Any> IGradleProperties.prop(
  key: String,
  default: T?,
  noinline mapper: (String) -> T?,
): Provider<T> = providers.gradleProperty(key).map(mapper).orElse(providers.provider { default })
