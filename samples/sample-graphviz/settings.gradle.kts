import atlas.core.LinkStyle.Bold
import atlas.core.LinkStyle.Dotted
import atlas.core.LinkStyle.Solid
import atlas.graphviz.ArrowType.Crow
import atlas.graphviz.ArrowType.Ediamond
import atlas.graphviz.ArrowType.None
import atlas.graphviz.Dir.Both
import atlas.graphviz.FileFormat.Svg
import atlas.graphviz.LayoutEngine.Dot
import atlas.graphviz.NodeStyle.Filled
import atlas.graphviz.NodeStyle.Radial
import atlas.graphviz.RankDir.TopToBottom
import atlas.graphviz.Shape.Box
import atlas.graphviz.Shape.Rarrow

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

rootProject.name = "sample-graphviz"

include(
  "sample-app-android",
  "sample-lib-android",
  "sample-lib-java",
  "sample-lib-kotlin-jvm",
  "sample-lib-kotlin-mp",
)

atlas {
  alsoTraverseUpwards = true
  displayLinkLabels = true
  generateOnSync = true
  checkOutputs = true

  projectTypes {
    androidApp {
      graphvizShape = Rarrow
      style = Radial
    }
    kotlinMultiplatform { fontColor = "red" }
    androidLibrary {
      color = "crimson:cyan4"
      gradientAngle = 90
    }
    kotlinJvm()
    java { color = null }
    other { color = "#444444" }
  }

  linkTypes {
    "jvmMainImplementation"(style = Bold, color = "orange") { arrowHead = Crow }
    api(Solid) { weight = 5 }
    implementation(Dotted) {
      dir = Both
      arrowTail = Ediamond
    }
  }

  graphviz {
    fileFormat = Svg
    intermediateFilesInBuildDir = false
    layoutEngine = Dot

    node {
      fontName = "Courier New"
      peripheries = 3
      style = Filled
      shape = Box
      lineColor = "#4C0000"
      fontColor = "white"
    }

    graph {
      bgColor = "MidnightBlue"
      fontSize = "30"
      rankDir = TopToBottom
      rankSep = 1.5
    }

    edge {
      arrowHead = Ediamond
      arrowTail = None
      fontColor = "white"
      labelFloat = false
      linkColor = "red"
    }
  }
}
