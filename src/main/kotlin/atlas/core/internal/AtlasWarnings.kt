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
    private val GROUP = ProblemGroup.create("atlas", "Atlas")

    val NO_FRAMEWORKS = problemId("no-frameworks", "No diagram frameworks configured")
    val PROJECT_TYPE_NO_MATCHER =
      problemId("project-type-no-matcher", "Project type has no matcher")
    val UNUSED_STYLE_PROPERTY =
      problemId("unused-style-property", "Style property not used by any configured framework")
    val UNSUPPORTED_LINK_STYLE =
      problemId("unsupported-link-style", "Link style not supported by a framework")
    val D2_VERSION_IGNORED = problemId("d2-version-ignored", "D2 version is ignored")

    private fun problemId(name: String, displayName: String) =
      ProblemId.create(name, displayName, GROUP)
  }
}
