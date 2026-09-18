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
 * Generated files are written to a per-framework directory, e.g. `atlas/d2/chart.svg`, so enabling
 * several frameworks at once never causes them to overwrite each other.
 */
public enum class Framework(override val string: String) : StringEnum {
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

  override fun toString(): String = string
}
