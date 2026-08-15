package tools.jackson.module.kotlin

import tools.jackson.databind.BeanDescription
import tools.jackson.databind.JavaType
import tools.jackson.databind.cfg.MapperConfig
import tools.jackson.databind.introspect.AnnotatedClass
import tools.jackson.databind.introspect.BasicClassIntrospector
import tools.jackson.databind.introspect.ClassIntrospector

internal class KotlinAwareClassIntrospector(
    private val delegate: ClassIntrospector,
) : ClassIntrospector() {

    override fun forMapper(): ClassIntrospector = KotlinAwareClassIntrospector(delegate.forMapper())

    override fun forOperation(config: MapperConfig<*>): ClassIntrospector {
        val operationDelegate = delegate.forOperation(config)
        return when {
            operationDelegate is KotlinAwareBasicClassIntrospector -> operationDelegate
            operationDelegate is BasicClassIntrospector -> KotlinAwareBasicClassIntrospector(config)
            else -> KotlinAwareClassIntrospector(operationDelegate)
        }
    }

    override fun introspectClassAnnotations(type: JavaType): AnnotatedClass =
        delegate.introspectClassAnnotations(type)

    override fun introspectDirectClassAnnotations(type: JavaType): AnnotatedClass =
        delegate.introspectDirectClassAnnotations(type)

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
}
