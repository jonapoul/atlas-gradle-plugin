package atlas.mermaid

import atlas.core.AtlasDsl
import atlas.core.PropertiesSpec

/**
 * Global diagram configuration properties.
 *
 * See [the Mermaid docs](https://mermaid.js.org/config/theming.html#theme-variables)
 */
@AtlasDsl
public interface MermaidThemeVariablesSpec : PropertiesSpec {
  /** Background color of the diagram. */
  public var background: String?

  /** Enables dark mode styling. */
  public var darkMode: Boolean?

  /** Font family for all text. */
  public var fontFamily: String?

  /** Base font size, e.g. "16px". */
  public var fontSize: String?

  /** Default color for lines and edges. */
  public var lineColor: String?

  /** Border color for primary elements. */
  public var primaryBorderColor: String?

  /** Primary fill color. */
  public var primaryColor: String?

  /** Text color for primary elements. */
  public var primaryTextColor: String?

  /** Secondary fill color. */
  public var secondaryColor: String?

  /** Tertiary fill color. */
  public var tertiaryColor: String?
}
