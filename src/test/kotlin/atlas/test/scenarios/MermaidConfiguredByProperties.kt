package atlas.test.scenarios

import atlas.test.MermaidScenario

/**
 * `themeVariables` comes entirely from gradle properties. `elk` still has to be selected in the
 * DSL, since that's what decides which layout the frontmatter gets, but its values don't.
 */
internal object MermaidConfiguredByProperties : MermaidScenario by MermaidBasic {
  override val atlasConfig =
    """
    mermaid {
      elk {
        mergeEdges = true
      }
    }
    """
      .trimIndent()

  override val gradlePropertiesFile =
    """
    atlas.mermaid.elk.cycleBreakingStrategy=DEPTH_FIRST
    atlas.mermaid.themeVariables.primaryColor=orange
    """
      .trimIndent()
}
