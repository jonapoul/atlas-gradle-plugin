package atlas.gradle

import org.gradle.api.JavaVersion
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.plugins.JavaPluginExtension
import org.gradle.kotlin.dsl.apply
import org.gradle.kotlin.dsl.configure
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.dsl.KotlinJvmProjectExtension
import org.jetbrains.kotlin.gradle.plugin.KotlinPluginWrapper

class ConventionKotlin : Plugin<Project> {
  override fun apply(target: Project) = with(target) {
    pluginsInternal {
      apply(KotlinPluginWrapper::class)
      apply(ConventionDetekt::class)
      apply(ConventionIdea::class)
      apply(ConventionLicensee::class)
    }

    val javaVersion = providers.gradleProperty("atlas.javaVersion")

    extensions.configure<KotlinJvmProjectExtension> {
      compilerOptions {
        allWarningsAsErrors.set(true)
        jvmTarget.set(javaVersion.map(JvmTarget::fromTarget))
        explicitApi()

        freeCompilerArgs.addAll(
          "-opt-in=kotlin.RequiresOptIn",
          "-opt-in=kotlinx.serialization.ExperimentalSerializationApi",
          "-opt-in=atlas.core.InternalAtlasApi",
        )
      }
    }

    extensions.configure<JavaPluginExtension> {
      val version = javaVersion.map(JavaVersion::toVersion).get()
      sourceCompatibility = version
      targetCompatibility = version
    }
  }
}
