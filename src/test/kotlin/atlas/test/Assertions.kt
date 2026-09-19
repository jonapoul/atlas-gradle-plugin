package atlas.test

import assertk.Assert
import assertk.assertions.isEqualTo
import assertk.assertions.support.expected
import assertk.assertions.support.show
import blueprint.test.Scenario as RunningScenario
import java.io.File

internal fun RunningScenario.resolve(path: String): File = rootDir.resolve(path)

internal fun <T> Assert<Set<T>>.isEqualToSet(vararg expected: T) = isEqualTo(expected.toSet())

internal fun Assert<String>.contains(text: String): Assert<String> = transform { actual ->
  if (text !in actual) expected("to contain:${show(text)} but was:${show(actual)}")
  actual
}
