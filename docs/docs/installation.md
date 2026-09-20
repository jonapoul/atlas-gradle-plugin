---
title: Installation
description: Steps for adding Atlas to your Gradle project
icon: lucide/package-open
---

# Installation

Atlas is a **settings plugin**, so everything below goes in `settings.gradle.kts` - not in a build script.

First add the central repository:

``` kotlin
pluginManagement {
  repositories {
    mavenCentral()
  }
}
```

Or for pre-release snapshots builds (the latest state of the main branch in this repo), add the Maven Central snapshots repo:

``` kotlin
pluginManagement {
  repositories {
    maven("https://central.sonatype.com/repository/maven-snapshots/")
  }
}
```

Then apply the plugin in the same file, after the `pluginManagement` block:

``` kotlin
plugins {
  id("dev.jonpoulton.atlas") version "x.y.z"
}
```

Nothing is generated until you pick a framework, which you do by configuring its block. Use as many as you like:

``` kotlin
atlas {
  d2()          // writes chart-d2.svg
  graphviz()    // writes chart-graphviz.svg
  mermaid()     // writes chart-mermaid.mmd
}
```

Each project's chart is written into that project's own directory, next to its README, and the framework is part of the filename, so enabling several at once never has two of them fighting over the same file. Legends are shared by the whole build, so they go in the root project's `atlas/` directory. Passing a configuration block switches the framework on too, so this is enough:

``` kotlin
atlas {
  mermaid {
    theme = Theme.Forest
  }
}
```

Putting it together, a complete `settings.gradle.kts` looks like:

``` kotlin
import atlas.mermaid.Theme

pluginManagement {
  repositories {
    mavenCentral()
  }
}

plugins {
  id("dev.jonpoulton.atlas") version "x.y.z"
}

include(":app", ":core")

atlas {
  projectTypes { useDefaults() }

  mermaid {
    theme = Theme.Forest
  }
}
```

!!! tip "Import Atlas types, don't fully qualify them"

    Inside `settings.gradle.kts` the `atlas` extension accessor shadows the `atlas` package, so
    `atlas.mermaid.Theme.Forest` won't resolve. Add an `import` at the top of the file, as above, and
    refer to the type by its short name.

!!! info "Only the settings file needs to change"

    You don't apply Atlas to your subprojects, and you don't add anything to their build scripts.
    The settings plugin wires up every project in the build for you.

Then generate your diagrams by running:

``` shell
gradle atlasGenerate
```

or validate them by running:

``` shell
gradle atlasCheck
```

That's all you need to get it working! See the next pages for further configuration of each of the above plugins.
