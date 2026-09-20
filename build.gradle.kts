@file:Suppress("AvoidDuplicateDependencies")

import app.cash.licensee.UnusedAction.IGNORE
import blueprint.core.getOptional
import blueprint.core.javaVersion
import blueprint.core.jvmTarget
import blueprint.core.localProperties
import dev.detekt.gradle.Detekt
import org.gradle.api.attributes.plugin.GradlePluginApiVersion.GRADLE_PLUGIN_API_VERSION_ATTRIBUTE
import org.gradle.api.tasks.testing.logging.TestExceptionFormat.FULL
import org.gradle.api.tasks.testing.logging.TestLogEvent.FAILED
import org.gradle.api.tasks.testing.logging.TestLogEvent.PASSED
import org.gradle.api.tasks.testing.logging.TestLogEvent.SKIPPED
import org.gradle.kotlin.dsl.withType
import org.gradle.language.base.plugins.LifecycleBasePlugin.VERIFICATION_GROUP
import org.jetbrains.dokka.gradle.engine.parameters.VisibilityModifier.Public
import org.jetbrains.kotlin.gradle.dsl.abi.ExperimentalAbiValidation
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
  alias(libs.plugins.blueprint)
  alias(libs.plugins.blueprintTest)
  alias(libs.plugins.buildConfig)
  alias(libs.plugins.detekt)
  alias(libs.plugins.dokka)
  alias(libs.plugins.kotlinJvm)
  alias(libs.plugins.kotlinSerialization)
  alias(libs.plugins.licensee)
  alias(libs.plugins.publish)
  `java-gradle-plugin`
}

dependencies {
  detektPlugins(libs.blueprint.detektRules)

  compileOnly(gradleApi())
  compileOnly(kotlin("stdlib"))
  compileOnly(libs.kotlin.gradle)

  implementation(libs.blueprint)
  implementation(libs.kotlinx.serialization)

  testPluginClasspath(libs.agp)
  testPluginClasspath(libs.kotlin.gradle)

  testImplementation(kotlin("stdlib"))
  testImplementation(kotlin("test"))
  testImplementation(libs.assertk)
  testImplementation(libs.blueprint.testAssertk)
  testImplementation(libs.junit.api)
  testImplementation(libs.junit.params)
  testRuntimeOnly(libs.junit.launcher)
}

gradlePlugin {
  vcsUrl = "https://github.com/jonapoul/atlas-gradle-plugin.git"
  website = "https://github.com/jonapoul/atlas-gradle-plugin"

  plugins.create("atlas") {
    id = "dev.jonpoulton.atlas"
    implementationClass = "atlas.core.AtlasPlugin"
    description = providers.gradleProperty("POM_DESCRIPTION").get()
    displayName = "Atlas"
    tags.addAll(
      "charts",
      "d2",
      "dagre",
      "diagrams",
      "dot",
      "elk",
      "gradle",
      "graphviz",
      "kotlin",
      "links",
      "markdown",
      "mermaid",
      "modules",
      "png",
      "projects",
      "svg",
    )
  }
}

// Set the minimum supported gradle version
val minGradleVersion = providers.gradleProperty("atlas.minimumGradleVersion")

configurations.named("apiElements").configure {
  attributes {
    attribute(
      GRADLE_PLUGIN_API_VERSION_ATTRIBUTE,
      objects.named<GradlePluginApiVersion>(minGradleVersion.get()),
    )
  }
}

tasks.validatePlugins {
  enableStricterValidation = true
  failOnWarning = true
}

kotlin {
  // https://kotlinlang.org/docs/gradle-binary-compatibility-validation.html
  @OptIn(ExperimentalAbiValidation::class) abiValidation()

  compilerOptions {
    allWarningsAsErrors = true
    jvmTarget = jvmTarget()
    explicitApi()

    freeCompilerArgs.addAll(
      "-Xcontext-sensitive-resolution", // https://kotlinlang.org/docs/whatsnew22.html#preview-of-context-sensitive-resolution
      "-Xintrinsic-const-evaluation", // https://kotlinlang.org/docs/whatsnew24.html#improved-compile-time-constants
      "-opt-in=kotlin.RequiresOptIn",
      "-opt-in=kotlinx.serialization.ExperimentalSerializationApi",
    )
  }
}

tasks.register("compileAll") {
  dependsOn(tasks.withType(KotlinCompile::class))
}

java {
  val version = javaVersion().get()
  sourceCompatibility = version
  targetCompatibility = version
}

dokka {
  dokkaPublications.html {
    outputDirectory = project.layout.projectDirectory.dir("docs/api")
    includes.from(project.layout.projectDirectory.file("README.md"))
  }

  dokkaPublications.configureEach {
    failOnWarning = true
    suppressInheritedMembers = true
    suppressObviousFunctions = true
  }

  dokkaSourceSets.configureEach {
    documentedVisibilities.add(Public)
    reportUndocumented = false
    skipDeprecated = true
    suppressGeneratedFiles = true

    perPackageOption {
      matchingRegex = ".*\\.internal.*"
      suppress = true
    }

    sourceLink {
      localDirectory = layout.projectDirectory
      remoteLineSuffix = "#L"
      val path = project.path.replace(":", "")
      remoteUrl("https://github.com/jonapoul/atlas-gradle-plugin/tree/main/$path")
    }
  }
}

detekt {
  config.from(rootProject.file("config/detekt.yml"))
  buildUponDefaultConfig = true
}

val detektTasks = tasks.withType<Detekt>()
val detektCheck =
  tasks.register("detektCheck") {
    group = VERIFICATION_GROUP
    description = "Aggregates all Detekt tasks"
    dependsOn(detektTasks)
  }

tasks.check.configure { dependsOn(detektCheck) }

detektTasks.configureEach {
  reports { html.required = true }
  exclude { it.file.path.contains("generated") }
}

licensee {
  unusedAction(IGNORE)
  listOf("Apache-2.0", "MIT").forEach(::allow)
  allowUrl("https://www.eclipse.org/legal/epl-v20.html")
}

buildConfig {
  generateAtSync = true
  sourceSets.getByName("test") {
    packageName = "atlas.test"
    useKotlinOutput { topLevelConstants = true }
    buildConfigField("AGP_VERSION", libs.versions.agp)
    buildConfigField("KOTLIN_VERSION", libs.versions.kotlin)
    buildConfigField("GRADLE_VERSION", GradleVersion.current().version)
    buildConfigField<File?>("ANDROID_HOME", androidHome())
  }
}

fun androidHome(): File? {
  val fromEnv =
    providers
      .environmentVariable("ANDROID_HOME")
      .orElse(providers.environmentVariable("ANDROID_SDK_ROOT"))
      .orNull
      ?.let(::File)
  if (fromEnv?.exists() == true) {
    logger.info("Using system environment variable $fromEnv as ANDROID_HOME")
    return fromEnv
  }

  val sdkHome = localProperties().getOptional("sdk.dir")?.let(::File)
  if (sdkHome?.exists() == true) {
    logger.info("Using local.properties sdk.dir $sdkHome as ANDROID_HOME")
    return sdkHome
  }

  logger.warn("No Android SDK found - Android unit tests will be skipped")
  return null
}

tasks.withType<Test>().configureEach {
  useJUnitPlatform()

  testLogging {
    events = setOf(PASSED, SKIPPED, FAILED)
    exceptionFormat = FULL
    showCauses = true
    showExceptions = true
    showStackTraces = true
    showStandardStreams = true
    displayGranularity = 2
  }
}
