package atlas.test.scenarios

import atlas.test.D2Scenario

/** Runs `echo` in place of `d2`, so the rendered "chart" is the list of arguments D2 would get. */
internal object D2CliFlags : D2Scenario by D2Basic {
  override val atlasConfig =
    """
    projectTypes.useDefaults()

    d2 {
      pathToD2Command = "echo"
      fileFormat = FileFormat.Ascii
      asciiMode = AsciiMode.Standard
      noXmlTag = true
      omitVersion = true
      timeout = 300

      // layout engine options arrive as a read-only map, which the flags above get added to
      layoutEngine.elk { nodeSelfLoop = 50 }

      fonts {
        regular = file("font.ttf")
        monoBold = file("font.ttf")
      }
    }
    """
      .trimIndent()
}
