@file:Suppress("unused") // public API

package atlas.mermaid

import atlas.core.AtlasDsl
import atlas.core.PropertiesSpec
import org.gradle.api.provider.Property

/** Supports future layout engine configurations. Currently only supports [ElkLayoutSpec]. */
@AtlasDsl
public interface MermaidLayoutSpec : PropertiesSpec {
  /**
   * The name of the layout engine, e.g. "dagre". Unset by default, so Mermaid uses "dagre". Set to
   * "elk" and fixed when configured through [MermaidSpec.elk].
   */
  public val name: Property<String>
}
