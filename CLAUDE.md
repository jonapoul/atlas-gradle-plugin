# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

Atlas is a Gradle plugin for generating diagrams of modular project dependencies. It supports three rendering frameworks: D2, Graphviz, and Mermaid. The plugin is built with Kotlin and targets Gradle 9+ with full configuration cache support.

**Key Characteristics:**
- Multi-module Gradle project with convention plugins in `build-logic/`
- Java 17 minimum (see `gradle.properties`)
- Uses Gradle configuration cache and parallel execution
- Published to Maven Central under `dev.jonpoulton.atlas`

## Commands

### Building and Testing
```bash
./gradlew build                    # Build all modules
./gradlew test                     # Run all tests
./gradlew :atlas-test:test         # Run tests for specific module
./gradlew check                    # Run all checks including detekt and licensee
```

### Single Test Execution
Tests use JUnit 6. To run a single test class:
```bash
./gradlew :atlas-test:test --tests "atlas.core.WriteModuleTreeTest"
```

To run a specific test method:
```bash
./gradlew :atlas-test:test --tests "atlas.core.WriteModuleTreeTest.Single links for diamond"
```

### Code Quality
```bash
./gradlew detektCheck              # Run static analysis
./gradlew licensee                 # Validate dependency licenses
./scripts/ktlintCheck.sh           # Run ktlint checks
```

### Documentation
This task will take a while, so don't run unless really necessary:
```bash
./gradlew dokkaGeneratePublicationHtml --rerun-tasks --no-build-cache --no-configuration-cache       # Generate API docs to docs/api/
```

### Plugin Development
The plugin can be tested locally using the test scenarios in `atlas-test`. Tests use Gradle TestKit to verify plugin behavior in realistic project structures.

## Architecture

### Module Structure

1. **atlas-core**: Base plugin implementation
   - `AtlasPlugin`: Abstract base plugin that auto-applies to root and subprojects
   - `AtlasExtension`: Main DSL for configuration
   - Core tasks: `CollateModuleTypes`, `CollateModuleLinks`, `WriteModuleType`, `WriteModuleLinks`, `WriteModuleTree`
   - Internal utilities for module graph traversal and serialization

2. **atlas-d2**: D2 diagram generation plugin
   - Extends `AtlasPlugin` with D2-specific functionality
   - `D2AtlasExtension`: D2-specific configuration DSL
   - Tasks: `WriteD2Chart`, `WriteD2Classes`, `ExecD2`

3. **atlas-graphviz**: Graphviz diagram generation plugin
   - Extends `AtlasPlugin` with Graphviz/DOT-specific functionality
   - `GraphvizAtlasExtension`: Graphviz-specific configuration DSL
   - Tasks: `WriteGraphvizChart`, `WriteGraphvizLegend`, `ExecGraphviz`

4. **atlas-mermaid**: Mermaid diagram generation plugin
   - Extends `AtlasPlugin` with Mermaid-specific functionality
   - `MermaidAtlasExtension`: Mermaid-specific configuration DSL
   - Tasks: `WriteMermaidChart`, `WriteMarkdownLegend`

5. **atlas-test**: Test infrastructure and scenarios
   - `ScenarioTest`: Base class for integration tests
   - Test scenarios in `atlas.test.scenarios.*` representing common project structures
   - Utilities for Gradle TestKit runners and assertions

6. **build-logic**: Convention plugins for the build itself
   - Located in `build-logic/src/main/kotlin/atlas/gradle/`
   - Convention plugins: `ConventionKotlin`, `ConventionPublish`, `ConventionDetekt`, etc.
   - Applied via `atlas.convention.*` plugin IDs

### Plugin Application Pattern

**Critical**: Only ONE Atlas plugin can be applied per project (d2, graphviz, or mermaid). The plugin is applied to the root project and automatically propagates to all subprojects.

The base `AtlasPlugin` uses a two-phase application:
1. **Root project**: Registers `CollateModuleTypes` and `CollateModuleLinks` tasks that aggregate data from all subprojects
2. **Subprojects**: Auto-applied via `subprojects {}`, registers `WriteModuleType`, `WriteModuleLinks`, `WriteModuleTree` tasks

Each subproject writes its local module information to `build/atlas/*.json`, then root tasks collate these into project-wide diagrams.

### Task Execution Flow

1. Each subproject runs `WriteModuleType` → outputs module type classification to JSON
2. Each subproject runs `WriteModuleLinks` → outputs direct dependencies to JSON
3. Root runs `CollateModuleTypes` → aggregates all module types
4. Root runs `CollateModuleLinks` → aggregates all module links
5. Each subproject runs `WriteModuleTree` → consumes collated links to compute full dependency tree
6. Framework-specific tasks (D2/Graphviz/Mermaid) generate diagram files from the aggregated data

### Gradle Isolated Projects Support

The plugin will one day be updated to support Gradle's isolated projects feature (see issue #307). This requires careful handling of project references and task dependencies to avoid cross-project configuration. For now though, this isn't a top priority.

### Testing Approach

Tests use a scenario-based pattern:
- Each scenario (e.g., `DiamondGraph`, `TriangleGraph`) defines a complete multi-module project structure
- `ScenarioTest.runScenario()` creates a temporary Gradle project with that structure
- Tests invoke Gradle tasks via TestKit and verify generated outputs
- Scenarios are reused across multiple test classes for different plugin variants

Example: `DiamondGraph` creates a 4-module project (top → mid-a/mid-b → bottom) to test transitive dependency tracking.

## Key Concepts

### Module Types
Modules are classified by `ModuleTypeSpec` using matchers:
- `pathContains`: substring match on module path
- `pathMatches`: regex match on module path
- `hasPluginId`: detect applied Gradle plugins

Built-in types include `KOTLIN_JVM`, `KOTLIN_ANDROID`, `JAVA_LIBRARY`, etc.

### Link Types
Module dependencies are classified by `LinkTypeSpec`:
- Based on Gradle configuration name (e.g., "api", "implementation")
- Can customize visual styling per link type in each framework

### Path Transforms
The `PathTransformSpec` allows regex-based transformations of module paths in generated diagrams (e.g., removing common prefixes).

## File Locations

- Generated diagrams: `build/atlas/` in root project
- Per-module data: `build/atlas/*.json` in each subproject
- Test fixtures: `atlas-test/src/test/kotlin/atlas/test/scenarios/`
- Documentation: `docs/` (MkDocs-based, deployed to GitHub Pages)

## Important Properties

From `gradle.properties`:
- `atlas.javaVersion`: Minimum Java version
- `atlas.minimumGradleVersion`: Minimum Gradle version
- `org.gradle.configuration-cache`: Configuration cache is enabled
- `org.gradle.parallel`: Parallel execution is enabled
