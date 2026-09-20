pluginManagement {
  includeBuild("../..")

  repositories {
    mavenCentral()
    gradlePluginPortal()
  }
}

plugins {
  id("dev.jonpoulton.atlas")
}

rootProject.name = "sample-basic"

// Named to match the other samples, but every module is a plain java one - this sample
// configures no project types, so the plugins applied here make no difference to the charts.
include(
  "android:app",
  "android:lib",
  "kotlin:kmp",
  "kotlin:jvm",
  "java",
  "other",
)

// Deliberately bare. This sample is what the layout engine comparison in docs/docs/usage-d2.md
// is rendered from, so it configures nothing beyond the framework itself and every chart comes
// out with Atlas's default styling.
atlas {
  d2()
  mermaid()
  graphviz()
}
