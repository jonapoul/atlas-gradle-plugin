package atlas.d2.tasks

import atlas.core.internal.ATLAS_TASK_GROUP
import atlas.core.internal.AtlasConfig
import atlas.core.internal.Variant
import atlas.core.internal.logIfConfigured
import atlas.core.internal.outputFile
import atlas.core.internal.singleFile
import atlas.core.tasks.AtlasGenerationTask
import atlas.core.tasks.TaskWithOutputFile
import atlas.d2.AsciiMode
import atlas.d2.D2Spec
import atlas.d2.ExecutableSource
import atlas.d2.ExecutableSource.Auto
import atlas.d2.ExecutableSource.Download
import atlas.d2.ExecutableSource.System
import atlas.d2.FileFormat
import atlas.d2.internal.D2SpecImpl
import atlas.d2.internal.DEFAULT_D2_VERSION
import atlas.d2.internal.downloadedD2
import java.io.ByteArrayOutputStream
import java.io.File
import javax.inject.Inject
import org.gradle.api.DefaultTask
import org.gradle.api.GradleException
import org.gradle.api.Project
import org.gradle.api.file.FileCollection
import org.gradle.api.file.RegularFile
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.MapProperty
import org.gradle.api.provider.Property
import org.gradle.api.provider.Provider
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
 * Executes `d2` with the configured inputs to generate an image file. [d2Executable] is whichever
 * of the configured one, the one on the system PATH or a downloaded one applies.
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
  public abstract val asciiMode: Property<AsciiMode>
  @get:[Input Optional]
  public abstract val noXmlTag: Property<Boolean>
  @get:[Input Optional]
  public abstract val omitVersion: Property<Boolean>
  @get:[Input Optional]
  public abstract val scale: Property<Float>
  @get:[Input Optional]
  public abstract val timeoutSeconds: Property<Int>
  @get:[PathSensitive(NONE) InputFile Optional]
  public abstract val fontRegular: RegularFileProperty
  @get:[PathSensitive(NONE) InputFile Optional]
  public abstract val fontItalic: RegularFileProperty
  @get:[PathSensitive(NONE) InputFile Optional]
  public abstract val fontBold: RegularFileProperty
  @get:[PathSensitive(NONE) InputFile Optional]
  public abstract val fontSemibold: RegularFileProperty
  @get:[PathSensitive(NONE) InputFile Optional]
  public abstract val fontMono: RegularFileProperty
  @get:[PathSensitive(NONE) InputFile Optional]
  public abstract val fontMonoBold: RegularFileProperty
  @get:[PathSensitive(NONE) InputFile Optional]
  public abstract val fontMonoItalic: RegularFileProperty
  @get:[PathSensitive(NONE) InputFile Optional]
  public abstract val fontMonoSemibold: RegularFileProperty
  @get:[Input Optional]
  public abstract val cliArguments: MapProperty<String, String>
  @get:[PathSensitive(NONE) InputFile Optional]
  public abstract val d2Executable: RegularFileProperty
  @get:OutputFile abstract override val outputFile: RegularFileProperty
  @get:Inject public abstract val execOperations: ExecOperations

  init {
    group = ATLAS_TASK_GROUP
  }

  // Not using kotlin setter because this pulls a property value
  override fun getDescription(): String =
    "Uses D2 to convert a text diagram into a ${outputFormat.get()} file"

  @TaskAction
  @Suppress("CyclomaticComplexMethod")
  public fun execute() {
    val inputFile = inputFile.get().asFile.absolutePath
    val outputFile = outputFile.get().asFile
    val executable = d2Executable.orNull?.asFile?.absolutePath ?: "d2"
    val cliArguments = cliArguments.getOrElse(emptyMap()).toMutableMap()

    // D2 defaults this to 1000ms for gifs, so it's only passed along when explicitly configured.
    if (outputFormat.get() == Gif) {
      animateInterval.orNull?.let { cliArguments += "animate-interval" to it.toString() }
    }

    if (outputFormat.get() == Ascii) {
      asciiMode.orNull?.let { cliArguments += "ascii-mode" to it.value }
    }

    if (outputFormat.get() == Svg && noXmlTag.getOrElse(false)) {
      cliArguments += "no-xml-tag" to "true"
    }

    if (omitVersion.getOrElse(false)) {
      cliArguments += "omit-version" to "true"
    }

    scale.orNull?.let { cliArguments += "scale" to it.toString() }
    timeoutSeconds.orNull?.let { cliArguments += "timeout" to it.toString() }

    mapOf(
        "font-regular" to fontRegular,
        "font-italic" to fontItalic,
        "font-bold" to fontBold,
        "font-semibold" to fontSemibold,
        "font-mono" to fontMono,
        "font-mono-bold" to fontMonoBold,
        "font-mono-italic" to fontMonoItalic,
        "font-mono-semibold" to fontMonoSemibold,
      )
      .forEach { (key, font) ->
        font.orNull?.let {
          cliArguments += key to it.asFile.absolutePath
        }
      }

    val errorBuffer = ByteArrayOutputStream()
    val command = buildList {
      add(executable)
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
      spec: D2SpecImpl,
      variant: Variant,
      d2FileTask: TaskProvider<T>,
      classesFile: FileCollection,
    ): TaskProvider<ExecD2> =
      with(target) {
        val name = "execD2$variant"
        val execD2 = tasks.register(name, ExecD2::class.java)
        // Outside configure {}, since the download wiring needs an afterEvaluate hook
        val d2Executable = d2Executable(spec, config)

        execD2.configure { task ->
          val d2File = d2FileTask.flatMap { it.outputFile }
          val imageFile = provider {
            outputFile(config, D2, variant, fileExtension = spec.fileFormat.get().value)
          }

          task.classesFile.fileProvider(classesFile.singleFile(D2Classes))
          task.dependsOn(classesFile)
          task.inputFile.convention(d2File)
          task.d2Executable.convention(d2Executable)
          task.outputFormat.convention(spec.fileFormat)
          task.outputFile.convention(layout.file(imageFile))
          task.cliArguments.convention(spec.layoutEngine.properties)
          task.animateInterval.convention(spec.animateInterval)
          task.asciiMode.convention(spec.asciiMode)
          task.noXmlTag.convention(spec.noXmlTag)
          task.omitVersion.convention(spec.omitVersion)
          task.scale.convention(spec.scale)
          task.timeoutSeconds.convention(spec.timeout)
          with(spec.fonts) {
            task.fontRegular.convention(regular)
            task.fontItalic.convention(italic)
            task.fontBold.convention(bold)
            task.fontSemibold.convention(semibold)
            task.fontMono.convention(mono)
            task.fontMonoBold.convention(monoBold)
            task.fontMonoItalic.convention(monoItalic)
            task.fontMonoSemibold.convention(monoSemibold)
          }
        }

        return execD2
      }

    /**
     * The explicitly configured `d2` always wins. Otherwise [ExecutableSource] decides between the
     * one on the PATH and a download, which is only resolved when the PATH didn't have one. Setting
     * [D2Spec.d2Version] means the user wants that version, so [Auto] skips the PATH for it.
     */
    private fun Project.d2Executable(spec: D2SpecImpl, config: AtlasConfig): Provider<RegularFile> {
      val source = spec.executableSource
      val explicit = spec.d2Executable.orElse(layout.file(spec.properties.d2Executable.map(::File)))
      val isPinned = spec.d2Version.map { true }.orElse(false)
      val skipPath = source.zip(isPinned) { s, pinned -> s == Download || (s == Auto && pinned) }
      val onPath =
        layout.file(
          skipPath.flatMap { skip -> if (skip) providers.provider<File> { null } else d2OnPath() }
        )
      val downloaded = downloadedD2(spec.d2Version.orElse(DEFAULT_D2_VERSION), config)
      val download = source.flatMap { s ->
        if (s == System) providers.provider<RegularFile> { null } else downloaded
      }
      return explicit.orElse(onPath).orElse(download)
    }

    // Resolved up front rather than leaving it to exec, so the binary is a task input and a
    // different d2 version invalidates the cached chart.
    private fun Project.d2OnPath(): Provider<File> =
      providers.environmentVariable("PATH").flatMap { path ->
        providers.provider {
          path
            .split(File.pathSeparator)
            .flatMap { dir -> listOf(File(dir, "d2"), File(dir, "d2.exe")) }
            .firstOrNull { it.isFile && it.canExecute() }
        }
      }
  }
}
