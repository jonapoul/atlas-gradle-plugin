---
title: D2
description: Configuring D2 diagrams in Atlas
icon: lucide/columns-2
---

# D2 Config

## Overview

See [here for the official D2 docs](https://d2lang.com/tour/intro/) or [here for an online playground](https://play.d2lang.com/). By default Atlas uses `d2` from the system PATH, and downloads it if it's not there. See [`executableSource`](#executablesource) to change that.

[See here for D2 installation steps](https://d2lang.com/tour/install/).

D2-specific configuration is performed from the `d2 { }` block within the base `atlas` extension function:

``` kotlin
plugins {
  id("dev.jonpoulton.atlas") version "x.y.z"
}

atlas {
  // other Atlas config - see common config docs

  d2 {
    animateInterval = 10
    animateLinks = true
    asciiMode = AsciiMode.Standard
    center = true
    d2Executable = file("/path/to/d2")
    d2Version = "0.9.0"
    direction = Direction.Down
    executableSource = ExecutableSource.Auto
    fileFormat = FileFormat.Svg
    groupLabelLocation = Location.Inside
    groupLabelPosition = Position.TopCenter
    intermediateFilesInBuildDir = true
    noXmlTag = true
    omitVersion = true
    pad = 5
    scale = 0.5f
    sketch = true
    theme = Theme.ColorblindClear
    themeDark = Theme.DarkMauve
    timeout = 300

    fonts {
      // ...
    }

    rootStyle {
      // ...
    }

    themeOverrides {
      // ...
    }

    themeDarkOverrides {
      // ...
    }

    globalProps {
      // ...
    }

    layoutEngine {
      // ...
    }
  }
}
```

## Generated files

Atlas will generate a `classes-d2.d2` file in the root project's `build/atlas` folder. This contains any shared styling, used between all child charts. It will also generate a `chart-d2.d2` file in each subproject's `build/atlas` folder, and a `chart-d2.XXX` file in the subproject's own directory, the latter's file extension depending on [fileFormat](#fileformat). Set [intermediateFilesInBuildDir](#intermediatefilesinbuilddir) to false to keep the `.d2` files next to the rendered image instead.

## Properties

### animateInterval

``` kotlin
atlas {
  d2 {
    animateInterval = 100
  }
}
```

Only used if [fileFormat](#fileformat) is set to `FileFormat.Gif`. Optional - leave it unset and D2 falls back to its own default of 1000ms.

### animateLinks

``` kotlin
atlas {
  d2 {
    animateLinks = true
  }
}
```

When enabled, dashed and dotted links between project nodes will be animated. Disabled by default.

!!! warning

    This will only work for "animatable" [output formats](#fileformat): either SVG or GIF. If you choose a different output, you'll get a Gradle warning to tell you about it when syncing the IDE.

<div class="side-by-side">
  <figure>
    <img src="../img/d2-animateLinks-disabled.svg" alt="Disabled">
    <figcaption>Disabled</figcaption>
  </figure>

  <figure>
    <img src="../img/d2-animateLinks-enabled.svg" alt="Enabled">
    <figcaption>Enabled</figcaption>
  </figure>
</div>

### asciiMode

``` kotlin
atlas {
  d2 {
    asciiMode = AsciiMode.Standard
  }
}
```

Only used if [fileFormat](#fileformat) is set to `FileFormat.Ascii`. `AsciiMode.Extended` draws the chart with Unicode box-drawing characters, like the example under [fileFormat](#fileformat). `AsciiMode.Standard` sticks to basic ASCII characters like `+`, `-` and `|`. Optional - leave it unset and D2 uses `Extended`. Requires D2 0.7.1 or newer.

### center

``` kotlin
atlas {
  d2 {
    center = true
  }
}
```

This flag centers the SVG within the containing viewbox. Doesn't really give an obvious change in me experience, but ¯\_(ツ)_/¯. [See here](https://d2lang.com/tour/vars/#configuration-variables).

### d2Executable

``` kotlin
atlas {
  d2 {
    d2Executable = file("/custom/path/to/d2")
  }
}
```

Use a specific `d2` executable. When set, this always wins over [`executableSource`](#executablesource).

### d2Version

``` kotlin
atlas {
  d2 {
    d2Version = "0.9.0"
  }
}
```

The D2 version to download. Unset by default, in which case Atlas may use `d2` from the PATH, and otherwise downloads the version this release of Atlas is tested against. Setting it means you want exactly that version, so the PATH is skipped and it's always downloaded, unless [`executableSource`](#executablesource) is `Path`. D2 is downloaded from [its GitHub releases](https://github.com/d2lang/d2/releases) as a regular Gradle dependency, so it's cached in the Gradle user home and only downloaded once per version per machine. It also follows Gradle's usual rules for proxies, `--offline` and [dependency verification](https://docs.gradle.org/current/userguide/dependency_verification.html).

When a download is needed, Atlas adds an Ivy repository called `atlasD2Releases` to `dependencyResolutionManagement` for it. Builds that use a `d2` from the PATH or [`d2Executable`](#d2executable) get no repository at all. It only serves D2, and D2 is only fetched from it, so it doesn't change how anything else in your build resolves. Under Gradle's default `PREFER_PROJECT` repositories mode, a project that declares its own repositories ignores the settings ones, so Atlas adds the repository to those projects as well.

!!! info "Dependency verification"

    If your build verifies dependencies, the D2 tarball needs an entry like any other dependency. Running with `--write-verification-metadata sha256` adds it.

### direction

``` kotlin
atlas {
  d2 {
    direction = Direction.Down
  }
}
```

Sets the flow direction of the dependency chart. Defaults to `Direction.Down`.

<div class="svg-grid">
  <figure class="svg-item">
    <img src="../img/d2-direction-up.svg" alt="Up">
    <figcaption>Direction.Up</figcaption>
  </figure>

  <figure class="svg-item">
    <img src="../img/d2-direction-down.svg" alt="Down">
    <figcaption>Direction.Down</figcaption>
  </figure>

  <figure class="svg-item">
    <img src="../img/d2-direction-left.svg" alt="Left">
    <figcaption>Direction.Left</figcaption>
  </figure>

  <figure class="svg-item">
    <img src="../img/d2-direction-right.svg" alt="Right">
    <figcaption>Direction.Right</figcaption>
  </figure>
</div>

### executableSource

``` kotlin
atlas {
  d2 {
    executableSource = ExecutableSource.Download
  }
}
```

Where to find `d2` when [`d2Executable`](#d2executable) isn't set:

- `Auto` (default): use `d2` from the system PATH if it's there, otherwise download it. If [`d2Version`](#d2version) is set, always download that version instead.
- `Path`: only use the system PATH, and never touch the network. [`d2Version`](#d2version) is ignored.
- `Download`: always download, using [`d2Version`](#d2version) if set. Best for CI, or anywhere the output is diffed, since D2's SVG output changes between releases.

Also settable with the `atlas.d2.executableSource` Gradle property, e.g. `-Patlas.d2.executableSource=path`. With `--offline`, a build fails unless that D2 version is already in Gradle's cache.

!!! note

    If you set [`d2Version`](#d2version) older than 0.9.0, PNG, PDF, PPTX and GIF output need a Chromium download that D2 can't do from inside a Gradle build. See [`fileFormat`](#fileformat).

### fileFormat

``` kotlin
atlas {
  d2 {
    fileFormat = FileFormat.Svg
  }
}
```

Defaults to SVG. Available options:

``` kotlin
FileFormat.Svg
FileFormat.Png
FileFormat.Pdf
FileFormat.Pptx
FileFormat.Gif
FileFormat.Ascii
```

!!! warning

    All six formats work out of the box on D2 0.9.0 and above, which renders PNG, PDF, PPTX and GIF with its own built-in rasteriser. On older versions those four go through a bundled Chromium that D2 offers to download the first time you ask for one of them, prompting `D2 needs to install Chromium vX. Continue? (y/N)` on stdin - which a Gradle build has no way to answer, so the task fails with `failed to read user input: EOF`. If you're stuck on an older D2, run `d2` by hand once to accept the download, or stay on SVG. [See here for the background](https://github.com/d2lang/d2/issues/2502#issuecomment-3305144085).

For reference, an ASCII chart looks like below. It (hopefully obviously) doesn't support more complex features like coloring, animation, etc. It is pretty cool though!

```
      ┌─────────────┐
      │:android:app │
      │             │
      └─────────────┘
           │   │
      ┌────┘   └───────┐
      │                │
      │                ▼
      │          ┌────────────┐
      │          │:kotlin:kmp │
      │          │            │
      │          └────────────┘
      │             │  │   │
      │   ┌─────────┘  │   └───┐
      │   │            │       │
      ▼   ▼            ▼       │
 ┌─────────────┐ ┌──────┐      │
 │:android:lib │ │:java │      │
 │             │ │      │      │
 └─────────────┘ └──────┘      │
      │   │          │         │
      │   └──────────│─────┐   │
      │              │     │   │
      └─────┐  ┌─────┘     │   │
            │  │           │   │
            ▼  ▼           ▼   ▼
         ┌───────┐     ┌────────────┐
         │:other │     │:kotlin:jvm │
         │       │     │            │
         └───────┘     └────────────┘
```

### groupLabelLocation & groupLabelPosition

``` kotlin
atlas {
  groupProjects = true

  d2 {
    groupLabelLocation = Location.Outside
    groupLabelPosition = Position.TopCenter
  }
}
```

Only does anything if `atlas.groupProjects = true`.

<div class="svg-grid">
  <figure class="svg-item">
    <img src="../img/d2-groupLabel-border-left.svg" alt="Border center left">
    <figcaption>Border center left</figcaption>
  </figure>

  <figure class="svg-item">
    <img src="../img/d2-groupLabel-inside-bottomright.svg" alt="Inside bottom right">
    <figcaption>Inside bottom right</figcaption>
  </figure>

  <figure class="svg-item">
    <img src="../img/d2-groupLabel-outside-bottomcenter.svg" alt="Outside bottom center">
    <figcaption>Outside bottom center</figcaption>
  </figure>

  <figure class="svg-item">

  </figure>
</div>

### intermediateFilesInBuildDir

``` kotlin
atlas {
  d2 {
    intermediateFilesInBuildDir = true
  }
}
```

When enabled, the `chart-d2.d2` and `classes-d2.d2` files go in `build/atlas/` instead of the project directory and the root project's `atlas/` folder, so you don't need to commit them. The rendered image stays alongside the project.

[checkOutputs](usage-common.md#checkoutputs) only compares the `.d2` files, so no D2 check tasks are registered while this is enabled.

Enabled by default.

### noXmlTag

``` kotlin
atlas {
  d2 {
    noXmlTag = true
  }
}
```

Leaves the `<?xml ... ?>` tag out of the generated SVG, which helps if you embed it straight into HTML. Only used if [fileFormat](#fileformat) is `FileFormat.Svg`. Defaults to false.

### omitVersion

``` kotlin
atlas {
  d2 {
    omitVersion = true
  }
}
```

Leaves the D2 version out of the generated chart. Handy if you commit your charts, since otherwise upgrading D2 changes every one of them. Defaults to false.

### pad

``` kotlin
atlas {
  d2 {
    pad = 100
  }
}
```

Should probably be called `padding` for clarity, but kept as `pad` for consistency with the underlying D2 config parlance. Units are in pixels, and default value is 100.

<div class="side-by-side">
  <figure>
    <img src="../img/d2-pad-0.svg" alt="0">
    <figcaption>0 padding</figcaption>
  </figure>

  <figure>
    <img src="../img/d2-pad-200.svg" alt="200">
    <figcaption>200 padding</figcaption>
  </figure>
</div>

### scale

``` kotlin
atlas {
  d2 {
    scale = 0.5f
  }
}
```

Scales the rendered chart, passed straight through to D2's `--scale`. Left unset, D2 fits SVGs to the viewer's screen and renders every other format at its natural size, so setting this to `1` turns that SVG fitting off and anything below `1` shrinks the output. Handy for PNGs of large charts, which D2 renders at full diagram size by default.

### sketch

``` kotlin
atlas {
  d2 {
    sketch = true
  }
}
```

Draws the chart in an excalidraw-like format, with pseudo-handwritten font and shaded backgrounds on shapes. Defaults to false.

!!! warning

    For awareness: enabling this property will inflate the size of the generated SVG file by quite a bit. The example below is 71kB, and disabling the flag drops it down to 15kB. It does look pretty nice, though.

![D2 sketch](img/d2-sketch.svg)

### theme & themeDark

``` kotlin
atlas {
  d2 {
    theme = Theme.ShirleyTemple
    themeDark = Theme.DarkMauve
  }
}
```

D2 comes with a suite of lovely built-in color schemes which you can apply to your charts. If you generate an SVG, you can also apply a separate dark theme to allow the file to support browsers in both dark and light screen display modes. Alternatively, you can specify one of the two dark themes in the `theme` parameter to force an always-dark theme, regardless of website/browser settings.

[See here for the full list of themes](https://d2lang.com/tour/themes/) in the D2 documentation. Some examples are shown below, where the bottom-right one will toggle between light and dark based on the browser theme. Try switching the theme from the site toolbar (or change in your browser's settings) to see!

!!! info

    If you set a specific color for a project type using the `projectTypes` API, it will overwrite the theme color for that node. Same for link colors.


!!! tip

    Remember also that you can override the chart's background color using [rootStyle's](#rootstyle) `fill` property.

<div class="svg-grid">
  <figure class="svg-item">
    <img src="../img/d2-theme-default.svg" alt="Default">
    <figcaption>Default</figcaption>
  </figure>
  <figure class="svg-item">
    <img src="../img/d2-theme-shirley.svg" alt="Shirley Temple">
    <figcaption>Shirley Temple</figcaption>
  </figure>
  <figure class="svg-item">
    <img src="../img/d2-theme-darkmauve.svg" alt="Dark Mauve">
    <figcaption>Dark Mauve</figcaption>
  </figure>
  <figure class="svg-item">
    <img src="../img/d2-theme-both.svg" alt="Light/Dark">
    <figcaption>Aubergine / Dark Flagship Terrastruct</figcaption>
  </figure>
</div>

### timeout

``` kotlin
atlas {
  d2 {
    timeout = 300
  }
}
```

The maximum number of seconds D2 can run for before the task fails. Optional - leave it unset and D2 uses its own default of 120. Worth raising if you have a very large chart. Set it to 0 to turn the limit off.

## Functions

### layoutEngine

``` kotlin
atlas {
  d2 {
    layoutEngine {
      // only call one of the below config functions!
      // if you call multiple, only the last one will be used.
      dagre {
        nodeSep = 60
        edgeSep = 20
      }

      elk {
        algorithm = ElkAlgorithm.Layered
        edgeNodeBetweenLayers = 40
        nodeNodeBetweenLayers = 70
        nodeSelfLoop = 50

        // padding is either a raw D2 string, or set with one of the helpers
        padding = "[top=50,left=50,bottom=50,right=50]"
        padding(all = 50)
        padding(horizontal = 50, vertical = 50)
        padding(top = 50, left = 50, bottom = 50, right = 50)
      }

      tala {
        seeds = listOf(1L, 2L, 3L)
      }
    }
  }
}
```

Defines the underlying engine used by D2 to organise the project nodes in each chart. [See this link in the D2 docs for more detailed information](https://d2lang.com/tour/layouts/). The available options are:

- **Dagre**: default option.
- **Elk**: Framework from Eclipse for diagram generation - also supported by [Mermaid](usage-mermaid.md).
- **Tala**: D2's own engine, built for software architecture diagrams. It was a paid closed-source plugin until D2 0.9.0, which open sourced it and bundled it into the standard installation, so it now works out of the box. Its only setting is `seeds`: D2 lays the chart out once per seed and keeps the best complete result, so more seeds gives a tidier chart and a slower build. Layout is deterministic for a given set of seeds, which matters if you commit your charts. D2 takes at most 16 of them.

Screenshots below are with all default settings.

The engine and each of its settings can also come from [Gradle properties](usage-common.md#gradle-properties): `atlas.d2.layoutEngine=elk`, then `atlas.d2.layoutEngine.elk.nodeSelfLoop=50` and so on for the rest.

<div class="side-by-side">
  <figure>
    <figcaption>Dagre</figcaption>
    <img src="../img/d2-layoutEngine-dagre.svg" alt="Dagre">
  </figure>

  <figure>
    <figcaption>Elk</figcaption>
    <img src="../img/d2-layoutEngine-elk.svg" alt="Elk">
  </figure>

  <figure>
    <figcaption>Tala</figcaption>
    <img src="../img/d2-layoutEngine-tala.svg" alt="Tala">
  </figure>
</div>

### rootStyle

``` kotlin
atlas {
  d2 {
    rootStyle {
      fill = "white"
      fillPattern = FillPattern.Grain
      stroke = "firebrick"
      strokeWidth = 5
      strokeDash = 3
      doubleBorder = true
    }
  }
}
```

A set of style properties to be applied to the chart itself. The most common one in my experience is `fill`, which you can set to `transparent` or any other CSS color or hex string.

[Check the D2 docs for the possible values of these properties](https://d2lang.com/tour/style/#root). All are optional.

Each one is also a [Gradle property](usage-common.md#gradle-properties), e.g. `atlas.d2.rootStyle.fill=transparent`.

### fonts

``` kotlin
atlas {
  d2 {
    fonts {
      regular = file("fonts/Inter-Regular.ttf")
      italic = file("fonts/Inter-Italic.ttf")
      bold = file("fonts/Inter-Bold.ttf")
      semibold = file("fonts/Inter-SemiBold.ttf")
      mono = file("fonts/JetBrainsMono-Regular.ttf")
      monoBold = file("fonts/JetBrainsMono-Bold.ttf")
      monoItalic = file("fonts/JetBrainsMono-Italic.ttf")
      monoSemibold = file("fonts/JetBrainsMono-SemiBold.ttf")
    }
  }
}
```

Custom `.ttf` files to draw the chart's text with. All are optional, and any you leave unset keep D2's bundled font: Source Sans Pro for the regular ones and Source Code Pro for the mono ones. The mono fonts are used when [globalProps](#globalprops) sets `font = Font.Mono`, and need D2 0.7.1 or newer.

Unlike most properties, these can't be set through Gradle properties.

### themeOverrides & themeDarkOverrides

``` kotlin
atlas {
  d2 {
    theme = Theme.Aubergine
    themeDark = Theme.DarkMauve

    themeOverrides {
      n1 = "#123456"
      b2 = "orange"
    }

    themeDarkOverrides {
      aa4 = "#abcdef"
    }
  }
}
```

Replaces individual colors of [theme and themeDark](#theme-themedark) without restyling each node yourself. Each value is a CSS color name or a hex code. The available codes are `n1` to `n7` (neutrals), `b1` to `b6` (base colors, used for containers), `aa2`, `aa4`, `aa5`, `ab4` and `ab5` (alternative colors). [See the D2 docs](https://d2lang.com/tour/themes/) for more.

`themeDarkOverrides` only works for SVGs, same as `themeDark`.

Each color is also a [Gradle property](usage-common.md#gradle-properties), e.g. `atlas.d2.themeOverrides.n1=orange` or `atlas.d2.themeDarkOverrides.aa4=#abcdef`.

### globalProps

``` kotlin
atlas {
  d2 {
    globalProps {
      arrowType = ArrowType.Circle
      fillArrowHeads = false
      font = Font.Mono
      fontSize = 15
    }
  }
}
```

Style properties to be applied to all nodes (project shapes) and links by default, unless overridden by `linkTypes` or `projectTypes` config.

Each one is also a [Gradle property](usage-common.md#gradle-properties), e.g. `atlas.d2.globalProps.fontSize=15`. The globs below aren't, since `put` takes an arbitrary key.

!!! note

    Be aware that `font` can only take the value `Mono`. If you want the default font, leave it alone or set to null.

!!! tip

    As a bonus, in `globalProps` you can also make use of D2's wonderfully-complicated "globs" feature to apply some style to all matching nodes/links in the chart. An example from the sample-d2 project in this repo, which sets all text on link labels to black:

    ``` kotlin
    atlas {
      d2 {
        globalProps {
          put("(** -> **)[*].style.font-color", "black")
        }
      }
    }
    ```

    I'm not going to document all this in here, but [take a look at the D2 docs if you're interested](https://d2lang.com/tour/globs/). If you want to add them to your chart, `globalProps` is probably(?) the best place for it. This will add the glob property to the global `classes.d2` file, which gets auto-imported into all project chart diagrams.
