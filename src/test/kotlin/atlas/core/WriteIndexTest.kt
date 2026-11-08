package atlas.core

import assertk.assertThat
import atlas.test.ScenarioTest
import atlas.test.resolve
import atlas.test.scenarios.MermaidBasic
import blueprint.test.assertThatTask
import blueprint.test.buildsSuccessfully
import blueprint.test.contentContains
import blueprint.test.doesNotExist
import blueprint.test.exists
import blueprint.test.noTasksFailed
import blueprint.test.outputContains
import blueprint.test.outputDoesNotContain
import blueprint.test.taskSucceeded
import blueprint.test.withArgument
import kotlin.test.Test

internal class WriteIndexTest : ScenarioTest() {
  @Test
  fun `Write index linking to every chart`() = MermaidBasic {
    // when
    assertThatTask("atlasGenerate")
      .buildsSuccessfully()
      .taskSucceeded(":atlasWriteIndex")
      .outputContains("Atlas index: file:")

    // then
    assertThat(resolve("build/atlas/index.html"))
      .exists()
      .contentContains(""""path":":a","framework":"Mermaid","url":"../../a/chart-mermaid.mmd"""")
      .contentContains(""""path":":b","framework":"Mermaid","url":"../../b/chart-mermaid.mmd"""")
      .contentContains(""""path":":c","framework":"Mermaid","url":"../../c/chart-mermaid.mmd"""")
      .contentContains("""graph TD\n  _a[\":a\"]""")
      .contentContains(""""fromPath":":a","toPath":":b"""")
  }

  @Test
  fun `Print index URL when nothing changed`() = MermaidBasic {
    // given
    assertThatTask("atlasGenerate").buildsSuccessfully()

    // when, then
    assertThatTask("atlasGenerate")
      .buildsSuccessfully()
      .taskSucceeded(":atlasWriteIndex")
      .outputContains("Atlas index: file:")
  }

  @Test
  fun `Charts are built before the index`() = MermaidBasic {
    // when only the index is requested, then the published charts still pull in their tasks
    assertThatTask(":atlasWriteIndex")
      .buildsSuccessfully()
      .taskSucceeded(":a:writeMermaidChart")
      .taskSucceeded(":b:writeMermaidChart")
      .taskSucceeded(":c:writeMermaidChart")
      .taskSucceeded(":atlasWriteIndex")

    // then
    assertThat(resolve("build/atlas/index.html")).contentContains("""graph TD\n  _a[\":a\"]""")
  }

  @Test
  fun `Don't write index when disabled`() = MermaidBasic {
    // when
    assertThatTask("atlasGenerate")
      .withArgument("-Patlas.generateIndex=false")
      .buildsSuccessfully()
      .noTasksFailed()
      .outputDoesNotContain("Atlas index")

    // then
    assertThat(resolve("build/atlas/index.html")).doesNotExist()
  }
}
