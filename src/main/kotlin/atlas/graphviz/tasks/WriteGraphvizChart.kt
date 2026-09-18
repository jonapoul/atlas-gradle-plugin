package atlas.graphviz.tasks

import atlas.core.Replacement
import atlas.core.internal.ATLAS_TASK_GROUP
import atlas.core.internal.AtlasContext
import atlas.core.internal.DummyAtlasGenerationTask
import atlas.core.internal.atlasBuildDirectory
import atlas.core.internal.intermediateFile
import atlas.core.internal.logIfConfigured
import atlas.core.internal.qualifier
import atlas.core.internal.readProjectLinks
import atlas.core.internal.readProjectTypes
import atlas.core.internal.singleFile
import atlas.core.tasks.AtlasGenerationTask
import atlas.core.tasks.TaskWithOutputFile
import atlas.core.tasks.WriteProjectTree
import atlas.graphviz.DotConfig
import atlas.graphviz.GraphvizSpec
import atlas.graphviz.internal.DotWriter
import java.io.File
import org.gradle.api.DefaultTask
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.Property
import org.gradle.api.provider.SetProperty
import org.gradle.api.tasks.CacheableTask
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputFile
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.PathSensitive
import org.gradle.api.tasks.TaskAction
import org.gradle.api.tasks.TaskProvider
import org.gradle.work.DisableCachingByDefault

/** Converts a [DotConfig] into a written project chart file, generated once for each project. */
@CacheableTask
public abstract class WriteGraphvizChart : DefaultTask(), TaskWithOutputFile, AtlasGenerationTask {
  // Files
  @get:[PathSensitive(NONE) InputFile]
  public abstract val linksFile: RegularFileProperty
  @get:[PathSensitive(NONE) InputFile]
  public abstract val projectTypesFile: RegularFileProperty
  @get:OutputFile abstract override val outputFile: RegularFileProperty

  // General
  @get:Input public abstract val groupProjects: Property<Boolean>
  @get:Input public abstract val replacements: SetProperty<Replacement>
  @get:Input public abstract val thisPath: Property<String>

  // Dotfile config
  @get:Input public abstract val config: Property<DotConfig>

  init {
    group = ATLAS_TASK_GROUP
    description = "Generates a project dependency graph in dotfile format"
  }

  @TaskAction
  public open fun execute() {
    val linksFile = linksFile.get().asFile
    val projectTypesFile = projectTypesFile.get().asFile

    val writer =
      DotWriter(
        typedProjects = readProjectTypes(projectTypesFile),
        links = readProjectLinks(linksFile),
        replacements = replacements.get(),
        thisPath = thisPath.get(),
        groupProjects = groupProjects.get(),
        config = config.get(),
      )

    val outputFile = outputFile.get().asFile
    outputFile.writeText(writer())
    logIfConfigured(outputFile)
  }

  @DisableCachingByDefault
  internal abstract class WriteGraphvizChartDummy : WriteGraphvizChart(), DummyAtlasGenerationTask

  internal companion object {
    internal fun real(context: AtlasContext, spec: GraphvizSpec) =
      register<WriteGraphvizChart>(
        context = context,
        spec = spec,
        outputFile =
          context.project.intermediateFile(
            config = context.config,
            framework = Graphviz,
            variant = Chart,
            fileExtension = spec.fileExtension.get(),
            inBuildDir = spec.intermediateFilesInBuildDir.get(),
          ),
      )

    internal fun dummy(context: AtlasContext, spec: GraphvizSpec) =
      register<WriteGraphvizChartDummy>(
        context = context,
        spec = spec,
        outputFile = context.project.atlasBuildDirectory.get().file("chart-temp.dot").asFile,
      )

    private inline fun <reified T : WriteGraphvizChart> register(
      context: AtlasContext,
      spec: GraphvizSpec,
      outputFile: File,
    ): TaskProvider<T> =
      with(context.project) {
        val collatedTypes = context.fromRoot(CollatedTypes)
        val calculateProjectTree = WriteProjectTree.get(this)
        val name = "write${T::class.qualifier}GraphvizChart"
        val writeChart =
          tasks.register(name, T::class.java) { task ->
            task.linksFile.convention(calculateProjectTree.flatMap { it.outputFile })
            task.outputFile.set(outputFile)
            task.thisPath.convention(path)
          }

        writeChart.configure { task ->
          task.projectTypesFile.fileProvider(collatedTypes.singleFile(CollatedTypes))
          task.dependsOn(collatedTypes)
          task.groupProjects.convention(context.config.groupProjects)
          task.replacements.convention(context.config.replacements)
          task.config.convention(DotConfig(context.config, spec))
        }

        return writeChart
      }
  }
}
