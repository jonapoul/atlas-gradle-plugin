package atlas.core

/**
 * The diagram frameworks Atlas can generate. A framework is switched on by configuring its block in
 * your build script:
 * ```kotlin
 * atlas {
 *   d2 { }         // generates D2 charts
 *   mermaid { }    // ... and Mermaid charts
 *   // graphviz isn't configured, so no Graphviz tasks are registered
 * }
 * ```
 *
 * Each project's chart is written next to its README, named for the framework that drew it, e.g.
 * `chart-d2.svg`. Legends are shared by the whole build, so they go in the root project's `atlas/`
 * directory as `legend-d2.svg` and so on. The framework is in the filename, so enabling several at
 * once never causes them to overwrite each other.
 */
public enum class Framework(override val value: String) : StringEnum {
  D2("d2"),
  Graphviz("graphviz"),
  Mermaid("mermaid");

  /** Human-readable name, used in log messages and task names. */
  public val displayName: String
    get() =
      when (this) {
        D2 -> "D2"
        Graphviz -> "Graphviz"
        Mermaid -> "Mermaid"
      }

  override fun toString(): String = value
}
