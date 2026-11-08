package atlas.test

import assertk.Assert
import assertk.assertions.isEqualTo
import assertk.assertions.support.expected
import assertk.assertions.support.show
import blueprint.test.Scenario as BlueprintScenario
import java.io.File
import org.gradle.testkit.runner.BuildResult

internal fun BlueprintScenario.resolve(path: String): File = rootDir.resolve(path)

internal fun <T> Assert<Set<T>>.isEqualToSet(vararg expected: T) = isEqualTo(expected.toSet())

internal fun Assert<String>.contains(text: String): Assert<String> = transform { actual ->
  if (text !in actual) expected("to contain:${show(text)} but was:${show(actual)}")
  actual
}

/** The tasks a `--dry-run` build would have run, in no particular order. */
internal fun Assert<BuildResult>.dryRunTasks(): Assert<List<String>> = transform { result ->
  result.output.lines().filter { it.endsWith(" SKIPPED") }.map { it.removeSuffix(" SKIPPED") }
}
