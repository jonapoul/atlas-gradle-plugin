package atlas.graphviz.internal

import atlas.core.Framework
import atlas.core.internal.AtlasArtifact
import atlas.core.internal.AtlasContext
import atlas.core.internal.ChartFiles
import atlas.core.internal.FrameworkTasks
import atlas.core.internal.publishAtlasArtifact
import atlas.core.tasks.CheckFileDiff
import atlas.graphviz.tasks.ExecGraphviz
import atlas.graphviz.tasks.WriteGraphvizChart
import atlas.graphviz.tasks.WriteGraphvizLegend

internal object GraphvizTasks : FrameworkTasks {
  override val framework: Framework = Graphviz

  override fun registerRootTasks(context: AtlasContext): Unit =
    with(context.project) {
      val spec = context.graphviz

      val realTask = WriteGraphvizLegend.real(context = context, spec = spec)

      val execLegend =
        ExecGraphviz.register(
          target = this,
          config = context.config,
          spec = spec,
          variant = Legend,
          dotFileTask = realTask,
        )

      // The README of every project links to this one file, and under isolated projects a
      // subproject can't reach the task that draws it, so publish it as an artifact instead.
      publishAtlasArtifact(
        artifact = AtlasArtifact.legend(framework),
        file = execLegend.flatMap { it.outputFile },
        builtBy = execLegend,
      )

      // Also validate the legend's dotfile when we call gradle check, unless it's in the build dir
      if (!spec.intermediateFilesInBuildDir.get()) {
        val dummyTask = WriteGraphvizLegend.dummy(context = context, spec = spec)

        CheckFileDiff.register(
          target = this,
          config = context.config,
          spec = spec,
          variant = Legend,
          realTask = realTask,
          dummyTask = dummyTask,
        )
      }
    }

  override fun registerChildTasks(context: AtlasContext): ChartFiles =
    with(context.project) {
      val graphvizSpec = context.graphviz

      val chartTask = WriteGraphvizChart.real(context = context, spec = graphvizSpec)
      // Nothing to check when the dotfile lives in the build directory
      if (!graphvizSpec.intermediateFilesInBuildDir.get()) {
        val dummyChartTask = WriteGraphvizChart.dummy(context = context, spec = graphvizSpec)

        CheckFileDiff.register(
          target = this,
          config = context.config,
          spec = graphvizSpec,
          variant = Chart,
          realTask = chartTask,
          dummyTask = dummyChartTask,
        )
      }

      val graphvizTask =
        ExecGraphviz.register(
          target = this,
          config = context.config,
          spec = graphvizSpec,
          variant = Chart,
          dotFileTask = chartTask,
        )

      ChartFiles(
        framework = framework,
        chart = graphvizTask.flatMap { it.outputFile },
        legend = context.fromRoot(AtlasArtifact.legend(framework)),
      )
    }
}
