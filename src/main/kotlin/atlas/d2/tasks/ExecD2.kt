package atlas.d2.tasks

import atlas.core.internal.ATLAS_TASK_GROUP
import atlas.core.internal.AtlasConfig
import atlas.core.internal.Variant
import atlas.core.internal.logIfConfigured
import atlas.core.internal.outputFile
import atlas.core.internal.singleFile
import atlas.core.tasks.AtlasGenerationTask
import atlas.core.tasks.TaskWithOutputFile
import atlas.d2.D2Spec
import atlas.d2.FileFormat
import java.io.ByteArrayOutputStream
import javax.inject.Inject
import org.gradle.api.DefaultTask
import org.gradle.api.GradleException
import org.gradle.api.Project
import org.gradle.api.file.FileCollection
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.MapProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.CacheableTask
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputFile
import org.gradle.api.tasks.Optional
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.PathSensitive
import org.gradle.api.tasks.TaskAction
import org.gradle.api.tasks.TaskProvider
import org.gradle.process.ExecOperations

/**
 * Executes `d2` with the configured inputs to generate an image file. Requires D2 to be
 * pre-installed.
 *
 * Just so I don't forget, [classesFile] is only used to force regeneration if the classes file
 * updates, since we don't directly read it in this task.
 */
@CacheableTask
public abstract class ExecD2 : DefaultTask(), AtlasGenerationTask, TaskWithOutputFile {
  @get:[PathSensitive(NONE) InputFile]
  public abstract val classesFile: RegularFileProperty
  @get:[PathSensitive(NONE) InputFile]
  public abstract val inputFile: RegularFileProperty
  @get:Input public abstract val outputFormat: Property<FileFormat>
  @get:[Input Optional]
  public abstract val animateInterval: Property<Int>
  @get:[Input Optional]
  public abstract val scale: Property<Float>
  @get:[Input Optional]
  public abstract val cliArguments: MapProperty<String, String>
  @get:[Input Optional]
  public abstract val pathToD2Command: Property<String>
  @get:OutputFile abstract override val outputFile: RegularFileProperty
  @get:Inject public abstract val execOperations: ExecOperations

  init {
    group = ATLAS_TASK_GROUP
  }

  // Not using kotlin setter because this pulls a property value
  override fun getDescription(): String =
    "Uses D2 to convert a text diagram into a ${outputFormat.get()} file"

  @TaskAction
  public fun execute() {
    val inputFile = inputFile.get().asFile.absolutePath
    val outputFile = outputFile.get().asFile
    val d2Executable = pathToD2Command.getOrElse("d2")
    val cliArguments = cliArguments.getOrElse(mutableMapOf())

    // D2 defaults this to 1000ms for gifs, so it's only passed along when explicitly configured.
    if (outputFormat.get() == Gif) {
      animateInterval.orNull?.let { cliArguments += "animate-interval" to it.toString() }
    }

    scale.orNull?.let { cliArguments += "scale" to it.toString() }

    val errorBuffer = ByteArrayOutputStream()
    val command = buildList {
      add(d2Executable)
      add(inputFile)
      add(outputFile)
      cliArguments.forEach { (key, value) -> add("--$key=$value") }
    }

    logger.info("Starting d2: '$command'")
    val result = execOperations.exec { spec ->
      spec.errorOutput = errorBuffer
      spec.standardOutput = outputFile.outputStream()
      spec.isIgnoreExitValue = true
      spec.commandLine(command)
    }

    if (result.exitValue != 0) {
      val cmd = command.joinToString(separator = " ")
      throw GradleException("Error code ${result.exitValue} running '$cmd':\n $errorBuffer")
    }

    logIfConfigured(outputFile)
  }

  internal companion object {
    internal fun get(target: Project, name: String): TaskProvider<ExecD2> =
      target.tasks.named(name, ExecD2::class.java)

    internal fun <T : TaskWithOutputFile> register(
      target: Project,
      config: AtlasConfig,
      spec: D2Spec,
      variant: Variant,
      d2FileTask: TaskProvider<T>,
      classesFile: FileCollection,
    ): TaskProvider<ExecD2> =
      with(target) {
        val name = "execD2$variant"
        val execD2 = tasks.register(name, ExecD2::class.java)

        execD2.configure { task ->
          val d2File = d2FileTask.flatMap { it.outputFile }
          val imageFile = provider {
            outputFile(config, D2, variant, fileExtension = spec.fileFormat.get().string)
          }

          task.classesFile.fileProvider(classesFile.singleFile(D2Classes))
          task.dependsOn(classesFile)
          task.inputFile.convention(d2File)
          task.pathToD2Command.convention(spec.pathToD2Command)
          task.outputFormat.convention(spec.fileFormat)
          task.outputFile.convention(layout.file(imageFile))
          task.cliArguments.convention(spec.layoutEngine.properties)
          task.animateInterval.convention(spec.animateInterval)
          task.scale.convention(spec.scale)
        }

        return execD2
      }
  }
}
