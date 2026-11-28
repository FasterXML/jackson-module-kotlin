package tools.jackson.module.kotlin

import com.fasterxml.jackson.annotation.Nulls
import tools.jackson.databind.BeanDescription
import tools.jackson.databind.BeanProperty
import tools.jackson.databind.DeserializationConfig
import tools.jackson.databind.DeserializationContext
import tools.jackson.databind.JavaType
import tools.jackson.databind.ValueDeserializer
import tools.jackson.databind.deser.ValueDeserializerModifier
import tools.jackson.databind.deser.std.DelegatingDeserializer
import tools.jackson.databind.introspect.AnnotatedParameter
import tools.jackson.databind.type.ArrayType
import tools.jackson.databind.type.CollectionType
import tools.jackson.databind.type.MapType
import kotlin.reflect.KType

internal class KotlinNestedNullChecks(private val cache: ReflectionCache) : ValueDeserializerModifier() {
    override fun modifyArrayDeserializer(
        config: DeserializationConfig,
        valueType: ArrayType,
        beanDescRef: BeanDescription.Supplier,
        deserializer: ValueDeserializer<*>,
    ): ValueDeserializer<*> = NestedContainerDeserializer(deserializer)

    override fun modifyCollectionDeserializer(
        config: DeserializationConfig,
        type: CollectionType,
        beanDescRef: BeanDescription.Supplier,
        deserializer: ValueDeserializer<*>,
    ): ValueDeserializer<*> = NestedContainerDeserializer(deserializer)

    override fun modifyMapDeserializer(
        config: DeserializationConfig,
        type: MapType,
        beanDescRef: BeanDescription.Supplier,
        deserializer: ValueDeserializer<*>,
    ): ValueDeserializer<*> = NestedContainerDeserializer(deserializer)

    private inner class NestedContainerDeserializer(delegate: ValueDeserializer<*>) : DelegatingDeserializer(delegate) {
        override fun newDelegatingInstance(newDelegatee: ValueDeserializer<*>): ValueDeserializer<*> =
            NestedContainerDeserializer(newDelegatee)

        override fun createContextual(ctxt: DeserializationContext, property: BeanProperty?): ValueDeserializer<*> {
            val contextualType = ctxt.contextualType
            val scopedProperty = if (contextualType == null || property == null) {
                property
            } else {
                scopeProperty(ctxt, property, contextualType)
            }
            return _delegatee.createContextual(ctxt, scopedProperty)
        }
    }

    private fun scopeProperty(ctxt: DeserializationContext, property: BeanProperty, type: JavaType): BeanProperty {
        if (property is ContainerProperty) {
            val projection = property.kotlinType.contentProjection(property.containerType)
            val kotlinType = projection?.type
            return if (type == property.containerType.contentType && kotlinType != null && projection.allowsNestedChecks) {
                ContainerProperty(property.original, type, kotlinType)
            } else {
                property.original
            }
        }

        if (type != property.type || type.contentType?.isContainerType != true) return property
        val member = property.member as? AnnotatedParameter ?: return property
        val kotlinType = cache.findKotlinParameter(member)?.type ?: return property
        if (!kotlinType.hasNestedContainer(type)) return property

        // Keep the same precedence as flat properties: explicit annotation, Kotlin inference, then defaults.
        if (property.metadata.contentNulls != null &&
            ctxt.annotationIntrospector.findSetterInfo(ctxt.config, member)?.nonDefaultContentNulls() != null
        ) {
            return property
        }
        return ContainerProperty(property, type, kotlinType)
    }

    // Jackson passes the same property when contextualizing both a container and its contents.
    // Keep the original property metadata available so inferred FAIL applies only at its own level.
    private class ContainerProperty(
        val original: BeanProperty,
        val containerType: JavaType,
        val kotlinType: KType,
    ) : BeanProperty by original {
        private val scopedMetadata = if (kotlinType.contentProjection(containerType)?.requiresStrictNullCheck == true) {
            original.metadata.withNulls(original.metadata.valueNulls, Nulls.FAIL)
        } else {
            original.metadata
        }

        override fun getMetadata() = scopedMetadata
    }
}
