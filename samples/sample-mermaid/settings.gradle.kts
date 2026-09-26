import atlas.core.LinkStyle.Bold
import atlas.core.LinkStyle.Dashed
import atlas.core.LinkStyle.Solid
import atlas.mermaid.ConsiderModelOrder.PreferEdges
import atlas.mermaid.CycleBreakingStrategy.Interactive
import atlas.mermaid.Look.HandDrawn
import atlas.mermaid.NodePlacementStrategy.LinearSegments
import atlas.mermaid.Theme.Forest

pluginManagement {
  includeBuild("../..")

  repositories {
    google {
      mavenContent {
        includeGroupByRegex(".*android.*")
        includeGroupByRegex(".*google.*")
      }
    }
    mavenCentral()
    gradlePluginPortal()
  }
}

plugins {
  id("dev.jonpoulton.atlas")
}

dependencyResolutionManagement {
  repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)

  repositories {
    google()
    mavenCentral()
  }

  versionCatalogs {
    create("libs") {
      from(files("../../gradle/libs.versions.toml"))
      providers
        .environmentVariable("ATLAS_KOTLIN_VERSION")
        .orNull
        ?.takeIf { it.isNotBlank() }
        ?.let { version("kotlin", it) }
      providers
        .environmentVariable("ATLAS_AGP_VERSION")
        .orNull
        ?.takeIf { it.isNotBlank() }
        ?.let { version("agp", it) }
    }
  }
}

rootProject.name = "sample-mermaid"

include(
  "module-android:app",
  "module-android:lib",
  "module-kotlin:kmp",
  "module-kotlin:jvm",
  "module-java",
  "module-other",
)

atlas {
  alsoTraverseUpwards = false
  displayLinkLabels = true
  generateOnSync = true

  projectTypes {
    androidApp()

    kotlinMultiplatform {
      fontColor = "white"
      strokeDashArray = "4 3 2 1"
      fontSize = "20px"
    }

    androidLibrary()
    kotlinJvm()
    java()
    other()
  }

  linkTypes {
    "jvmMainImplementation"(style = Bold, color = "orange")
    api(Solid) { strokeWidth = "5px" }
    implementation(Dashed) { stroke = "aqua" }
  }

  pathTransforms {
    remove(pattern = "^:sample-")
    replace(pattern = "-", replacement = " ")
  }

  mermaid {
    animateLinks = false
    look = HandDrawn
    theme = Forest

    elk {
      mergeEdges = true
      forceNodeModelOrder = true
      nodePlacementStrategy = LinearSegments
      cycleBreakingStrategy = Interactive
      considerModelOrder = PreferEdges
    }

    themeVariables {
      background = "#FFF"
      fontFamily = "arial"
      lineColor = "#55FF55"
      primaryBorderColor = "#FF5555"
      primaryColor = "#ABC123"
      darkMode = true
      fontSize = "30px"

      put("defaultLinkColor", "#5555FF")
    }
  }
}
