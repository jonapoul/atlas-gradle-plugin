package atlas.core

import assertk.assertThat
import assertk.assertions.endsWith
import atlas.test.ScenarioTest
import atlas.test.resolve
import atlas.test.scenarios.MermaidBasic
import atlas.test.scenarios.MermaidWithLinkTypes
import blueprint.test.allTasksSuccessful
import blueprint.test.assertThatTask
import blueprint.test.buildsSuccessfully
import blueprint.test.contentEquals
import blueprint.test.doesNotExist
import blueprint.test.exists
import blueprint.test.taskHadResult
import blueprint.test.withArgument
import kotlin.test.Test

internal class WriteReadmeTest : ScenarioTest() {
  @Test
  fun `Mermaid readme links correctly with default outputs`() = MermaidBasic {
    // when
    assertThatTask("atlasGenerate").buildsSuccessfully().allTasksSuccessful()

    // then
    assertThat(resolve("a/README.md"))
      .exists()
      .contentEquals(
        """
        # a

        <!--region chart-->
        ```mermaid
        graph TD
          _a[":a"]
          _b[":b"]
          _c[":c"]
          _a --> _b
          _a --> _c
        ```
        <!--endregion-->
        """
          .trimIndent(),
      )
    assertThat(resolve("a/README.md").readText()).endsWith("\n")
  }

  @Test
  fun `Write mermaid readme with link types`() = MermaidWithLinkTypes {
    // when
    assertThatTask("atlasGenerate").buildsSuccessfully().allTasksSuccessful()

    // then
    assertThat(resolve("a/README.md"))
      .exists()
      .contentEquals(
        """
        # a

        <!--region chart-->
        ```mermaid
        graph TD
          _a[":a"]
          _b[":b"]
          _c[":c"]
          _a --> _b
          linkStyle 0 stroke:green
          _a --> _c
          linkStyle 1 stroke:#5555FF
        ```

        | Link Types | Style |
        |:--:|:--:|
        | api | Green |
        | implementation | #5555FF |
        | compileOnly | Yellow Dashed |
        <!--endregion-->
        """
          .trimIndent(),
      )
  }

  @Test
  fun `Inject mermaid into existing readme`() = MermaidWithLinkTypes {
    // given
    val readme = resolve("a/README.md")
    assertThat(readme).doesNotExist()
    readme.writeText(
      """
      # My custom readme title

      Some prefix

      <!--region chart-->

      <!--endregion-->

      Some suffix
      """
        .trimIndent(),
    )

    // when
    assertThatTask(":a:writeReadme").buildsSuccessfully().allTasksSuccessful()

    // then
    val expected =
      """
      # My custom readme title

      Some prefix

      <!--region chart-->
      ```mermaid
      graph TD
        _a[":a"]
        _b[":b"]
        _c[":c"]
        _a --> _b
        linkStyle 0 stroke:green
        _a --> _c
        linkStyle 1 stroke:#5555FF
      ```

      | Link Types | Style |
      |:--:|:--:|
      | api | Green |
      | implementation | #5555FF |
      | compileOnly | Yellow Dashed |
      <!--endregion-->

      Some suffix
      """
        .trimIndent()
    assertThat(readme).contentEquals(expected)

    // when we run again and force the regeneration
    assertThatTask(":a:writeReadme")
      .withArgument("--rerun-tasks")
      .buildsSuccessfully()
      .taskHadResult(":a:writeReadme", SUCCESS)
    assertThat(readme).contentEquals(expected)
  }

  @Test
  fun `Edits outside the chart region survive the build cache`() = MermaidBasic {
    // given a generated readme, cached
    assertThatTask(":a:writeReadme").withArgument("--build-cache").buildsSuccessfully()

    // and the user then adds to it
    val readme = resolve("a/README.md")
    readme.writeText(readme.readText() + "\nMy own notes\n")

    // when the task runs again with the same inputs
    assertThatTask(":a:writeReadme")
      .withArgument("--build-cache")
      .buildsSuccessfully()
      .taskHadResult(":a:writeReadme", SUCCESS)

    // then it edits the readme in place, rather than restoring the cached one
    assertThat(readme.readText()).endsWith("My own notes\n")
  }
}
