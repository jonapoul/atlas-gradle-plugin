package atlas.test.scenarios

import atlas.test.Scenario

internal object CustomConfigurationExcluded : Scenario by CustomConfigurations {
  override val atlasConfig =
    """
    projectTypes {
      kotlinJvm()
    }

    ignoredConfigs.add("xyz")
    """
      .trimIndent()
}
