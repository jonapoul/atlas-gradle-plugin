package atlas.core.internal

import org.gradle.api.logging.Logger
import org.gradle.api.problems.ProblemGroup
import org.gradle.api.problems.ProblemId
import org.gradle.api.problems.ProblemReporter

@Suppress("UnstableApiUsage")
internal class AtlasWarnings(private val logger: Logger, private val reporter: ProblemReporter) {
  fun warn(id: ProblemId, message: String) {
    logger.warn("Warning: $message")
    reporter.report(id) { spec -> spec.contextualLabel(message) }
  }

  companion object {
    private val PROBLEM_GROUP: ProblemGroup = ProblemGroup.create("atlas-group", "Atlas")

    val NO_FRAMEWORKS =
      problemId(id = "no-frameworks", description = "No diagram frameworks configured")

    val PROJECT_TYPE_NO_MATCHER =
      problemId(id = "project-type-no-matcher", description = "Project type has no matcher")

    val UNUSED_STYLE_PROPERTY =
      problemId(
        id = "unused-style-property",
        description = "Style property not used by any configured framework",
      )
    val UNSUPPORTED_LINK_STYLE =
      problemId(
        id = "unsupported-link-style",
        description = "Link style not supported by a framework",
      )

    val D2_VERSION_IGNORED =
      problemId(id = "d2-version-ignored", description = "D2 version is ignored")

    val PROBLEM_DOESNT_EXIST =
      problemId(
        id = "check-doesnt-exist",
        description = "Expected file doesn't exist",
      )

    val PROBLEM_NEEDS_REGENERATION =
      problemId(
        id = "check-regenerate",
        description = "Chart file needs regenerating",
      )

    private fun problemId(id: String, description: String): ProblemId =
      ProblemId.create("atlas-$id", description, PROBLEM_GROUP)
  }
}
