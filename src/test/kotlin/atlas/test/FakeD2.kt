package atlas.test

import java.io.File

internal const val FAKE_D2 = "fake-d2"

/**
 * Writes a stand-in for `d2` into this directory. Like `d2`, it takes the input and output paths
 * first and writes the output file itself, but the "chart" it writes is the rest of its arguments.
 */
internal fun File.writeFakeD2(): File =
  resolve(FAKE_D2).apply {
    writeText(
      """
      #!/bin/sh
      out="${'$'}2"
      shift 2
      echo "${'$'}@" > "${'$'}out"
      """
        .trimIndent() + "\n",
    )
    setExecutable(true)
  }
