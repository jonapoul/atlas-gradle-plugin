package atlas.d2.internal

import atlas.core.Framework
import atlas.core.internal.AtlasContext
import atlas.core.internal.ChartFiles
import atlas.core.internal.FrameworkTasks
import atlas.core.internal.atlasBuildDirectory
import atlas.core.internal.intermediateFile
import atlas.core.internal.outputFile
import atlas.core.internal.publishAtlasArtifact
import atlas.core.internal.singleFile
import atlas.core.tasks.CheckFileDiff
import atlas.d2.FileFormat
import atlas.d2.tasks.ExecD2
import atlas.d2.tasks.WriteD2Chart
import atlas.d2.tasks.WriteD2Classes
import java.io.File
import org.gradle.api.Project
import org.gradle.api.file.FileCollection
import org.gradle.api.provider.Provider

internal object D2Tasks : FrameworkTasks {
  override val framework: Framework = D2

  override fun registerRootTasks(context: AtlasContext): Unit =
    with(context.project) {
      val d2 = context.d2

      warnIfLabelLocationSpecifiedButNotPosition(context)
      warnIfAnimationSelectedWithNonAnimatedFileFormat(context)

      val classes =
        WriteD2Classes.real(
          context = context,
          outputFile =
            intermediateFile(
              config = context.config,
              framework = framework,
              variant = Legend,
              fileExtension = "d2",
              inBuildDir = d2.intermediateFilesInBuildDir.get(),
              filename = "classes",
            ),
        )

      // Every project's chart references this one file, and under isolated projects a subproject
      // can't reach the task that writes it, so publish it as an artifact instead.
      publishAtlasArtifact(
        artifact = D2Classes,
        file = classes.flatMap { it.outputFile },
        builtBy = classes,
      )

      // Nothing to check when the classes file lives in the build directory
      if (!d2.intermediateFilesInBuildDir.get()) {
        val dummyClasses =
          WriteD2Classes.dummy(
            context = context,
            outputFile = atlasBuildDirectory.get().file("classes-temp.d2").asFile,
          )

        CheckFileDiff.register(
          target = this,
          config = context.config,
          spec = d2,
          variant = Chart,
          realTask = classes,
          dummyTask = dummyClasses,
        )
      }
    }

  override fun registerChildTasks(context: AtlasContext): ChartFiles =
    with(context.project) {
      val d2Spec = context.d2

      val classesFile = context.fromRoot(D2Classes)
      val d2File =
        intermediateFile(
          config = context.config,
          framework = framework,
          variant = Chart,
          fileExtension = d2Spec.fileExtension.get(),
          inBuildDir = d2Spec.intermediateFilesInBuildDir.get(),
        )
      val chartTask = WriteD2Chart.real(context = context, outputFile = d2File)

      // The `...@` import is relative to wherever the chart lands, and that isn't necessarily
      // [d2File] - outputFile is public, so a build can move it. Reading it back off the task only
      // gives the moved location if this action is registered after the build script has run,
      // hence afterEvaluate. It has to be read eagerly here too: a provider built from the task's
      // own output property makes the task depend on itself.
      afterEvaluate {
        chartTask.configure { task ->
          val chartDirectory = task.outputFile.get().asFile.parentFile
          task.pathToClassesFile.convention(importPath(classesFile, chartDirectory))
        }
      }

      // Nothing to check when the chart file lives in the build directory
      if (!d2Spec.intermediateFilesInBuildDir.get()) {
        val dummyChartTask =
          WriteD2Chart.dummy(
            context = context,
            outputFile = atlasBuildDirectory.get().file("chart-temp.d2").asFile,
          )

        dummyChartTask.configure { task ->
          // The dummy writes to the build directory, so working the import out from its own output
          // would give a different path to the real chart's and the check would report a diff that
          // isn't there. Borrow the real chart's directory instead, read eagerly - as CheckFileDiff
          // does - to break the task dependency on the real task.
          val chartDirectory = chartTask.map { it.outputFile }.get().get().asFile.parentFile
          task.pathToClassesFile.convention(importPath(classesFile, chartDirectory))
        }

        CheckFileDiff.register(
          target = this,
          config = context.config,
          spec = d2Spec,
          variant = Chart,
          realTask = chartTask,
          dummyTask = dummyChartTask,
        )
      }

      val d2Task =
        ExecD2.register(
          target = this,
          config = context.config,
          spec = d2Spec,
          variant = Chart,
          d2FileTask = chartTask,
          classesFile = classesFile,
        )

      ChartFiles(
        framework = framework,
        chart = d2Task.flatMap { it.outputFile },
        legend = null,
      )
    }

  /** The `...@` import d2 resolves against a chart sitting in [chartDirectory]. */
  private fun importPath(classesFile: FileCollection, chartDirectory: File): Provider<String> =
    classesFile.singleFile(D2Classes).map { it.relativeTo(chartDirectory).path }

  private fun Project.warnIfLabelLocationSpecifiedButNotPosition(context: AtlasContext) {
    val d2 = context.d2
    val position = d2.groupLabelPosition.orNull
    val location = d2.groupLabelLocation.orNull
    val shouldSuppress = d2.properties.suppressLabelLocationWarning.get()
    if (position == null && location != null && !shouldSuppress) {
      logger.warn(
        "Warning: you've configured groupLabelLocation but not groupLabelPosition - this is not supported in D2 " +
          "diagrams. If you want to suppress this warning, add 'atlas.d2.suppressLabelLocationWarning=true' to " +
          "your gradle.properties file."
      )
    }
  }

  private fun Project.warnIfAnimationSelectedWithNonAnimatedFileFormat(context: AtlasContext) {
    val d2 = context.d2
    val format = d2.fileFormat.get()
    val animatedFormats = setOf<FileFormat>(Svg, Gif)
    val animated = d2.animateLinks.orNull
    val shouldSuppress = d2.properties.suppressAnimationWarning.get()
    if (animated == true && format !in animatedFormats && !shouldSuppress) {
      logger.warn(
        "Warning: you've configured animateLinks but chosen a non-animatable file format ($format). If you want to " +
          "suppress this warning, add 'atlas.d2.suppressAnimationWarning=true' to your gradle.properties file."
      )
    }
  }
}
