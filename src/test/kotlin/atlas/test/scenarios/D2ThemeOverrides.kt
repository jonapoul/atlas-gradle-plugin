package atlas.test.scenarios

import atlas.test.D2Scenario

internal object D2ThemeOverrides : D2Scenario by D2Basic {
  override val atlasConfig =
    """
    projectTypes.useDefaults()

    d2 {
      theme = Theme.Aubergine

      themeOverrides {
        n1 = "#123456"
        b2 = "orange"
      }

      themeDarkOverrides {
        aa4 = "#abcdef"
      }
    }
    """
      .trimIndent()
}
