import atlas.core.LinkStyle
import atlas.d2.*

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
    }
  }
}

rootProject.name = "sample-d2"

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
  groupProjects = true

  pathTransforms {
    replace(":module-", replacement = ":")
  }

  projectTypes {
    androidApp {
      d2Shape = Shape.Hexagon
      strokeWidth = "10"
      stroke = "black"
      fontColor = "black"
    }
    kotlinMultiplatform()
    androidLibrary {
      fontColor = "red"
      multiple = true
      italic = true
    }
    kotlinJvm { fillPattern = FillPattern.Lines }
    java { animated = true }
    other()
  }

  linkTypes {
    "jvmMainImplementation"(style = LinkStyle.Bold, color = "orange") {
      opacity = 0.5f
      fontColor = "orange"
      strokeDash = 3
    }
    api(style = LinkStyle.Solid, color = "greenyellow") {
      strokeWidth = "5"
    }
    implementation(style = LinkStyle.Dotted, color = "fuchsia") {
      textTransform = TextTransform.Uppercase
    }
  }

  d2 {
    animateLinks = true
    center = true
    direction = Direction.Right
    fileFormat = FileFormat.Svg
    groupLabelLocation = Location.Border
    groupLabelPosition = Position.BottomCenter
    pad = 100
    sketch = true
    theme = Theme.ShirleyTemple
    themeDark = Theme.DarkMauve

    layoutEngine.elk {
      algorithm = ElkAlgorithm.Layered
      edgeEdgeBetweenLayers = 10
      edgeNodeBetweenLayers = 20
      nodeNodeBetweenLayers = 10
      nodeSelfLoop = 50
    }

    rootStyle {
      stroke = "floralwhite"
      strokeWidth = 3
      strokeDash = 4
      doubleBorder = true
    }

    globalProps {
      arrowType = ArrowType.Box
      fillArrowHeads = true
      font = Font.Mono
      fontSize = 32
      put("(** -> **)[*].style.font-color", "black")
    }
  }
}
