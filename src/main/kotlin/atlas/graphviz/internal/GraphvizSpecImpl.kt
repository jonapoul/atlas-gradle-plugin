package atlas.graphviz.internal

import atlas.core.internal.bool
import atlas.core.internal.enum
import atlas.core.internal.string
import atlas.graphviz.ClusterAttributes
import atlas.graphviz.EdgeAttributes
import atlas.graphviz.FileFormat
import atlas.graphviz.GraphAttributes
import atlas.graphviz.GraphvizSpec
import atlas.graphviz.LayoutEngine
import atlas.graphviz.NodeAttributes
import org.gradle.api.Action
import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.Property
import org.gradle.api.provider.ProviderFactory

internal class GraphvizSpecImpl(
  objects: ObjectFactory,
  providers: ProviderFactory,
) : GraphvizSpec {
  internal val properties = GraphvizGradleProperties(providers)

  override val name: String = "Graphviz"
  override val fileExtension: Property<String> = objects.string(convention = "dot")
  override val pathToDotCommand: Property<String> = objects.string(convention = null)
  override val fileFormat: Property<FileFormat> = objects.enum(properties.fileFormat)
  override val intermediateFilesInBuildDir: Property<Boolean> =
    objects.bool(properties.intermediateFilesInBuildDir)
  override val layoutEngine: Property<LayoutEngine> = objects.enum(properties.layoutEngine)

  override val node = NodeAttributesImpl(objects, providers)

  override fun node(action: Action<NodeAttributes>) = action.execute(node)

  override val edge = EdgeAttributesImpl(objects, providers)

  override fun edge(action: Action<EdgeAttributes>) = action.execute(edge)

  override val graph = GraphAttributesImpl(objects, providers)

  override fun graph(action: Action<GraphAttributes>) = action.execute(graph)

  override val cluster = ClusterAttributesImpl(objects, providers)

  override fun cluster(action: Action<ClusterAttributes>) = action.execute(cluster)
}
