package atlas.core.internal

import org.gradle.api.logging.Logger
import org.gradle.api.problems.ProblemId
import org.gradle.api.problems.ProblemReporter

@Suppress("UnstableApiUsage")
internal class AtlasWarnings(private val logger: Logger, private val reporter: ProblemReporter) {
  fun warn(id: ProblemId, message: String) {
    logger.warn("Warning: $message")
    reporter.report(id) { spec -> spec.contextualLabel(message) }
  }

  companion object {
    val NO_FRAMEWORKS = problemId("atlas-no-frameworks", "No diagram frameworks configured")
    val PROJECT_TYPE_NO_MATCHER =
      problemId("atlas-project-type-no-matcher", "Project type has no matcher")
    val UNUSED_STYLE_PROPERTY =
      problemId(
        "atlas-unused-style-property",
        "Style property not used by any configured framework",
      )
    val UNSUPPORTED_LINK_STYLE =
      problemId("atlas-unsupported-link-style", "Link style not supported by a framework")
    val D2_VERSION_IGNORED = problemId("atlas-d2-version-ignored", "D2 version is ignored")
  }
}
