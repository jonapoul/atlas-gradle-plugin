package atlas.test.scenarios

import atlas.test.GraphvizScenario

internal object GroovyGraphVizFull : GraphvizScenario by GroovyGraphVizBasic {
  override val atlasConfig =
    """
    graphviz {
      fileFormat = FileFormat.Svg
      layoutEngine = LayoutEngine.Circo
    }
    """
      .trimIndent()
}
