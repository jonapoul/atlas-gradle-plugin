package atlas.core

import org.gradle.api.provider.Property

/** Base interface for a framework-specific configuration. */
public interface AtlasSpec {
  /** The framework's display name, e.g. "D2". */
  public val name: String

  /**
   * The file extension of the generated chart source files: "d2" for D2, "dot" for Graphviz and
   * "mmd" for Mermaid.
   */
  public val fileExtension: Property<String>
}
