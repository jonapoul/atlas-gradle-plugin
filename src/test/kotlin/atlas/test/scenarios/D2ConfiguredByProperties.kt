package atlas.test.scenarios

import atlas.test.Scenario

/** The whole D2 setup supplied through gradle properties instead of the `d2 { }` block. */
internal object D2ConfiguredByProperties : Scenario by D2Basic {
  override val gradlePropertiesFile =
    """
    atlas.d2.center=true
    atlas.d2.direction=down
    atlas.d2.groupLabelLocation=border
    atlas.d2.groupLabelPosition=bottom-center
    atlas.d2.pad=5
    atlas.d2.theme=DarkFlagshipTerrastruct
    atlas.d2.globalProps.fontSize=24
    atlas.d2.rootStyle.fill=transparent
    atlas.d2.themeOverrides.n1=orange
    """
      .trimIndent()
}
