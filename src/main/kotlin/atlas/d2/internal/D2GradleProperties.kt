package atlas.d2.internal

import atlas.core.internal.IGradleProperties
import atlas.core.internal.bool
import atlas.core.internal.enum
import atlas.core.internal.float
import atlas.core.internal.int
import atlas.core.internal.intEnum
import atlas.core.internal.string
import atlas.d2.AsciiMode
import atlas.d2.Direction
import atlas.d2.ExecutableSource
import atlas.d2.FileFormat
import atlas.d2.LayoutEngine
import atlas.d2.Location
import atlas.d2.Position
import atlas.d2.Theme
import org.gradle.api.provider.Provider
import org.gradle.api.provider.ProviderFactory

internal class D2GradleProperties(override val providers: ProviderFactory) : IGradleProperties {
  // Actual config
  val animateLinks: Provider<Boolean> = bool("atlas.d2.animateLinks", default = null)
  val animateInterval: Provider<Int> = int("atlas.d2.animateInterval", default = null)
  val asciiMode: Provider<AsciiMode> = enum("atlas.d2.asciiMode", default = null)
  val center: Provider<Boolean> = bool("atlas.d2.center", default = null)
  val d2Executable: Provider<String> = string("atlas.d2.d2Executable", default = null)
  val d2Version: Provider<String> = string("atlas.d2.d2Version", default = DEFAULT_D2_VERSION)
  val darkTheme: Provider<Theme> = intEnum("atlas.d2.darkTheme", default = null)
  val direction: Provider<Direction> = enum("atlas.d2.direction", default = null)
  val executableSource: Provider<ExecutableSource> =
    enum("atlas.d2.executableSource", default = Auto)
  val fileFormat: Provider<FileFormat> = enum("atlas.d2.fileFormat", default = Svg)
  val groupLabelLocation: Provider<Location> = enum("atlas.d2.groupLabelLocation", default = null)
  val groupLabelPosition: Provider<Position> = enum("atlas.d2.groupLabelPosition", default = null)
  val layoutEngine: Provider<LayoutEngine> = enum("atlas.d2.layoutEngine", default = null)
  val intermediateFilesInBuildDir: Provider<Boolean> =
    bool("atlas.d2.intermediateFilesInBuildDir", default = true)
  val noXmlTag: Provider<Boolean> = bool("atlas.d2.noXmlTag", default = null)
  val omitVersion: Provider<Boolean> = bool("atlas.d2.omitVersion", default = null)
  val pad: Provider<Int> = int("atlas.d2.pad", default = null)
  val scale: Provider<Float> = float("atlas.d2.scale", default = null)
  val sketch: Provider<Boolean> = bool("atlas.d2.sketch", default = null)
  val theme: Provider<Theme> = intEnum("atlas.d2.theme", default = null)
  val timeout: Provider<Int> = int("atlas.d2.timeout", default = null)

  // Warning suppressions
  val suppressLabelLocationWarning: Provider<Boolean> =
    bool("atlas.d2.suppressLabelLocationWarning", default = false)
  val suppressAnimationWarning: Provider<Boolean> =
    bool("atlas.d2.suppressAnimationWarning", default = false)
}
