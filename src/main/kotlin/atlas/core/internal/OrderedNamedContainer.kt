package atlas.core.internal

import atlas.core.LinkTypeSpec
import atlas.core.NamedLinkTypeContainer
import atlas.core.NamedProjectTypeContainer
import atlas.core.ProjectTypeSpec
import groovy.lang.Closure
import java.util.function.IntFunction
import org.gradle.api.Action
import org.gradle.api.NamedDomainObjectContainer
import org.gradle.api.NamedDomainObjectProvider
import org.gradle.api.model.ObjectFactory

internal class ProjectTypeContainer(objects: ObjectFactory) :
  OrderedNamedContainer<ProjectTypeSpec>(
    container =
      objects.domainObjectContainer(ProjectTypeSpec::class.java) { name ->
        objects.newInstance(ProjectTypeSpecImpl::class.java, name)
      }
  ),
  NamedProjectTypeContainer

internal class LinkTypeContainer(objects: ObjectFactory) :
  OrderedNamedContainer<LinkTypeSpec>(
    container =
      objects.domainObjectContainer(LinkTypeSpec::class.java) { name ->
        objects.newInstance(LinkTypeSpecImpl::class.java, name)
      }
  ),
  NamedLinkTypeContainer

@Suppress("PreferRegisterOverCreate") // passthrough overrides
internal open class OrderedNamedContainer<T : Any>(
  private val container: NamedDomainObjectContainer<T>
) : NamedDomainObjectContainer<T> by container {
  private val orderedNames = mutableSetOf<String>()

  override fun register(
    name: String,
    configurationAction: Action<in T>,
  ): NamedDomainObjectProvider<T> {
    orderedNames.add(name)
    return container.register(name, configurationAction)
  }

  override fun register(name: String): NamedDomainObjectProvider<T> {
    orderedNames.add(name)
    return container.register(name)
  }

  override fun create(name: String): T {
    orderedNames.add(name)
    return container.create(name)
  }

  override fun create(name: String, configureAction: Action<in T>): T {
    orderedNames.add(name)
    return container.create(name, configureAction)
  }

  override fun create(name: String, configureClosure: Closure<*>): T {
    orderedNames.add(name)
    return container.create(name, configureClosure)
  }

  override fun maybeCreate(name: String): T {
    orderedNames.add(name)
    return container.maybeCreate(name)
  }

  fun getInOrder(): List<T> = orderedNames.map(container::getByName)

  @Suppress("OVERRIDE_DEPRECATION", "RedundantOverride", "DEPRECATION")
  override fun <T : Any> toArray(generator: IntFunction<Array<out T>>): Array<out T> =
    super.toArray(generator)

  @Suppress("UnstableApiUsage", "RedundantOverride")
  override fun disallowChanges() = super.disallowChanges()
}
