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

/** Pinning a version on its own skips the PATH, even though the source is left on `Auto`. */
internal object D2DownloadPinnedVersion : D2Scenario by D2Basic {
  override val atlasConfig =
    """
    projectTypes.useDefaults()

    d2 {
      d2Version = "0.9.0"
    }
    """
      .trimIndent()
}

/**
 * Under Gradle's default `PREFER_PROJECT` mode, a project declaring its own repositories ignores
 * the settings ones, including the D2 releases repository Atlas adds there.
 */
internal object D2DownloadWithProjectRepositories : D2Scenario by D2DownloadExecutable {
  override val subprojectBuildFiles =
    D2Basic.subprojectBuildFiles.mapValues { (_, buildFile) ->
      "$buildFile\n\nrepositories { mavenCentral() }"
    }
}

/** Atlas mustn't add a project repository here, or the build fails outright. */
internal object D2DownloadFailOnProjectRepos : D2Scenario by D2DownloadExecutable {
  override val repositoriesMode = "FAIL_ON_PROJECT_REPOS"
}
