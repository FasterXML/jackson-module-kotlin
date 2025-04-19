package tools.jackson.module.kotlin

import tools.jackson.databind.cfg.MapperConfig
import tools.jackson.databind.introspect.Annotated
import tools.jackson.databind.introspect.AnnotatedClass
import tools.jackson.databind.introspect.AnnotatedMethod
import tools.jackson.databind.introspect.NopAnnotationIntrospector
import tools.jackson.databind.jsontype.NamedType
import tools.jackson.databind.util.Converter
import kotlin.time.Duration

internal class KotlinAnnotationIntrospector(
    private val cache: ReflectionCache,
    private val useJavaDurationConversion: Boolean,
) : NopAnnotationIntrospector() {

    override fun findSerializationConverter(config: MapperConfig<*>?, a: Annotated): Converter<*, *>? = when (a) {
        // Find a converter to handle the case where the getter returns an unboxed value from the value class.
        is AnnotatedMethod -> a.findValueClassReturnType()?.let {
            if (useJavaDurationConversion && it == Duration::class) {
                if (a.rawReturnType == Duration::class.java)
                    KotlinToJavaDurationConverter
                else
                    KotlinDurationValueToJavaDurationConverter
            } else {
                cache.getValueClassBoxConverter(a.rawReturnType, it)
            }
        }
        is AnnotatedClass -> lookupKotlinTypeConverter(a)
        else -> null
    }

    private fun lookupKotlinTypeConverter(a: AnnotatedClass) = when {
        Sequence::class.java.isAssignableFrom(a.rawType) -> SequenceToIteratorConverter(a.type)
        Duration::class.java == a.rawType -> KotlinToJavaDurationConverter.takeIf { useJavaDurationConversion }
        else -> null
    }

    // Perform proper serialization even if the value wrapped by the value class is null.
    // If value is a non-null object type, it must not be reboxing.
    override fun findNullSerializer(config: MapperConfig<*>?, am: Annotated) = (am as? AnnotatedMethod)
        ?.findValueClassReturnType()
        ?.takeIf { it.wrapsNullable() }
        ?.let { cache.getValueClassBoxConverter(am.rawReturnType, it).delegatingSerializer }

    /**
     * Subclasses can be detected automatically for sealed classes, since all possible subclasses are known
     * at compile-time to Kotlin. This makes [com.fasterxml.jackson.annotation.JsonSubTypes] redundant.
     */
    override fun findSubtypes(cfg: MapperConfig<*>, a: Annotated): MutableList<NamedType>? = a.rawType
        .takeIf { it.isKotlinClass() }
        ?.let { rawType ->
            rawType.kotlin.sealedSubclasses
                .map { NamedType(it.java) }
                .toMutableList()
                .ifEmpty { null }
        }

    private fun AnnotatedMethod.findValueClassReturnType() = cache.findValueClassReturnType(this)
}
