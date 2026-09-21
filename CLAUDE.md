# CLAUDE.md

## Project Overview

Atlas is a Gradle **settings** plugin for generating diagrams of modular project dependencies,
rendered with D2, Graphviz and/or Mermaid. Single-project build, published as
`dev.jonpoulton.atlas:plugin`, Java 21 minimum (root `.java-version`). Uses
[Blueprint](https://github.com/jonapoul/blueprint) for Gradle DSL shortcuts, and runs with
configuration cache, parallel execution and isolated projects all enabled.

## Commands

```bash
./gradlew build                    # Build everything
./gradlew test                     # Run all tests (JUnit 6)
./gradlew test --tests "atlas.core.WriteProjectTreeTest.Single links for diamond"
./gradlew check                    # detekt, licensee and the ABI dump
./gradlew detektCheck
./gradlew updateKotlinAbi          # refresh the ABI dump after a public API change, else `check` fails

./scripts/ktfmt.sh                 # format files changed since main (ktfmt, Google style)
./scripts/ktfmt.sh check           # check-only
./scripts/ktfmt.sh --force         # all files

# Slow, only when really necessary:
./gradlew dokkaGeneratePublicationHtml --rerun-tasks --no-build-cache  # API docs to docs/api/
```

## Architecture

### Package Structure

`atlas.core` for everything framework-agnostic, then `atlas.d2`, `atlas.graphviz`, `atlas.mermaid`.
Each framework package has the same layout:

| Location | Holds |
|---|---|
| `atlas.<pkg>` | user-facing API - the `*Spec` config interfaces and shared value types |
| `atlas.<pkg>.internal` | implementation, including the `*SpecImpl`s and each framework's `*Tasks` registrar |
| `atlas.<pkg>.tasks` | task types |

Adding a framework means a `<Framework>Spec`, a `<Framework>Tasks` implementing `FrameworkTasks`,
and a `Framework` enum entry. Anything not user-facing API is Kotlin `internal` - no opt-in
annotation. Tests live in `src/test` so they can see `internal` declarations.

`atlas.core` also holds `AtlasPlugin` (a `Plugin<Settings>`, the single entry point) and
`AtlasExtension`; `atlas.core.internal` holds the wiring layer.

### Shared Project and Link Types

`projectTypes` and `linkTypes` are declared once and rendered by every configured framework. Each
spec carries the union of all three frameworks' style properties: shared meanings get one property
mapping to several framework keys (`fontColor` -> D2 `style.font-color`, Graphviz `fontcolor`,
Mermaid `color`), and only genuine clashes are prefixed (`d2Shape`, `graphvizShape`).
`StyleProperties` holds one attribute map per framework and records which DSL properties were set,
so `Warnings.kt` can warn about properties no configured framework will read.

### The Config Snapshot (important constraint)

`AtlasPlugin.apply(Settings)` creates the extension, snapshots config into `AtlasConfig` on
`settingsEvaluated`, then registers `gradle.lifecycle.beforeProject` to call `wireProject`.

`beforeProject` actions are **isolated**, i.e. serialized, before they run. That limits what the
per-project wiring may capture:

- **`NamedDomainObjectContainer` cannot be isolated** (`ConcurrentModificationException` while
  serializing its pending-actions map). So `projectTypes` and `linkTypes` are flattened into plain
  value types in `AtlasConfig`/`ProjectTypeMatcher`.
- **Managed `Property` instances isolate fine**, including ones with a `providers.gradleProperty(...)`
  convention. That's why the `*SpecImpl`s are captured live on `AtlasWiring` rather than snapshotted.
- **Script references cannot be captured at all**, which is why the callback lives in a plugin class.

Because the specs are captured live, they must never hold a `Project`. They take `ObjectFactory` and
`ProviderFactory` instead, which is also why `IGradleProperties` exposes `providers`.

### Cross-Project Data Flow

Isolated projects forbids reading another project's tasks or extensions, so **every file Atlas passes
between projects travels as a dependency-resolution artifact**. See `internal/Aggregation.kt`. Each
kind of file is an `AtlasArtifact` with its own value for the `dev.jonpoulton.atlas.artifact`
attribute, so one project dependency can carry any number of them:

- Each subproject publishes `ProjectType` and `ProjectLinks`
- The root resolves those from every subproject, collates them, and publishes `CollatedTypes`,
  `CollatedLinks`, `D2Classes` and per-framework `legend(framework)` back out
- Each subproject resolves what it needs from `:`

The two directions use different configurations distinguished by attribute, so there's no cycle.
Only root-collating-from-subprojects resolves leniently, because a subproject may legitimately
publish nothing (e.g. a group directory with no build file). `AtlasContext.fromRoot` resolves
strictly: the root always publishes, so a failure there is real and must be reported as itself -
under leniency it became an empty file collection and only surfaced later as `Collection is empty`
during configuration cache serialization.

Which is why the root project is wired even when it has **no build file of its own**. It isn't a
node in the chart either way, but every subproject resolves the collated files and legends from it.

> **Every configuration Atlas creates must keep the `ATLAS_CONFIGURATION_PREFIX` (`"atlas"`) prefix.**
> `createProjectLinks` scans `project.configurations` for `ProjectDependency` entries to build the
> graph, and skips anything with that prefix. Drop the prefix and Atlas's own plumbing draws itself
> into every user's diagrams as a phantom edge to the root project.

### D2 Executable

`ExecD2` runs, in order: an explicit `executable`, the `d2` on the system PATH, or a downloaded
one, as picked by `ExecutableSource`. Whether anything downloads is decided once from settings
(`d2DownloadVersion` in `d2/internal/D2Download.kt`) and kept in `AtlasConfig.d2DownloadVersion`.
When that's null, Atlas adds no repository or configuration at all.

A download is plain dependency resolution: an exclusive Ivy repository `atlasD2Releases` pointing at
D2's GitHub releases, the `atlasD2Executable` configuration, and the `UnpackD2` transform. The
repository goes in `dependencyResolutionManagement`, and under `PREFER_PROJECT` also into every
project declaring repositories of its own, since those ignore the settings ones.

The default version lives only in `config/d2.version`. It feeds `DEFAULT_D2_VERSION` (via the
buildconfig plugin), `docker/Dockerfile` and Renovate.

### Task Execution Flow

`WriteProjectType` + `WriteProjectLinks` per subproject → `CollateProjectTypes` /
`CollateProjectLinks` at the root → `WriteProjectTree` per subproject → framework diagram tasks →
`WriteReadme`, which injects every configured framework's diagram into the project README.

### Testing Approach

Scenarios (e.g. `DiamondGraph`, `TriangleGraph`) in `src/test/kotlin/atlas/test/scenarios/` define
complete multi-module structures. Inside a `ScenarioTest`, `DiamondGraph { ... }` builds one in a
temp dir and runs tasks via TestKit. Scenarios are reused across test classes for different plugin variants.

Rules that follow from Atlas being a settings plugin:
- The `atlas { }` block goes in `Scenario.atlasConfig`, which `ScenarioTest` writes into
  `settings.gradle.kts`. It does **not** belong in `rootBuildFile`.
- Subproject build files must **not** apply `id("dev.jonpoulton.atlas")`.
- Build scripts must **not** declare plugin versions (e.g. `kotlin("jvm") version "..."`). Applying
  the plugin from settings puts the TestKit classpath on the settings classloader, so a version
  request fails with "already on the classpath with an unknown version".
- `ScenarioTest` emits blanket imports into the settings file, because the `atlas` extension
  accessor shadows the `atlas` package. `atlas.core.*` and `atlas.core.internal.*` always, plus
  `atlas.<framework>.*` and `atlas.<framework>.tasks.*` per declared framework. Framework imports
  are conditional on purpose: D2 and Graphviz both export `FileFormat`, `LayoutEngine`, `Shape` and
  `ArrowType`.
- `pluginManagement { }` is written before `plugins { }`, `dependencyResolutionManagement { }` after
  it. Groovy settings scripts reject anything but `pluginManagement`/`buildscript` ahead of
  `plugins`, so Blueprint's combined `DEFAULT_REPOSITORIES_KTS` isn't used here.
- Every scenario runs with `org.gradle.unsafe.isolated-projects=true`.

## Key Concepts

**Project types**: `ProjectTypeSpec` matchers are `pathContains`, `pathMatches` and `hasPluginId`.
The first matcher that is *set* wins, even if it doesn't match. Matching runs in the project's own
`afterEvaluate` (see `WriteProjectType`), which is what makes `hasPluginId` work without violating
isolated projects. Helpers (`androidApp()`, `kotlinJvm()`, `useDefaults()`, ...) live in
`org.gradle.kotlin.dsl.ProjectTypeDsl`.

**Link types**: `LinkTypeSpec`, keyed on configuration name. `style` uses the shared
`atlas.core.LinkStyle`; frameworks that can't draw a given style fall back to the closest one and
warn at configuration time.

**Path transforms**: `PathTransformSpec` applies regex transforms to project paths in diagrams.

## File Locations

- Generated charts: the project directory itself, next to its README, e.g. `chart-d2.svg`. No
  `atlas/` directory per project
- Generated legends: `atlas/` in the root project only, e.g. `atlas/legend-graphviz.svg`, resolved
  from `AtlasConfig.rootDir`. D2's shared `classes-d2.d2` goes there too
- The framework is a filename suffix rather than a directory, which is what stops two frameworks
  overwriting each other. See `frameworkFilename` in `internal/Extensions.kt`
- Intermediate files (D2 `.d2`, Graphviz `.dot`) and per-project JSON: `build/atlas/`, unless
  `intermediateFilesInBuildDir` is disabled in that framework's block
- Documentation: `docs/` (MkDocs, deployed to GitHub Pages)

## Important Properties

The Java version comes from the root `.java-version` (via Blueprint's `javaVersion()`/`jvmTarget()`),
and is what CI's `setup-java` and the `gradle:*-jdk*` docker images pin.

`atlas.minimumGradleVersion` in `gradle.properties`: the APIs used are available from 8.8
(`gradle.lifecycle.beforeProject`) and 8.4 (configuration role factories), but isolated projects is
only really usable on 9.x.
