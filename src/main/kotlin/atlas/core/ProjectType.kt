package atlas.core

import java.io.Serializable as JSerializable
import kotlinx.serialization.Serializable as KSerializable

@KSerializable
public data class ProjectType(
  public val name: String,
  public val color: String?,
  public val properties: Map<String, Map<String, String>> = emptyMap(),
) : JSerializable {
  /** The attributes which [framework] should apply to nodes of this type. */
  public fun properties(framework: Framework): Map<String, String> =
    properties[framework.value].orEmpty()
}
