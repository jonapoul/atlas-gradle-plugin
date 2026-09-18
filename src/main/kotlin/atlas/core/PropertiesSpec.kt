package atlas.core

import org.gradle.api.provider.MapProperty

/**
 * Used to configure custom properties into a [Map]. This will be implemented by a few other more
 * specific interfaces depending on the use case, but you will have the ability to call [put] to
 * manually add custom properties to the map. This might be useful if a new release of the chart
 * framework adds a property before this plugin gets updated to support it.
 */
@AtlasDsl
public interface PropertiesSpec {
  /** The raw key/value attributes which will be handed to the framework when writing charts. */
  public val properties: MapProperty<String, String>

  /** Sets a single raw attribute. [value] is converted with [toString]. */
  public fun put(key: String, value: Any): Unit = properties.put(key, value.toString())

  /** Removes every attribute set on this spec. */
  public fun clear(): Unit = properties.set(emptyMap())
}
