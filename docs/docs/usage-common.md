---
title: Common Config
description: Configuration shared by every Atlas diagram framework
icon: lucide/component
---

# Common Config

## Overview

Configuration is done via the `atlas` Gradle extension function in your `settings.gradle.kts` file. [See here for the KDoc](api/atlas/atlas.core/-atlas-extension/index.html), or [here for the source file](https://github.com/jonapoul/atlas-gradle-plugin/blob/main/src/main/kotlin/atlas/core/AtlasExtension.kt).

``` kotlin
// none of these are required - the below values are the defaults
atlas {
  alsoTraverseUpwards = false
  checkOutputs = true
  displayLinkLabels = false
  generateOnSync = false
  groupProjects = false
  ignoredConfigs = setOf("debug", "kover", "ksp", "test")
  ignoredProjects = emptySet<Regex>()
  printFilesToConsole = false

  projectTypes {
    // ...
  }

  linkTypes {
    // ...
  }

  pathTransforms {
    // ...
  }

  // plus a block per framework you want diagrams from - see below
  d2 { }
  graphviz { }
  mermaid { }
}
```

## Frameworks

Everything above applies to every diagram Atlas generates. Which diagrams those are is decided by the framework blocks you configure - use one, or all three. Each project's chart is written into that project's directory as `chart-<framework>.<ext>`, so they never overwrite each other, and every framework you enable adds its own block to each project's README. Legends belong to the whole build, so they go in the root project's `atlas/` directory as `legend-<framework>.<ext>`.

``` kotlin
atlas {
  // switch a framework on with its defaults
  mermaid()

  // or to enable and apply some non-default configuration
  d2 {
    fileFormat = FileFormat.Svg
  }

  // setting a property directly switches it on too
  graphviz.pathToDotCommand = "/usr/local/bin/dot"

  // any framework not mentioned at all gets no tasks
}
```

The framework-specific configs are documented in:

- [Graphviz](usage-graphviz.md)
- [D2](usage-d2.md)
- [Mermaid](usage-mermaid.md)

## Properties

### alsoTraverseUpwards

``` kotlin
atlas {
  alsoTraverseUpwards = true
}
```

If enabled, the generated project graph will also go "upwards" (showing projects depending on this one) as well as the default "downwards" (projects being consumed by this one).

Also settable with the `atlas.alsoTraverseUpwards` [Gradle property](#gradle-properties).

Examples below from the perspective of `:android:lib`:

<div class="side-by-side">
  <figure>
    <figcaption>Disabled</figcaption>
    <img src="../img/alsoTraverseUpwards-disabled.svg" alt="Disabled">
  </figure>

  <figure>
    <figcaption>Enabled</figcaption>
    <img src="../img/alsoTraverseUpwards-enabled.svg" alt="Enabled">
  </figure>
</div>

### checkOutputs

``` kotlin
atlas {
  checkOutputs = true
}
```

If enabled, a diffing task will be attached to `gradle check`. It will verify that your generated charts match the current state of the project layout, failing if not with a useful error message. Enabled by default.

The generated task name will depend on your chosen framework (`D2`, `Mermaid` or `Graphviz`) and the file type that you're checking (`Chart` or `Legend`), e.g. `checkD2Chart` or `checkGraphvizLegend`.

Even if this option is disabled, the task will still be created, it just won't be attached to `gradle check`.

Also settable with the `atlas.checkOutputs` [Gradle property](#gradle-properties).

D2 and Graphviz only create these tasks when their `intermediateFilesInBuildDir` is set to false, which isn't the default. See the [D2](usage-d2.md#intermediatefilesinbuilddir) and [Graphviz](usage-graphviz.md#intermediatefilesinbuilddir) docs.

### displayLinkLabels

``` kotlin
atlas {
  displayLinkLabels = true

  linkTypes {
    // two built-in link types:
    api(style = LinkStyle.Bold, displayName = "API")
    implementation(LinkStyle.Dashed, color = "red")

    // for using other configurations:
    "compileOnly"(style = LinkStyle.Dotted, displayName = "Compile Only")
    "^commonMain.*"(style = LinkStyle.Dashed, displayName = "Supports case-insensitive regex")
  }
}
```

When enabled, a string label is attached on each project link, showing which configuration caused the link. When true, the `LinkTypeSpec.name` property will be used. Disabled by default.

Requires some `linkTypes` to be declared - otherwise this will have no effect.

Also settable with the `atlas.displayLinkLabels` [Gradle property](#gradle-properties).

<div class="side-by-side">
  <figure>
    <figcaption>Disabled</figcaption>
    <img src="../img/displayLinkLabels-disabled.svg" alt="Disabled">
  </figure>

  <figure>
    <figcaption>Enabled</figcaption>
    <img src="../img/displayLinkLabels-enabled.svg" alt="Enabled">
  </figure>
</div>

### generateOnSync

``` kotlin
atlas {
  generateOnSync = true
}
```

When enabled, syncing your IntelliJ IDE (including Android Studio) will automatically trigger regeneration of your project diagrams. Disabled by default.

Also settable with the `atlas.generateOnSync` [Gradle property](#gradle-properties).

!!! danger

    Be careful enabling this on larger projects - sync time might extend quite a bit.

### groupProjects

``` kotlin
atlas {
  groupProjects = true
}
```

Set to true if you want project charts to gather together groups of projects into bordered containers. E.g. a graph with `":a"`, `":b"` and `":c"` won't be grouped at all because they don't share any path segments, but `":a:b"` and `"a:c"` will be grouped together. A grouped project is labelled with only its last path segment, e.g. `":b"` instead of `":a:b"`, since its container already shows the rest. Disabled by default.

!!! tip

    Remember this will have no effect if your projects aren't nested at all.

!!! warning

    Automatic layout generation will get a bit complicated for larger projects when using grouping.

Also settable with the `atlas.groupProjects` [Gradle property](#gradle-properties).

<div class="side-by-side">
  <figure>
    <figcaption>Disabled</figcaption>
    <img src="../img/groupProjects-disabled.svg" alt="Disabled">
  </figure>

  <figure>
    <figcaption>Enabled</figcaption>
    <img src="../img/groupProjects-enabled.svg" alt="Enabled">
  </figure>
</div>

### ignoredConfigs

``` kotlin
atlas {
  ignoredConfigs = setOf("debug", "kover", "ksp", "test")
}
```

Use this to configure Gradle `Configuration`s to ignore when collating project diagrams. A configuration is ignored if its name contains any of these strings, ignoring case. Gradle does have a load of configurations floating around (depending on your project) - most of which will be practically useless when generating a diagram like this.

Defaults to `setOf("debug", "kover", "ksp", "test")`.

!!! tip "Remember"

    If you don't ignore any configurations, you might end up with double links between projects - or broken builds

This has no Gradle property - it's a set, so it can only be set from the DSL.

### ignoredProjects

``` kotlin
atlas {
  ignoredProjects = setOf(
    ":path:to:some:project".toRegex(),
    ".*:test:.*".toRegex(),
  )
}
```

Use this to leave projects out of your charts, based on their path. Each pattern has to match the whole path. Defaults to an empty set.

This has no Gradle property - the patterns are `Regex` objects, so they can only be set from the DSL.

### printFilesToConsole

``` kotlin
atlas {
  printFilesToConsole = true
}
```

Set to true to print the absolute path of any generated files to the Gradle console output. You can use this to help with scripting, if you like.

Disabled by default.

Also settable with the `atlas.printFilesToConsole` [Gradle property](#gradle-properties).

## Functions

### projectTypes

Use the `projectTypes` block to identify project categories, along with the styling to apply to each one in the output chart. Every framework reads these, and each framework has its own extra styling options (see their docs for details):

- **name** string, shown on the legend
- **color**, as a CSS color string (`"chartreuse"`) or hex string (`"#7FFF00"`)
- **matcher**, used to decide whether a given project should match this type:
    - **pathContains** - checks whether the project path (`":projects:path:to:my:project"`) contains a given string. Case sensitive.
    - **pathMatches** - same as `pathContains`, but uses Regex pattern matching against the whole path. You can also pass an `options` parameter to configure this more specifically, if you need.
    - **hasPluginId** - checks whether the project has applied the given plugin ID string, e.g. `com.android.application` or `org.jetbrains.kotlin.jvm`.

  Only one of these three project matchers should be specified. If none are set, Atlas warns you and ignores the type. If more than one is set, only the first of `pathContains`, `pathMatches` and `hasPluginId` is checked.

Sample usage:

``` kotlin
atlas {
  projectTypes {
    hasPluginId(
      name = "UI",
      color = "#ABC123",
      pluginId = "org.jetbrains.kotlin.plugin.compose",
    )

    pathMatches(
      name = "Data",
      color = "#ABCDEF",
      pathMatches = ".*data$",
    )

    pathContains(name = "Domain", pathContains = "domain") {
      // some custom config can go as a trailing lambda
      // the available options here are the style properties described below
    }
  }
}
```

A few project type quick-access functions are built into Atlas for use in the projectTypes block if you need them:

``` kotlin
atlas {
  projectTypes {
    androidApp()
    androidLibrary()
    java()
    kotlinJvm()
    kotlinMultiplatform()
    other()

    // or useDefaults() to quickly add all of the above
  }
}
```

!!! warning

    Remember that the order of declaring project types does matter! When identifying a project, Atlas will use the first one and go down the list until it finds a match.

The below example shows one project of each of the built-in project types in a sample D2 project layout:

<img class="rounded-corner" src="../img/project-types-default.png" alt="Default project types">

Remember also that you can style any project type with a trailing lambda:

``` kotlin
atlas {
  projectTypes {
    androidApp {
      // read by every framework
      fill = "#ABC123"
      stroke = "black"
      strokeWidth = "10"

      // read by one framework each
      d2Shape = Shape.Hexagon        // D2
      graphvizShape = Shape.Rarrow   // Graphviz
      strokeDashArray = "5 5"        // Mermaid
    }

    kotlinMultiplatform()

    androidLibrary {
      fontColor = "red"
    }
  }
}
```

Every framework's properties are available on every project type, whichever frameworks you've enabled. The ones your configured frameworks don't understand are ignored, and Atlas logs a warning naming them:

```
Warning: project type 'Android App' sets render3D and shadow, which only D2 uses.
Configure the d2 { } block to use them, or remove the config.
```

Where two frameworks mean the same thing by a property, there's one property covering both - `fontColor` writes D2's `style.font-color`, Graphviz's `fontcolor` and Mermaid's `color`. Only genuinely incompatible ones are prefixed, which is currently just `d2Shape` and `graphvizShape`.

### linkTypes

Use this block to configure categories of link to be detected in your project and drawn onto the projects chart. These are detected by Gradle's configuration names. In most cases you'll probably use `api` and `implementation` as your main link types, so these are available as quick-access config functions:

``` kotlin
atlas {
  linkTypes {
    implementation(color = "red")

    api(style = LinkStyle.Bold) {
      // custom style properties here
    }
  }
}
```

!!! tip

    The `style` parameter takes the shared [`LinkStyle`](api/atlas/atlas.core/-link-style/index.html) enum, which each framework draws in its own way. Not all of them can draw all of the styles, so where one can't, Atlas falls back to the closest match and warns you:

    | Style | D2 | Graphviz | Mermaid |
    |--|--|--|--|
    | Solid | ✅ | ✅ | ✅ |
    | Bold | ✅ | ✅ | ✅ |
    | Dashed | ✅ | ✅ | ✅ |
    | Dotted | ✅ | ✅ | drawn as Dashed |
    | Invisible | ✅ | ✅ | ✅ |
    | Tapered | drawn as Solid | ✅ | drawn as Solid |

Besides the default `api` and `implementation`, you can declare links representing other Gradle configurations too:

``` kotlin
atlas {
  linkTypes {
    // All parameters are optional
    "compileOnly"(
      style = LinkStyle.Dotted,
      color = "#ABC123",
      displayName = "Compile-Only",
    )
  }
}
```

!!! warning

    As with project types, remember that the order of declaration matters! Top takes priority. So if you define "implementation" before "testImplementation", you won't get any links matching the latter because they all also match the former.

### pathTransforms

This is a little API for modifying project paths when inserting them into any generated diagrams. For example if your projects are all within a `"projects"` directory in your project's root, you might want to call something like:

``` kotlin
atlas {
  pathTransforms {
    // ":projects:path:to:something" => "path:to:something"
    remove("^:projects:")

    // "path:to:something" => "path to something"
    replace(":", replacement = " ")
  }
}
```

Remember the declarations inside `pathTransforms` are called in descending order. It does not support regex group replacement (yet?) - regex is only used for pattern matching.

## Gradle properties

Most config can also be set from `gradle.properties`, which is handy for changing something in CI without touching the build scripts. The property name follows the path through the DSL:

``` properties
atlas.alsoTraverseUpwards=true
atlas.checkOutputs=false
atlas.displayLinkLabels=true
atlas.generateOnSync=true
atlas.groupProjects=true
atlas.printFilesToConsole=true
atlas.d2.theme=DarkMauve
atlas.d2.layoutEngine=elk
atlas.d2.layoutEngine.elk.nodeSelfLoop=50
atlas.d2.rootStyle.fill=transparent
atlas.d2.themeOverrides.n1=orange
atlas.graphviz.node.shape=box
atlas.graphviz.graph.rankDir=LR
atlas.mermaid.elk.mergeEdges=true
atlas.mermaid.themeVariables.primaryColor=orange
```

The last part is the name you'd write in Kotlin, not the framework's own attribute name. So it's `atlas.graphviz.node.lineColor`, even though the attribute Graphviz ends up seeing is `color`.

Values are parsed and validated the same way as the DSL, so a typo like `atlas.d2.layoutEngine.elk.algorithm=layred` fails the build and tells you the valid options. Lists are comma-separated, e.g. `atlas.d2.layoutEngine.tala.seeds=1,2,3`.

If you set the same thing both ways, the DSL wins. A Gradle property is a default you can override in the build, not the other way round.

A few things have no Gradle property:

- `put("key", value)`, since the key is arbitrary. See [Extra properties](#extra-properties).
- [`d2.fonts`](usage-d2.md#fonts), which takes file paths.
- [`ignoredConfigs`](#ignoredconfigs) and [`ignoredProjects`](#ignoredprojects), which take collections.
- Styles on [projectTypes](#projecttypes) and [linkTypes](#linktypes).

Each property's KDoc names its Gradle property, so [check the API docs](api/index.html) if you're unsure about one.

## Extra properties

Several components in Atlas make use of the [`PropertiesSpec`](api/atlas/atlas.core/-properties-spec/index.html?query=interface%20PropertiesSpec) interface, which allows you to apply arbitrary key-value pair properties to the interfaces that make use of it. Specifically, you can call `put("key", value)`.

Project and link types are shared between frameworks, so their equivalent takes the framework you're aiming at: `put(Framework.D2, "key", value)`.

The intention with this is to let you pass in anything to the scope in question - allowing you to make use of any new APIs in that framework which haven't been explicitly implemented in Atlas. Any usage of these keys is up to you to validate - sometimes the diagram framework won't give you a warning if you pass in an invalid key. If you have some brand-spanking new attribute that you want to apply somewhere:

``` kotlin
atlas {
  projectTypes {
    // no custom config necessary
    androidApp()

    // but if you want to, use a trailing lambda:
    hasPluginId("Custom", pluginId = "com.custom.plugin") {
      // built-in setters for some known properties, depending on the context
      fill = "red"
      render3D = true
    }

    // standard colors/styles in the brackets, everything else in the lambda
    java(color = "orange") {
      // or custom setters for undefined properties, naming the framework which should read them
      put(Framework.D2, "insertKeyHere", "some-value")
    }
  }

  linkTypes {
    // no custom config necessary
    api()

    // again with the trailing lambda:
    implementation {
      animated = true
      strokeWidth = "100"
    }
  }

  d2 {
    layoutEngine {
      elk {
        algorithm = ElkAlgorithm.Layered
        put("anotherElkProperty", 420)
      }
    }

    rootStyle {
      strokeDash = 5
      doubleBorder = true
      put("somenewbool", true)
      put("somenewint", 123)
      put("somenewstring", "yes")
    }
  }
}
```

These examples are not exhaustive - you can do the same with many components in Mermaid and Graphviz too. If you're using some API for customising styles - have a look at the API spec for that class to see what else is available.
