package atlas.core.internal

import atlas.core.PropertiesSpec
import atlas.core.StringEnum
import kotlin.reflect.KProperty
import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.MapProperty

internal class PropertiesSpecImpl(objects: ObjectFactory) : PropertiesSpec {
  override val properties: MapProperty<String, String> =
    objects.mapProperty(String::class.java, String::class.java).convention(null)
}

internal class Delegate<T>(
  private val mapProperty: MapProperty<String, String>,
  private val key: String,
  private val fromString: (String) -> T,
  private val toString: (T?) -> String? = { it?.toString() },
) {
  operator fun setValue(thisRef: Any?, property: KProperty<*>, value: T?): Unit? =
    toString(value)?.let { mapProperty.put(key, it) }

  operator fun getValue(thisRef: Any?, property: KProperty<*>): T? =
    mapProperty.get()[key]?.let(fromString)
}

internal fun PropertiesSpec.bool(key: String): Delegate<Boolean> =
  Delegate(properties, key, fromString = { it.toBoolean() })

internal inline fun <reified E> PropertiesSpec.enum(key: String): Delegate<E>
  where E : StringEnum, E : Enum<E> =
  Delegate(
    mapProperty = properties,
    key = key,
    fromString = { str -> enumValues<E>().first { it.value == str } },
    toString = { it?.value },
  )

internal fun PropertiesSpec.int(key: String): Delegate<Int> =
  Delegate(properties, key, fromString = { Integer.valueOf(it) })

internal fun PropertiesSpec.float(key: String): Delegate<Float> =
  Delegate(properties, key, fromString = { it.toFloat() })

internal fun PropertiesSpec.string(key: String): Delegate<String> =
  Delegate(properties, key, fromString = { it })

/** Comma-separated, which is how the D2 CLI parses its list-valued flags. */
internal fun PropertiesSpec.longList(key: String): Delegate<List<Long>> =
  Delegate(
    mapProperty = properties,
    key = key,
    fromString = { str -> str.split(",").map(String::toLong) },
    toString = { list -> list?.takeIf { it.isNotEmpty() }?.joinToString(separator = ",") },
  )

internal fun PropertiesSpec.number(key: String): Delegate<Number> =
  Delegate(
    mapProperty = properties,
    key = key,
    fromString = { if (it.contains(".")) it.toFloat() else it.toInt() },
  )
