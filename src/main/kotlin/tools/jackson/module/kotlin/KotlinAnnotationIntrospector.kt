package tools.jackson.module.kotlin

import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.annotation.OptBoolean
import tools.jackson.databind.DeserializationFeature
import tools.jackson.databind.JacksonModule
import tools.jackson.databind.cfg.MapperConfig
import tools.jackson.databind.introspect.Annotated
import tools.jackson.databind.introspect.AnnotatedClass
import tools.jackson.databind.introspect.AnnotatedField
import tools.jackson.databind.introspect.AnnotatedMember
import tools.jackson.databind.introspect.AnnotatedMethod
import tools.jackson.databind.introspect.AnnotatedParameter
import tools.jackson.databind.introspect.NopAnnotationIntrospector
import tools.jackson.databind.jsontype.NamedType
import tools.jackson.databind.util.Converter
import java.lang.reflect.AccessibleObject
import java.lang.reflect.Field
import java.lang.reflect.Method
import kotlin.reflect.KFunction
import kotlin.reflect.KMutableProperty1
import kotlin.reflect.KParameter
import kotlin.reflect.KProperty1
import kotlin.reflect.KType
import kotlin.reflect.full.createType
import kotlin.reflect.full.declaredMemberProperties
import kotlin.reflect.full.valueParameters
import kotlin.reflect.jvm.javaGetter
import kotlin.reflect.jvm.javaSetter
import kotlin.reflect.jvm.javaType
import kotlin.reflect.jvm.kotlinProperty
import kotlin.time.Duration

internal class KotlinAnnotationIntrospector(
    private val context: JacksonModule.SetupContext,
    private val cache: ReflectionCache,
    private val nullToEmptyCollection: Boolean,
    private val nullToEmptyMap: Boolean,
    private val nullIsSameAsDefault: Boolean,
    private val useJavaDurationConversion: Boolean,
) : NopAnnotationIntrospector() {

    // TODO: implement nullIsSameAsDefault flag, which represents when TRUE that if something has a default value, it can be passed a null to default it
    //       this likely impacts this class to be accurate about what COULD be considered required

    // If a new isRequired is explicitly specified or the old required is true, those values take precedence.
    // In other cases, override is done by KotlinModule.
    private fun JsonProperty.forceRequiredByAnnotation(): Boolean? = when {
        isRequired != OptBoolean.DEFAULT -> isRequired.asBoolean()
        required -> true
        else -> null
    }

    private fun AccessibleObject.forceRequiredByAnnotation(): Boolean? =
        getAnnotation(JsonProperty::class.java)?.forceRequiredByAnnotation()

    override fun hasRequiredMarker(
        cfg : MapperConfig<*>,
        m: AnnotatedMember
    ): Boolean? = m.takeIf { it.member.declaringClass.isKotlinClass() }?.let { _ ->
        cache.javaMemberIsRequired(m) {
            try {
                when (m) {
                    is AnnotatedField -> m.hasRequiredMarker()
                    is AnnotatedMethod -> m.hasRequiredMarker()
                    is AnnotatedParameter -> m.hasRequiredMarker()
                    else -> null
                }
            } catch (_: UnsupportedOperationException) {
                null
            }
        }
    }

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
    override fun findSubtypes(cfg : MapperConfig<*>, a: Annotated): MutableList<NamedType>? = a.rawType
        .takeIf { it.isKotlinClass() }
        ?.let { rawType ->
            rawType.kotlin.sealedSubclasses
                .map { NamedType(it.java) }
                .toMutableList()
                .ifEmpty { null }
        }

    private fun AnnotatedField.hasRequiredMarker(): Boolean? {
        val field = member as Field
        return field.forceRequiredByAnnotation()
            ?: field.kotlinProperty?.returnType?.isRequired()
    }

    // Since Kotlin's property has the same Type for each field, getter, and setter,
    // nullability can be determined from the returnType of KProperty.
    private fun KProperty1<*, *>.isRequiredByNullability() = returnType.isRequired()

    // This could be a setter or a getter of a class property or
    // a setter-like/getter-like method.
    private fun AnnotatedMethod.hasRequiredMarker(): Boolean? = this.getRequiredMarkerFromCorrespondingAccessor()
        ?: this.member.getRequiredMarkerFromAccessorLikeMethod()

    private fun AnnotatedMethod.getRequiredMarkerFromCorrespondingAccessor(): Boolean? {
        member.declaringClass.kotlin.declaredMemberProperties.forEach { kProperty ->
            if (kProperty.javaGetter == this.member || (kProperty as? KMutableProperty1)?.javaSetter == this.member) {
                return member.forceRequiredByAnnotation() ?: kProperty.isRequiredByNullability()
            }
        }
        return null
    }

    // Is the member method a regular method of the data class or
    private fun Method.getRequiredMarkerFromAccessorLikeMethod(): Boolean? = cache.kotlinFromJava(this)?.let { func ->
        forceRequiredByAnnotation() ?: when {
            func.isGetterLike() -> func.returnType.isRequired()
            // If nullToEmpty could be supported for setters,
            // a branch similar to AnnotatedParameter.hasRequiredMarker should be added.
            func.isSetterLike() -> func.valueParameters[0].isRequired()
            else -> null
        }
    }

    private fun KFunction<*>.isGetterLike(): Boolean = parameters.size == 1
    private fun KFunction<*>.isSetterLike(): Boolean = parameters.size == 2 && returnType == UNIT_TYPE

    private fun AnnotatedParameter.hasRequiredMarker(): Boolean? = getAnnotation(JsonProperty::class.java)
        ?.forceRequiredByAnnotation()
        ?: run {
            when {
                nullToEmptyCollection && type.isCollectionLikeType -> false
                nullToEmptyMap && type.isMapLikeType -> false
                else -> cache.findKotlinParameter(this)?.isRequired()
            }
        }

    private fun AnnotatedMethod.findValueClassReturnType() = cache.findValueClassReturnType(this)

    private fun KParameter.isRequired(): Boolean {
        val paramType = type
        val isPrimitive = when (val javaType = paramType.javaType) {
            is Class<*> -> javaType.isPrimitive
            else -> false
        }

        return !paramType.isMarkedNullable && !isOptional && !isVararg &&
                !(isPrimitive && !context.isEnabled(DeserializationFeature.FAIL_ON_NULL_FOR_PRIMITIVES))
    }

    private fun KType.isRequired(): Boolean = !isMarkedNullable

    companion object {
        val UNIT_TYPE: KType by lazy { Unit::class.createType() }
    }
}
