package atlas.core.internal

import atlas.core.Framework
import atlas.core.StringEnum
import atlas.core.StyleSpec
import kotlin.reflect.KProperty
import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.MapProperty

/**
 * Backs [StyleSpec] with one attribute map per [Framework], plus a record of which DSL properties
 * were set so that we can warn about the ones no configured framework will read.
 */
internal class StyleProperties(objects: ObjectFactory) : StyleSpec {
  private val maps: Map<Framework, MapProperty<String, String>> =
    Framework.entries.associateWith { objects.mapProperty(String::class.java, String::class.java) }

  private val mutableUsages = mutableListOf<PropertyUsage>()

  /** Every DSL property set on this spec, in the order they were set. */
  val usages: List<PropertyUsage>
    get() = mutableUsages.toList()

  override fun properties(framework: Framework): MapProperty<String, String> =
    maps.getValue(framework)

  override fun put(framework: Framework, key: String, value: Any) {
    maps.getValue(framework).put(key, value.toString())
    record(name = key, frameworks = setOf(framework))
  }

  override fun clear() {
    maps.values.forEach { it.set(emptyMap()) }
    mutableUsages.clear()
  }

  override fun clear(framework: Framework) {
    maps.getValue(framework).set(emptyMap())
    mutableUsages.removeAll { it.frameworks == setOf(framework) }
  }

  internal fun record(name: String, frameworks: Set<Framework>) {
    mutableUsages += PropertyUsage(name = name, frameworks = frameworks)
  }

  internal fun get(framework: Framework): Map<String, String> =
    maps.getValue(framework).getOrElse(emptyMap())
}

/** A single DSL property assignment, and the frameworks which will read it. */
internal data class PropertyUsage(
  val name: String,
  val frameworks: Set<Framework>,
)

/**
 * Writes a DSL property through to the attribute maps of every framework which understands it. E.g.
 * `fontColor` is `style.font-color` in D2, `fontcolor` in Graphviz and `color` in Mermaid.
 */
internal class StyleDelegate<T>(
  private val properties: StyleProperties,
  private val keys: Map<Framework, String>,
  private val fromString: (String) -> T,
  private val toString: (T?) -> String? = { it?.toString() },
) {
  operator fun setValue(thisRef: Any?, property: KProperty<*>, value: T?) {
    val string = toString(value) ?: return
    properties.record(name = property.name, frameworks = keys.keys)
    keys.forEach { (framework, key) -> properties.properties(framework).put(key, string) }
  }

  operator fun getValue(thisRef: Any?, property: KProperty<*>): T? =
    keys
      .firstNotNullOfOrNull { (framework, key) -> properties.get(framework)[key] }
      ?.let(fromString)
}

internal fun StyleProperties.bool(vararg keys: Pair<Framework, String>): StyleDelegate<Boolean> =
  StyleDelegate(this, keys.toMap(), fromString = { it.toBoolean() })

internal fun StyleProperties.int(vararg keys: Pair<Framework, String>): StyleDelegate<Int> =
  StyleDelegate(this, keys.toMap(), fromString = { it.toInt() })

internal fun StyleProperties.float(vararg keys: Pair<Framework, String>): StyleDelegate<Float> =
  StyleDelegate(this, keys.toMap(), fromString = { it.toFloat() })

internal fun StyleProperties.number(vararg keys: Pair<Framework, String>): StyleDelegate<Number> =
  StyleDelegate(
    properties = this,
    keys = keys.toMap(),
    fromString = { if (it.contains(".")) it.toFloat() else it.toInt() },
  )

internal fun StyleProperties.string(vararg keys: Pair<Framework, String>): StyleDelegate<String> =
  StyleDelegate(this, keys.toMap(), fromString = { it })

internal inline fun <reified E> StyleProperties.enum(
  vararg keys: Pair<Framework, String>
): StyleDelegate<E> where E : StringEnum, E : Enum<E> =
  StyleDelegate(
    properties = this,
    keys = keys.toMap(),
    fromString = { string -> enumValues<E>().first { it.value == string } },
    toString = { it?.value },
  )
