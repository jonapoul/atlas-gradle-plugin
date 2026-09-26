@file:Suppress("UnstableApiUsage")

package atlas.core.tasks

import atlas.core.AtlasSpec
import atlas.core.internal.AtlasConfig
import atlas.core.internal.AtlasWarnings.Companion.PROBLEM_DOESNT_EXIST
import atlas.core.internal.AtlasWarnings.Companion.PROBLEM_NEEDS_REGENERATION
import atlas.core.internal.Variant
import atlas.core.internal.diff
import java.io.File
import java.io.FileNotFoundException
import javax.inject.Inject
import org.gradle.api.DefaultTask
import org.gradle.api.GradleException
import org.gradle.api.Project
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.problems.Problems
import org.gradle.api.provider.Property
import org.gradle.api.tasks.CacheableTask
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputFile
import org.gradle.api.tasks.PathSensitive
import org.gradle.api.tasks.TaskAction
import org.gradle.api.tasks.TaskProvider
import org.gradle.internal.extensions.stdlib.capitalized
import org.gradle.language.base.plugins.LifecycleBasePlugin.CHECK_TASK_NAME
import org.gradle.language.base.plugins.LifecycleBasePlugin.VERIFICATION_GROUP

/**
 * Registered on each chart file generation task to confirm that its configuration hasn't changed
 * since the last `gradle atlasGenerate` task run. Will throw an exception if a difference is found.
 *
 * This will auto-attach to `gradle check` if [atlas.core.AtlasExtension.checkOutputs] is enabled,
 * which it is by default.
 */
@CacheableTask
public abstract class CheckFileDiff : DefaultTask() {
  @get:[PathSensitive(NONE) InputFile]
  public abstract val actualFile: RegularFileProperty
  @get:Input public abstract val expectedDirectory: Property<String>
  @get:Input public abstract val expectedFilename: Property<String>
  @get:Input public abstract val taskPath: Property<String>
  @get:Inject public abstract val problems: Problems

  init {
    group = VERIFICATION_GROUP
    description = "Checks whether two files are equivalent"
  }

  @TaskAction
  public fun execute() {
    val expectedFile = File(expectedDirectory.get(), expectedFilename.get())
    val actualFile = actualFile.get().asFile

    if (!expectedFile.exists()) {
      val e = FileNotFoundException(expectedFile.absolutePath)
      problems.reporter.throwing(e, PROBLEM_DOESNT_EXIST) { spec ->
        with(spec) {
          details("Tried to run comparison on the file at $expectedFile, but it doesn't exist yet.")
          fileLocation(expectedFile.absolutePath)
          solution("Run `gradle ${taskPath.get()}` to generate the file.")
          withException(e)
        }
      }
    }

    val expectedContents = expectedFile.readText()
    val actualContents = actualFile.readText()

    if (expectedContents != actualContents) {
      val exception = GradleException("Expected chart file differs from the actual!")
      problems.reporter.throwing(exception, PROBLEM_NEEDS_REGENERATION) { spec ->
        with(spec) {
          details(diff(expectedContents, actualContents))
          fileLocation(expectedFile.absolutePath)
          solution("Run `gradle ${taskPath.get()}` to apply the fixes.")
          withException(exception)
        }
      }
    }
  }

  internal companion object {
    internal inline fun <reified T1 : TaskWithOutputFile, T2 : TaskWithOutputFile> register(
      target: Project,
      config: AtlasConfig,
      spec: AtlasSpec,
      variant: Variant,
      realTask: TaskProvider<T1>,
      dummyTask: TaskProvider<T2>,
    ): TaskProvider<CheckFileDiff> =
      with(target) {
        val name = "check${spec.name.capitalized()}$variant"
        val checkDiff =
          tasks.register(name, CheckFileDiff::class.java) { task ->
            task.taskPath.convention(target.path + ":" + realTask.name)
            task.actualFile.convention(dummyTask.flatMap { it.outputFile })
          }

        checkDiff.configure { task ->
          // explicitly splitting like this to force-break the task dependency between write and
          // check
          val expectedFile = realTask.map { it.outputFile }.get().get().asFile
          task.expectedDirectory.set(expectedFile.parentFile.absolutePath)
          task.expectedFilename.set(expectedFile.name)
        }

        tasks.named(CHECK_TASK_NAME).configure { check ->
          if (config.checkOutputs) {
            check.dependsOn(checkDiff)
          }
        }

        checkDiff
      }
  }
}
