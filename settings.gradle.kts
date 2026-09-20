@file:Suppress("UnstableApiUsage", "AvoidApplyPluginMethod")

rootProject.name = "atlas"

apply("gradle/repositories.gradle.kts")

pluginManagement {
  repositories {
    google {
      mavenContent {
        includeGroupByRegex(".*android.*")
        includeGroupByRegex(".*google.*")
      }
    }
    mavenCentral()
    gradlePluginPortal()
    maven("https://central.sonatype.com/repository/maven-snapshots/") {
      mavenContent { snapshotsOnly() }
    }
  }
}

plugins {
  id("io.github.gmazzo.publications.report") version "1.4.1"
}

dependencyResolutionManagement {
  repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
  repositories {
    google()
    mavenCentral()
  }
}
