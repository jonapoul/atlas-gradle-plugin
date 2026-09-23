package atlas.d2

import assertk.assertThat
import atlas.test.ScenarioTest
import atlas.test.resolve
import atlas.test.scenarios.D2ConfiguredByProperties
import blueprint.test.assertThatTask
import blueprint.test.buildsSuccessfully
import blueprint.test.contentContains
import blueprint.test.taskSucceeded
import kotlin.test.Test

internal class D2PropertiesTest : ScenarioTest() {
  @Test
  fun `Configure D2 entirely through gradle properties`() = D2ConfiguredByProperties {
    // when
    assertThatTask("writeD2Classes").buildsSuccessfully().taskSucceeded(":writeD2Classes")

    // then
    val classes = resolve("build/atlas/classes-d2.d2")
    assertThat(classes)
      // the theme is an int enum, but the DSL names it, so the property takes the name too
      .contentContains("theme-id: 201")
      // groupLabelLocation used to read a mis-keyed property, so it was silently dropped
      .contentContains("label.near: border-bottom-center")
      .contentContains("center: true")
      .contentContains("pad: 5")
      .contentContains("direction: down")
      // sub-configs take their gradle property name from their path in the DSL
      .contentContains("fill: \"transparent\"")
      .contentContains("N1: \"orange\"")
      .contentContains("***.style.font-size: 24")
  }
}
