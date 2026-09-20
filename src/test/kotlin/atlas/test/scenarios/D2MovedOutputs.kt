package atlas.test.scenarios

import atlas.test.D2Scenario

/**
 * Both intermediate files are moved out of the per-framework directory Atlas picked, so the `...@`
 * import in each chart has to be worked out from where the files actually landed.
 */
internal object D2MovedOutputs : D2Scenario {
  override val rootBuildFile =
    """
    plugins {
      kotlin("jvm") apply false
    }

    tasks.withType(atlas.d2.tasks.WriteD2Classes::class.java).configureEach {
      outputFile.set(layout.buildDirectory.file("atlas/classes.d2"))
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

        tasks.withType(atlas.d2.tasks.WriteD2Chart::class.java).configureEach {
          outputFile.set(layout.buildDirectory.file("atlas/chart.d2"))
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

        tasks.withType(atlas.d2.tasks.WriteD2Chart::class.java).configureEach {
          outputFile.set(layout.buildDirectory.file("atlas/chart.d2"))
        }
        """
          .trimIndent(),
    )
}
