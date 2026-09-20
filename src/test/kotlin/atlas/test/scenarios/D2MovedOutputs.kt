package atlas.test.scenarios

import atlas.test.D2Scenario

/**
 * Both intermediate files are moved out of the directory Atlas picked, so the `...@` import in each
 * chart has to be worked out from where they actually landed.
 *
 * Tasks are moved by name rather than with `withType`, so that the dummy tasks Atlas registers for
 * `check` keep their own locations - pointing a real task and its dummy at one file would have them
 * overwrite each other.
 */
internal object D2MovedOutputs : D2Scenario {
  override val rootBuildFile =
    """
    plugins {
      kotlin("jvm") apply false
    }

    tasks.named("writeD2Classes", atlas.d2.tasks.WriteD2Classes::class.java) { task ->
      task.outputFile.set(layout.projectDirectory.file("charts/classes.d2"))
    }
    """
      .trimIndent()

  override val atlasConfig =
    """
    projectTypes {
      kotlinJvm()
    }
    """
      .trimIndent()

  override val subprojectBuildFiles =
    mapOf(
      "a" to
        """
        plugins {
          kotlin("jvm")
        }

        tasks.named("writeD2Chart", atlas.d2.tasks.WriteD2Chart::class.java) { task ->
          task.outputFile.set(layout.projectDirectory.file("charts/chart.d2"))
        }

        dependencies {
          api(project(":nested:b"))
        }
        """
          .trimIndent(),
      "nested:b" to
        """
        plugins {
          kotlin("jvm")
        }

        tasks.named("writeD2Chart", atlas.d2.tasks.WriteD2Chart::class.java) { task ->
          task.outputFile.set(layout.projectDirectory.file("charts/chart.d2"))
        }
        """
          .trimIndent(),
    )
}
