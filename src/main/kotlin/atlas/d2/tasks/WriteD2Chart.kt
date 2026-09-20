package atlas.d2.tasks

import atlas.core.Replacement
import atlas.core.internal.ATLAS_TASK_GROUP
import atlas.core.internal.AtlasContext
import atlas.core.internal.DummyAtlasGenerationTask
import atlas.core.internal.logIfConfigured
import atlas.core.internal.qualifier
import atlas.core.internal.readProjectLinks
import atlas.core.internal.readProjectTypes
import atlas.core.internal.singleFile
import atlas.core.tasks.AtlasGenerationTask
import atlas.core.tasks.TaskWithOutputFile
import atlas.core.tasks.WriteProjectTree
import atlas.d2.internal.D2Writer
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

@CacheableTask
public abstract class WriteD2Chart : DefaultTask(), TaskWithOutputFile, AtlasGenerationTask {
  // Files
  @get:[PathSensitive(NONE) InputFile]
  public abstract val linksFile: RegularFileProperty
  @get:[PathSensitive(NONE) InputFile]
  public abstract val projectTypesFile: RegularFileProperty
  @get:Input public abstract val pathToClassesFile: Property<String>
  @get:OutputFile abstract override val outputFile: RegularFileProperty

  // General
  @get:Input public abstract val groupProjects: Property<Boolean>
  @get:Input public abstract val replacements: SetProperty<Replacement>
  @get:Input public abstract val thisPath: Property<String>

  init {
    group = ATLAS_TASK_GROUP
    description = "Generates a project dependency graph in d2 format"
  }

  @TaskAction
  public open fun execute() {
    val linksFile = linksFile.get().asFile
    val projectTypesFile = projectTypesFile.get().asFile
    val outputFile = outputFile.get().asFile
    val pathToClassesFile = pathToClassesFile.get()

    val writer =
      D2Writer(
        typedProjects = readProjectTypes(projectTypesFile),
        links = readProjectLinks(linksFile),
        replacements = replacements.get(),
        thisPath = thisPath.get(),
        groupProjects = groupProjects.get(),
        pathToClassesFile = pathToClassesFile,
      )

    outputFile.writeText(writer())
    logIfConfigured(outputFile)
  }

  @DisableCachingByDefault
  internal abstract class WriteD2ChartDummy : WriteD2Chart(), DummyAtlasGenerationTask

  internal companion object {
    internal fun real(context: AtlasContext, outputFile: File) =
      register<WriteD2Chart>(context, outputFile)

    internal fun dummy(context: AtlasContext, outputFile: File) =
      register<WriteD2ChartDummy>(context, outputFile)

    private inline fun <reified T : WriteD2Chart> register(
      context: AtlasContext,
      outputFile: File,
    ): TaskProvider<T> =
      with(context.project) {
        val collatedTypes = context.fromRoot(CollatedTypes)
        val writeProjectTree = WriteProjectTree.get(this)
        val name = "write${T::class.qualifier}D2Chart"
        val writeChart =
          tasks.register(name, T::class.java) { task ->
            task.linksFile.convention(writeProjectTree.flatMap { it.outputFile })
            task.outputFile.set(outputFile)
            task.thisPath.convention(path)
          }

        writeChart.configure { task ->
          task.projectTypesFile.fileProvider(collatedTypes.singleFile(CollatedTypes))
          task.dependsOn(collatedTypes)
          task.groupProjects.convention(context.config.groupProjects)
          task.replacements.convention(context.config.replacements)
        }

        return writeChart
      }
  }
}
