package tools.jackson.module.kotlin

import tools.jackson.databind.BeanDescription
import tools.jackson.databind.JavaType
import tools.jackson.databind.cfg.MapperConfig
import tools.jackson.databind.introspect.AnnotatedClass
import tools.jackson.databind.introspect.ClassIntrospector

internal class KotlinAwareClassIntrospector(
    private val delegate: ClassIntrospector,
) : ClassIntrospector() {

    override fun forMapper(): ClassIntrospector = KotlinAwareClassIntrospector(delegate.forMapper())

    override fun forOperation(config: MapperConfig<*>): ClassIntrospector =
        KotlinAwareClassIntrospector(delegate.forOperation(config))

    override fun introspectClassAnnotations(type: JavaType): AnnotatedClass =
        delegate.introspectClassAnnotations(type).also(::fixKotlinMethodMapIfNeeded)

    override fun introspectDirectClassAnnotations(type: JavaType): AnnotatedClass =
        delegate.introspectDirectClassAnnotations(type).also(::fixKotlinMethodMapIfNeeded)

    override fun introspectForSerialization(type: JavaType, classDef: AnnotatedClass): BeanDescription =
        delegate.introspectForSerialization(type, classDef)

    override fun introspectForDeserialization(type: JavaType, classDef: AnnotatedClass): BeanDescription =
        delegate.introspectForDeserialization(type, classDef)

    override fun introspectForDeserializationWithBuilder(
        type: JavaType,
        valueTypeDesc: BeanDescription,
    ): BeanDescription = delegate.introspectForDeserializationWithBuilder(type, valueTypeDesc)

    override fun introspectForCreation(type: JavaType, classDef: AnnotatedClass): BeanDescription =
        delegate.introspectForCreation(type, classDef)

    private fun fixKotlinMethodMapIfNeeded(annotatedClass: AnnotatedClass) {
        KotlinMethodCollectorFix.fixMethodMap(annotatedClass)
    }
}
