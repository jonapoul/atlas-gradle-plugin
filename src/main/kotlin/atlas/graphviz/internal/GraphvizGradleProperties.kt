package atlas.graphviz.internal

import atlas.core.internal.IGradleProperties
import atlas.core.internal.bool
import atlas.core.internal.enum
import atlas.graphviz.FileFormat
import atlas.graphviz.LayoutEngine
import org.gradle.api.provider.Provider
import org.gradle.api.provider.ProviderFactory

internal class GraphvizGradleProperties(override val providers: ProviderFactory) :
  IGradleProperties {
  val fileFormat: Provider<FileFormat> = enum("atlas.graphviz.fileFormat", default = Svg)
  val intermediateFilesInBuildDir: Provider<Boolean> =
    bool("atlas.graphviz.intermediateFilesInBuildDir", default = true)
  val layoutEngine: Provider<LayoutEngine> = enum("atlas.graphviz.layoutEngine", default = null)
}
