package atlas.core

import atlas.core.internal.RegexSerializer
import java.io.Serializable as JSerializable
import kotlinx.serialization.Serializable as KSerializable
import org.gradle.api.provider.SetProperty
import org.gradle.internal.impldep.org.intellij.lang.annotations.Language

/**
 * API for modifying project names when inserting them into any generated diagrams. For example if
 * your projects are within a heavily-nested `"projects"` directory in your project's root, you
 * might want to call something like:
 * ```kotlin
 * atlas {
 *   pathTransforms {
 *     remove("^:projects:")
 *     replace(pattern = ":", replacement = " ")
 *   }
 * }
 * ```
 *
 * then a path of `":projects:path:to:something"` will be mapped to `"path to something"` for
 * display in the charts. Remember the declarations inside `pathTransforms` are called in descending
 * order.
 *
 * It doesn't support Regex group replacement, just pattern identification.
 */
@AtlasDsl
public interface PathTransformSpec {
  /** Every transform added by [remove] or [replace], applied in the order they were added. */
  public val replacements: SetProperty<Replacement>

  /** Removes every match of [pattern] from the path. */
  public fun remove(@Language("RegExp") pattern: String)

  /** Removes every match of [pattern] from the path. */
  public fun remove(pattern: Regex)

  /** Replaces every match of [pattern] in the path with [replacement]. */
  public fun replace(@Language("RegExp") pattern: String, replacement: String)

  /** Replaces every match of [pattern] in the path with [replacement]. */
  public fun replace(pattern: Regex, replacement: String)
}

/** Model class for a [PathTransformSpec]. */
@KSerializable
public class Replacement(
  @KSerializable(RegexSerializer::class) public val pattern: Regex,
  public val replacement: String,
) : JSerializable
