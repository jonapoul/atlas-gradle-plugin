package atlas.core.internal

import atlas.core.PropertiesSpec
import atlas.core.StringEnum
import kotlin.reflect.KProperty
import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.MapProperty
import org.gradle.api.provider.ProviderFactory

internal interface InternalPropertiesSpec : PropertiesSpec {
  val providers: ProviderFactory
  val gradlePropertyPrefix: String
}

internal class PropertiesSpecImpl(
  objects: ObjectFactory,
  override val providers: ProviderFactory,
  override val gradlePropertyPrefix: String,
) : InternalPropertiesSpec {
  override val properties: MapProperty<String, String> =
    objects.mapProperty(String::class.java, String::class.java).convention(null)
}

internal fun InternalPropertiesSpec.child(name: String): InternalPropertiesSpec =
  ChildPropertiesSpec(parent = this, name = name)

private class ChildPropertiesSpec(
  private val parent: InternalPropertiesSpec,
  name: String,
) : InternalPropertiesSpec by parent {
  override val gradlePropertyPrefix: String = "${parent.gradlePropertyPrefix}.$name"
}

internal class Delegate<T>(
  private val spec: InternalPropertiesSpec,
  private val key: String,
  private val fromString: (String) -> T,
  private val toString: (T?) -> String? = { it?.toString() },
) {
  // Read eagerly rather than as a provider, so an unset property leaves the map absent - the
  // writers use an absent map to mean "nothing configured". Putting it here, while the spec is
  // being constructed, means a later DSL assignment to the same key wins.
  operator fun provideDelegate(thisRef: Any?, property: KProperty<*>): Delegate<T> {
    val gradleKey = "${spec.gradlePropertyPrefix}.${property.name}"
    val raw = spec.providers.gradleProperty(gradleKey).orNull ?: return this
    spec.properties.put(key, parse(raw, gradleKey))
    return this
  }

  @Suppress("TooGenericExceptionCaught")
  private fun parse(raw: String, gradleKey: String): String =
    try {
      toString(fromString(raw)) ?: raw
    } catch (e: RuntimeException) {
      throw IllegalArgumentException("Invalid value '$raw' for Gradle property '$gradleKey'", e)
    }

  operator fun setValue(thisRef: Any?, property: KProperty<*>, value: T?): Unit? =
    toString(value)?.let { spec.properties.put(key, it) }

  operator fun getValue(thisRef: Any?, property: KProperty<*>): T? =
    spec.properties.get()[key]?.let(fromString)
}

internal fun InternalPropertiesSpec.bool(key: String): Delegate<Boolean> =
  Delegate(this, key, fromString = { it.toBooleanStrict() })

internal inline fun <reified E> InternalPropertiesSpec.enum(key: String): Delegate<E>
  where E : StringEnum, E : Enum<E> =
  Delegate(
    spec = this,
    key = key,
    fromString = { str -> parseEnum<E>(str) },
    toString = { it?.value },
  )

internal fun InternalPropertiesSpec.int(key: String): Delegate<Int> =
  Delegate(this, key, fromString = { Integer.valueOf(it) })

internal fun InternalPropertiesSpec.float(key: String): Delegate<Float> =
  Delegate(this, key, fromString = { it.toFloat() })

internal fun InternalPropertiesSpec.string(key: String): Delegate<String> =
  Delegate(this, key, fromString = { it })

/** Comma-separated, which is how the D2 CLI parses its list-valued flags. */
internal fun InternalPropertiesSpec.longList(key: String): Delegate<List<Long>> =
  Delegate(
    spec = this,
    key = key,
    fromString = { str -> str.split(",").map { it.trim().toLong() } },
    toString = { list -> list?.takeIf { it.isNotEmpty() }?.joinToString(separator = ",") },
  )

internal fun InternalPropertiesSpec.number(key: String): Delegate<Number> =
  Delegate(
    spec = this,
    key = key,
    fromString = { if (it.contains(".")) it.toFloat() else it.toInt() },
  )
