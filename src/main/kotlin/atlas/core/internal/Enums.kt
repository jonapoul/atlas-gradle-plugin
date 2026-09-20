package atlas.core.internal

import atlas.core.IntEnum
import atlas.core.StringEnum

internal inline fun <reified E> parseEnum(string: String): E where E : StringEnum, E : Enum<E> =
  enumValues<E>().firstOrNull { it.value == string }
    ?: error(
      "No ${E::class.simpleName} matching '$string'. Expected one of ${stringOptionsOf<E>()}."
    )

/**
 * Gradle properties are strings, so an [IntEnum] can be given either as its number or by name -
 * `atlas.d2.theme=201` and `atlas.d2.theme=DarkFlagshipTerrastruct` mean the same thing. The DSL
 * only ever names them, so a property that couldn't do both would be a trap.
 */
internal inline fun <reified E> parseIntEnum(string: String): E where E : IntEnum, E : Enum<E> {
  val number = string.toIntOrNull()
  val match =
    if (number == null) {
      enumValues<E>().firstOrNull { it.name.equals(string, ignoreCase = true) }
    } else {
      enumValues<E>().firstOrNull { it.value == number }
    }
  return match
    ?: error("No ${E::class.simpleName} matching '$string'. Expected one of ${intOptionsOf<E>()}.")
}

internal inline fun <reified E> stringOptionsOf(): String where E : StringEnum, E : Enum<E> =
  enumValues<E>().joinToString { it.value }

internal inline fun <reified E> intOptionsOf(): String where E : IntEnum, E : Enum<E> =
  enumValues<E>().joinToString { "${it.name} (${it.value})" }
