package atlas.test.scenarios

import atlas.test.D2Scenario

/**
 * Ignores any `d2` on the PATH, so the pinned version is downloaded into the TestKit Gradle home.
 */
internal object D2DownloadExecutable : D2Scenario by D2Basic {
  override val atlasConfig =
    """
    projectTypes.useDefaults()

    d2 {
      executableSource = ExecutableSource.Download
    }
    """
      .trimIndent()
}
