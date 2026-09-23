package atlas.d2.internal

import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.assertions.isNotEqualTo
import assertk.assertions.startsWith
import atlas.core.LinkType
import kotlin.test.Test

internal class ClassIdTest {
  @Test
  fun `Plain configuration names are used as-is`() {
    assertThat(LinkType(configuration = "implementation").classId).isEqualTo("link-implementation")
  }

  @Test
  fun `Regex configurations which strip to the same key get different classes`() {
    val first = LinkType(configuration = "^.*Api$").classId
    val second = LinkType(configuration = ".*Api").classId

    assertThat(first).startsWith("link-Api-")
    assertThat(second).startsWith("link-Api-")
    assertThat(first).isNotEqualTo(second)
  }
}
