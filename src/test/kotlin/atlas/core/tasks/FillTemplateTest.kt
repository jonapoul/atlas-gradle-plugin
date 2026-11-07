package atlas.core.tasks

import assertk.assertThat
import assertk.assertions.isEqualTo
import kotlin.test.Test

internal class FillTemplateTest {
  @Test
  fun `Inserted values are not substituted again`() {
    // given
    val template = "a=__A__ b=__B__"
    val values = mapOf("A" to "__B__", "B" to "x")

    // when
    val result = WriteIndex.fillTemplate(template, values)

    // then
    assertThat(result).isEqualTo("a=__B__ b=x")
  }

  @Test
  fun `Unknown placeholders are left alone`() {
    assertThat(WriteIndex.fillTemplate("__NOPE__", emptyMap())).isEqualTo("__NOPE__")
  }
}
