package atlas.core.internal

import atlas.core.IntEnum
import atlas.core.StringEnum
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
  prop(key, default, String::toFloat)

internal fun IGradleProperties.int(key: String, default: Int? = null): Provider<Int> =
  prop(key, default, String::toInt)

internal fun IGradleProperties.string(key: String, default: String? = null): Provider<String> =
  prop(key, default) { it }

internal inline fun <reified E> IGradleProperties.enum(
  key: String,
  default: E? = null,
): Provider<E> where E : StringEnum, E : Enum<E> = prop(key, default) { parseEnum<E>(it) }

internal inline fun <reified E> IGradleProperties.intEnum(
  key: String,
  default: E? = null,
): Provider<E> where E : IntEnum, E : Enum<E> = prop(key, default) { parseIntEnum<E>(it) }

// parse needs to be crossinline because it's called from the map lambda. Inlining copies that
// lambda into each caller as a fresh instance rather than a Kotlin singleton, which Gradle 9.8's
// config cache rejects once the beforeProject action has been isolated.
private inline fun <T : Any> IGradleProperties.prop(
  key: String,
  default: T?,
  crossinline parse: (String) -> T,
): Provider<T> =
  providers.gradleProperty(key).map { value -> parse(value) }.orElse(providers.provider { default })
