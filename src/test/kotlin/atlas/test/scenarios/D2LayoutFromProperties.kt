package atlas.test.scenarios

import atlas.test.D2Scenario

/**
 * Layout engine settings supplied through Gradle properties rather than `layoutEngine { }`.
 * `nodeSelfLoop` is set both ways, to prove the DSL wins. Runs `echo` in place of `d2`, like
 * [D2CliFlags], so the rendered "chart" is the list of arguments D2 would get.
 */
internal object D2LayoutFromProperties : D2Scenario by D2Basic {
  override val atlasConfig =
    """
    projectTypes.useDefaults()

    d2 {
      d2Executable = file("/bin/echo")
      fileFormat = FileFormat.Ascii

      layoutEngine.elk.nodeSelfLoop = 25
    }
    """
      .trimIndent()

  override val gradlePropertiesFile =
    """
    atlas.d2.layoutEngine=elk
    atlas.d2.layoutEngine.elk.algorithm=mrtree
    atlas.d2.layoutEngine.elk.nodeNodeBetweenLayers=70
    atlas.d2.layoutEngine.elk.nodeSelfLoop=99
    """
      .trimIndent()
}
