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
  /**
   * Background color of the diagram.
   *
   * Also controlled by the `atlas.mermaid.themeVariables.background` Gradle property.
   */
  public var background: String?

  /**
   * Enables dark mode styling.
   *
   * Also controlled by the `atlas.mermaid.themeVariables.darkMode` Gradle property.
   */
  public var darkMode: Boolean?

  /**
   * Font family for all text.
   *
   * Also controlled by the `atlas.mermaid.themeVariables.fontFamily` Gradle property.
   */
  public var fontFamily: String?

  /**
   * Base font size, e.g. "16px".
   *
   * Also controlled by the `atlas.mermaid.themeVariables.fontSize` Gradle property.
   */
  public var fontSize: String?

  /**
   * Default color for lines and edges.
   *
   * Also controlled by the `atlas.mermaid.themeVariables.lineColor` Gradle property.
   */
  public var lineColor: String?

  /**
   * Border color for primary elements.
   *
   * Also controlled by the `atlas.mermaid.themeVariables.primaryBorderColor` Gradle property.
   */
  public var primaryBorderColor: String?

  /**
   * Primary fill color.
   *
   * Also controlled by the `atlas.mermaid.themeVariables.primaryColor` Gradle property.
   */
  public var primaryColor: String?

  /**
   * Text color for primary elements.
   *
   * Also controlled by the `atlas.mermaid.themeVariables.primaryTextColor` Gradle property.
   */
  public var primaryTextColor: String?

  /**
   * Secondary fill color.
   *
   * Also controlled by the `atlas.mermaid.themeVariables.secondaryColor` Gradle property.
   */
  public var secondaryColor: String?

  /**
   * Tertiary fill color.
   *
   * Also controlled by the `atlas.mermaid.themeVariables.tertiaryColor` Gradle property.
   */
  public var tertiaryColor: String?
}
