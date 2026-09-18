package atlas.mermaid

import atlas.core.AtlasDsl
import atlas.core.AtlasSpec
import org.gradle.api.Action
import org.gradle.api.provider.Property

/**
 * Used to configure Mermaid output from Atlas. For barebones output with the default config, call
 * `mermaid()` in the `atlas` block. Or for a more fleshed-out config:
 * ```kotlin
 * atlas {
 *   // other Atlas config
 *
 *   mermaid {
 *     animateLinks = false
 *     look = Look.HandDrawn
 *     theme = Theme.Forest
 *
 *     elk {
 *       ...
 *     }
 *
 *     themeVariables {
 *       ...
 *     }
 *   }
 * }
 * ```
 */
@AtlasDsl
public interface MermaidSpec : AtlasSpec {
  /**
   * Set a custom layout engine for the diagram. Unset by default, but mermaid will fall back to
   * "dagre". Use [MermaidLayoutSpec.properties] to set any custom key/value properties. See
   * [the Mermaid docs](https://mermaid.js.org/config/layouts.html)
   */
  public val layout: MermaidLayoutSpec

  public fun layout(action: Action<MermaidLayoutSpec>)

  /** Configure [ELK](https://www.eclipse.org/elk/) as a layout engine. */
  public fun elk(action: Action<ElkLayoutSpec>? = null)

  /**
   * The visual style of the chart. Unset by default, so Mermaid uses [Look.Classic].
   *
   * Also controlled by the `atlas.mermaid.chart.look` Gradle property. See
   * [the Mermaid docs](https://mermaid.js.org/intro/syntax-reference.html#layout-and-look)
   */
  public val look: Property<Look>

  /**
   * A built-in Mermaid color scheme. Unset by default, so Mermaid uses [Theme.Default].
   *
   * Also controlled by the `atlas.mermaid.chart.theme` Gradle property. See
   * [the Mermaid docs](https://mermaid.js.org/config/theming.html)
   */
  public val theme: Property<Theme>

  /** Configure some global variables to apply to the project diagram itself. */
  public val themeVariables: MermaidThemeVariablesSpec

  public fun themeVariables(action: Action<MermaidThemeVariablesSpec>)

  /**
   * When set to true, all links between projects will have a pretty animation applied to them.
   *
   * WARNING: this doesn't currently work on GitHub or in IntelliJ's Mermaid renderers, but it does
   * work on [mermaid.live](https://mermaid.live). IntelliJ breaks rendering completely, while
   * GitHub still renders the diagram but not the animation. Not really recommended for practical
   * use, but it does look cool when it works.
   *
   * Also controlled by the `atlas.mermaid.chart.animateLinks` Gradle property. Defaults to false.
   * See [the Mermaid docs](https://mermaid.js.org/syntax/flowchart.html#turning-an-animation-on)
   */
  public val animateLinks: Property<Boolean>
}
