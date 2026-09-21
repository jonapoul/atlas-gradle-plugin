package atlas.test.scenarios

import atlas.test.D2Scenario

/** Pinning a version on its own skips the PATH, even though the source is left on `Auto`. */
internal object D2PinnedVersion : D2Scenario by D2Basic {
  override val atlasConfig =
    D2Basic.atlasConfig +
      "\n\n" +
      """
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
internal object D2WithProjectRepositories : D2Scenario by D2Basic {
  override val subprojectBuildFiles =
    D2Basic.subprojectBuildFiles.mapValues { (_, buildFile) ->
      "$buildFile\n\nrepositories { mavenCentral() }"
    }
}

/** Atlas mustn't add a project repository here, or the build fails outright. */
internal object D2FailOnProjectRepos : D2Scenario by D2Basic {
  override val repositoriesMode = "FAIL_ON_PROJECT_REPOS"
}

/** Prints the settings repositories, to show whether Atlas added its own. */
internal object D2PrintingRepositories : D2Scenario by D2Basic {
  override val atlasConfig =
    D2Basic.atlasConfig +
      "\n\n" +
      """
      gradle.settingsEvaluated {
        println("Settings repositories: " + dependencyResolutionManagement.repositories.names)
      }
      """
        .trimIndent()
}

/** Groovy, so the settings file sets the repositories mode with Groovy's property syntax. */
internal object GroovyD2PreferSettings : D2Scenario by GroovyD2Basic {
  override val repositoriesMode = "PREFER_SETTINGS"
}
