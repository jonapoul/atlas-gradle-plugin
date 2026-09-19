package atlas.core.internal

internal fun diff(expected: String, actual: String): String {
  val expectedLines = expected.lines()
  val actualLines = actual.lines()

  // Build LCS (Longest Common Subsequence) table to align matching lines
  val dp = Array(expectedLines.size + 1) { IntArray(actualLines.size + 1) }
  for (i in expectedLines.indices.reversed()) {
    for (j in actualLines.indices.reversed()) {
      dp[i][j] =
        if (expectedLines[i] == actualLines[j]) {
          1 + dp[i + 1][j + 1]
        } else {
          maxOf(dp[i + 1][j], dp[i][j + 1])
        }
    }
  }

  return buildString {
    var i = 0
    var j = 0

    while (i < expectedLines.size || j < actualLines.size) {
      when {
        // Case 1: Lines are equal -> output as unchanged
        i < expectedLines.size && j < actualLines.size && expectedLines[i] == actualLines[j] -> {
          appendLine("    ${expectedLines[i]}")
          i++
          j++
        }

        // Case 2: Prefer consuming a line from actual if it leads to a longer match later. This
        // means a line was
        // inserted in actual that doesn't exist in expected.
        j < actualLines.size && (i == expectedLines.size || dp[i][j + 1] >= dp[i + 1][j]) -> {
          appendLine("--- ${actualLines[j]}")
          j++
        }

        // Case 3: consume a line from expected. This means a line was removed from actual compared
        // to expected
        i < expectedLines.size -> {
          appendLine("+++ ${expectedLines[i]}")
          i++
        }
      }
    }
  }
}

internal class IndentedStringBuilder(private val indentSize: Int) {
  private val sb = StringBuilder()
  private var currentIndent = 0
  private var atLineStart = true
  private val indent
    get() = " ".repeat(currentIndent)

  fun appendLine(line: String = ""): IndentedStringBuilder {
    if (atLineStart) sb.append(indent)
    sb.appendLine(line)
    atLineStart = true
    return this
  }

  fun append(text: String): IndentedStringBuilder {
    if (atLineStart) sb.append(indent)
    sb.append(text)
    atLineStart = false
    return this
  }

  fun indent(block: IndentedStringBuilder.() -> Unit): IndentedStringBuilder {
    currentIndent += indentSize
    block()
    currentIndent -= indentSize
    return this
  }

  override fun toString(): String = sb.toString()
}

internal fun buildIndentedString(
  size: Int = 2,
  block: IndentedStringBuilder.() -> Unit,
): String = IndentedStringBuilder(size).apply(block).toString()
