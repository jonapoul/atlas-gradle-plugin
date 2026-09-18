# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

Atlas is a Gradle **settings** plugin for generating diagrams of modular project dependencies. It supports three rendering frameworks: D2, Graphviz, and Mermaid. The plugin is built with Kotlin and targets Gradle 9+ with full configuration cache and isolated projects support.

**Key Characteristics:**
- A single-project build: the whole plugin lives in `src/`, published as `dev.jonpoulton.atlas:plugin`
- One plugin id, `dev.jonpoulton.atlas`, applied in `settings.gradle.kts`. Users pick frameworks by configuring `d2 { }`, `graphviz { }` and/or `mermaid { }` in the `atlas` extension - any combination is allowed
- Java 21 minimum (see the root `.java-version` file)
- Uses [Blueprint](https://github.com/jonapoul/blueprint) (`dev.jonpoulton.blueprint:core`) for Gradle DSL shortcuts
- Uses Gradle configuration cache, parallel execution, and isolated projects
- Published to Maven Central under `dev.jonpoulton.atlas`

## Commands

### Building and Testing
```bash
./gradlew build                    # Build everything
./gradlew test                     # Run all tests
./gradlew check                    # Run all checks including detekt, licensee and the ABI dump
```

### Single Test Execution
Tests use JUnit 6. To run a single test class:
```bash
./gradlew test --tests "atlas.core.WriteProjectTreeTest"
```

To run a specific test method:
```bash
./gradlew test --tests "atlas.core.WriteProjectTreeTest.Single links for diamond"
```

### Code Quality
```bash
./gradlew detektCheck              # Run static analysis
./gradlew licensee                 # Validate dependency licenses

# Formatting, using ktfmt with Google style
./scripts/ktfmt.sh                 # format files changed since main
./scripts/ktfmt.sh check           # check-only
./scripts/ktfmt.sh --force         # all files
```

### Documentation
This task will take a while, so don't run unless really necessary:
```bash
./gradlew dokkaGeneratePublicationHtml --rerun-tasks --no-build-cache  # Generate API docs to docs/api/
```

### Plugin Development
The plugin is tested from its own test source set (`src/test`), so tests can see `internal` declarations. Tests use Gradle TestKit to verify plugin behavior in realistic project structures.

When the public API changes, refresh the ABI dump with `./gradlew updateKotlinAbi`, otherwise `check` fails.

## Architecture

### Package Structure

One package per concern: `atlas.core` for everything framework-agnostic, then `atlas.d2`,
`atlas.graphviz` and `atlas.mermaid`. Each follows the same layout:

| Location | Holds |
|---|---|
| `atlas.<pkg>` | user-facing API - the `*Spec` config interfaces and shared value types |
| `atlas.<pkg>.internal` | implementation, including the `*SpecImpl`s and each framework's `*Tasks` registrar |
| `atlas.<pkg>.tasks` | task types |

So a framework is reached through exactly two entry points: `<Framework>Spec` for its config, and a
`<Framework>Tasks` object implementing `FrameworkTasks` for its task registration. Adding a
framework means adding those two plus a `Framework` enum entry.

`atlas.core` additionally holds `AtlasPlugin` (a `Plugin<Settings>`, the single entry point) and
`AtlasExtension`; `atlas.core.internal` holds the wiring layer described in the sections below.

Anything not part of the user-facing API is Kotlin `internal` - there's no opt-in annotation. Tests
live in `src/test`, with `ScenarioTest` + `atlas.test.scenarios.*` driving TestKit builds.

### Shared Project and Link Types

`projectTypes` and `linkTypes` are declared once and rendered by every configured framework. Each
spec carries the union of all three frameworks' style properties: shared meanings get one property
mapping to several framework keys (`fontColor` -> D2 `style.font-color`, Graphviz `fontcolor`,
Mermaid `color`), and only genuine clashes are prefixed (`d2Shape`, `graphvizShape`). `StyleProperties`
holds one attribute map per framework and records which DSL properties were set, so `Warnings.kt`
can warn about properties no configured framework will read.

### Plugin Application Pattern

Atlas is applied to `settings.gradle.kts` and configured there:

```kotlin
plugins { id("dev.jonpoulton.atlas") }

atlas {
  projectTypes { useDefaults() }
  graphviz { }
}
```

It lives in settings because every project needs the same config, and under isolated projects a
subproject may not read the root project's extension.

`AtlasPlugin.apply(Settings)` does three things:
1. Creates the `AtlasExtension` on the settings object
2. On `settingsEvaluated`, snapshots the config into `AtlasConfig` and enumerates every project path
3. Registers `gradle.lifecycle.beforeProject`, which calls `wireProject` for each project

### The Config Snapshot (important constraint)

`beforeProject` actions are **isolated**, i.e. serialized, before they run. That imposes hard limits
on what the per-project wiring may capture:

- **`NamedDomainObjectContainer` cannot be isolated** (it fails with a `ConcurrentModificationException`
  while serializing its pending-actions map). So `projectTypes` and `linkTypes` are flattened into
  plain value types in `AtlasConfig`/`ProjectTypeMatcher`.
- **Managed `Property` instances isolate fine**, including ones whose convention comes from
  `providers.gradleProperty(...)`. That's why `D2SpecImpl`/`GraphvizSpecImpl`/`MermaidSpecImpl` are
  captured live on `AtlasWiring` rather than snapshotted.
- **Script references cannot be captured at all**, which is why the callback lives in a plugin class.

Because the specs are captured live, they must never hold a `Project`. They take `ObjectFactory` and
`ProviderFactory` instead, which is also why `IGradleProperties` exposes `providers` rather than a
project.

### Cross-Project Data Flow

Isolated projects forbids reading another project's tasks or extensions, so **every file Atlas passes
between projects travels as a dependency-resolution artifact**. See `internal/Aggregation.kt`.

Each kind of file is an `AtlasArtifact` with its own value for the `dev.jonpoulton.atlas.artifact`
attribute, so a single project dependency can carry any number of them:

- Each subproject publishes `ProjectType` and `ProjectLinks`
- The root resolves those from every subproject, collates them, and publishes `CollatedTypes`,
  `CollatedLinks`, `D2Classes` and per-framework `legend(framework)` back out
- Each subproject resolves what it needs from `:`

The two directions use different configurations distinguished by attribute, so there is no cycle.
Only the root-collating-from-subprojects direction resolves leniently, because a subproject may
legitimately publish nothing (e.g. a group directory with no build file, which Atlas leaves out of
the graph entirely). `AtlasContext.fromRoot` resolves strictly: the root always publishes, so a
failure there is a real one and needs to be reported as itself. Under leniency it instead turned
into an empty file collection, and only surfaced much later as `Collection is empty` while the
configuration cache serialized whichever task property the file ended up in.

Which is why the root project is wired even when it has **no build file of its own** - it isn't a
node in the chart either way, but every subproject resolves the collated files and the shared legends
from it.

> **Every configuration Atlas creates must keep the `ATLAS_CONFIGURATION_PREFIX` (`"atlas"`) prefix.**
> `createProjectLinks` scans `project.configurations` for `ProjectDependency` entries to build the
> graph, and skips anything with that prefix. Drop the prefix and Atlas's own plumbing draws itself
> into every user's diagrams as a phantom edge to the root project.

### Task Execution Flow

1. Each subproject runs `WriteProjectType` → outputs project type classification to JSON
2. Each subproject runs `WriteProjectLinks` → outputs direct dependencies to JSON
3. Root runs `CollateProjectTypes` / `CollateProjectLinks` → aggregates via resolved artifacts
4. Each subproject runs `WriteProjectTree` → consumes the collated links to compute its own tree
5. Framework-specific tasks (D2/Graphviz/Mermaid) generate diagram files
6. Each subproject runs `WriteReadme` → injects every configured framework's diagram into its README

Note the root project is not itself a node in the chart: it only collates and draws legends.

### Testing Approach

Tests use a scenario-based pattern:
- Each scenario (e.g. `DiamondGraph`, `TriangleGraph`) defines a complete multi-module project structure
- `ScenarioTest.runScenario()` creates a temporary Gradle project with that structure
- Tests invoke Gradle tasks via TestKit and verify generated outputs
- Scenarios are reused across multiple test classes for different plugin variants

Example: `DiamondGraph` creates a 4-module project (top → mid-a/mid-b → bottom) to test transitive dependency tracking.

Scenario rules that follow from Atlas being a settings plugin:
- The `atlas { }` block goes in `Scenario.atlasConfig`, which `ScenarioTest` writes into
  `settings.gradle.kts`. It does **not** belong in `rootBuildFile`.
- Subproject build files must **not** apply `id("dev.jonpoulton.atlas")` - the settings plugin wires
  every project itself.
- Build scripts must **not** declare plugin versions (e.g. `kotlin("jvm") version "..."`). Applying
  the plugin from settings puts the TestKit-injected classpath on the settings classloader, so a
  version request fails with "already on the classpath with an unknown version".
- `ScenarioTest` emits blanket imports into the settings file, because the `atlas` extension
  accessor shadows the `atlas` package and blocks fully qualified references there. `atlas.core.*`
  and `atlas.core.internal.*` always, plus `atlas.<framework>.*` and `atlas.<framework>.tasks.*` for
  each framework the scenario declares. Framework packages are conditional on purpose: D2 and
  Graphviz both export `FileFormat`, `LayoutEngine`, `Shape` and `ArrowType`, so importing both
  unconditionally would make those names ambiguous.
- `pluginManagement { }` is written before `plugins { }` and `dependencyResolutionManagement { }`
  after it. Groovy settings scripts reject anything but `pluginManagement`/`buildscript` ahead of
  `plugins`, which is why Blueprint's combined `DEFAULT_REPOSITORIES_KTS` isn't used here.
- Every scenario runs with `org.gradle.unsafe.isolated-projects=true`.

## Key Concepts

### Project Types
Projects are classified by `ProjectTypeSpec` using matchers:
- `pathContains`: substring match on project path
- `pathMatches`: regex match on project path
- `hasPluginId`: detect applied Gradle plugins

The first matcher that is *set* wins, even if it doesn't match. Matching runs in the project's own
`afterEvaluate` (see `WriteProjectType`), which is what makes `hasPluginId` work without violating
isolated projects.

Quick-access helpers (`androidApp()`, `kotlinJvm()`, `useDefaults()`, ...) live in `org.gradle.kotlin.dsl.ProjectTypeDsl`.

### Link Types
Project dependencies are classified by `LinkTypeSpec`:
- Based on Gradle configuration name (e.g. "api", "implementation")
- `style` uses the shared `atlas.core.LinkStyle`; frameworks which can't draw a given style fall back to the closest one and warn at configuration time

### Path Transforms
The `PathTransformSpec` allows regex-based transformations of project paths in generated diagrams (e.g., removing common prefixes).

## File Locations

- Generated diagrams: `atlas/<framework>/` in each project, e.g. `atlas/d2/chart.svg`. Legends only in the root project, resolved from `AtlasConfig.rootDir`
- Intermediate files (D2 `.d2`, Graphviz `.dot`): `build/atlas/<framework>/`, unless `intermediateFilesInBuildDir` is disabled in that framework's block
- Per-project data: `build/atlas/*.json` in each subproject
- Test fixtures: `src/test/kotlin/atlas/test/scenarios/`
- Documentation: `docs/` (MkDocs-based, deployed to GitHub Pages)

## Important Properties

The Java version is read from the root `.java-version` file (via Blueprint's `javaVersion()`/`jvmTarget()`), and is also what CI's `setup-java` steps and the `gradle:*-jdk*` docker images pin.

From `gradle.properties`:
- `atlas.minimumGradleVersion`: Minimum Gradle version. The APIs used are available from 8.8
  (`gradle.lifecycle.beforeProject`) and 8.4 (the configuration role factories), but isolated
  projects is only really usable on 9.x
- `org.gradle.configuration-cache`: Configuration cache is enabled
- `org.gradle.parallel`: Parallel execution is enabled
- `org.gradle.unsafe.isolated-projects`: Isolated projects is enabled
