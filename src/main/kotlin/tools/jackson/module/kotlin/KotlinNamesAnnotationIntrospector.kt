package tools.jackson.module.kotlin

import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.annotation.JsonSetter
import com.fasterxml.jackson.annotation.Nulls
import tools.jackson.databind.DeserializationFeature
import tools.jackson.databind.JacksonModule
import tools.jackson.databind.JavaType
import tools.jackson.databind.cfg.MapperConfig
import tools.jackson.databind.introspect.Annotated
import tools.jackson.databind.introspect.AnnotatedClass
import tools.jackson.databind.introspect.AnnotatedField
import tools.jackson.databind.introspect.AnnotatedMember
import tools.jackson.databind.introspect.AnnotatedMethod
import tools.jackson.databind.introspect.AnnotatedParameter
import tools.jackson.databind.introspect.NopAnnotationIntrospector
import tools.jackson.databind.introspect.PotentialCreator
import java.lang.reflect.Constructor
import java.lang.reflect.Field
import java.lang.reflect.Method
import java.util.Locale
import kotlin.collections.getOrNull
import kotlin.reflect.KClass
import kotlin.reflect.KFunction
import kotlin.reflect.KMutableProperty1
import kotlin.reflect.KParameter
import kotlin.reflect.KProperty1
import kotlin.reflect.KType
import kotlin.reflect.full.createType
import kotlin.reflect.full.declaredMemberProperties
import kotlin.reflect.full.hasAnnotation
import kotlin.reflect.full.memberProperties
import kotlin.reflect.full.primaryConstructor
import kotlin.reflect.full.valueParameters
import kotlin.reflect.jvm.javaGetter
import kotlin.reflect.jvm.javaSetter
import kotlin.reflect.jvm.javaType
import kotlin.reflect.jvm.kotlinProperty

internal class KotlinNamesAnnotationIntrospector(
    private val context: JacksonModule.SetupContext,
    private val cache: ReflectionCache,
    private val nullToEmptyCollection: Boolean,
    private val nullToEmptyMap: Boolean,
    private val nullIsSameAsDefault: Boolean,
    private val strictNullChecks: Boolean,
    private val kotlinPropertyNameAsImplicitName: Boolean
) : NopAnnotationIntrospector() {
    private fun KType.isRequired(): Boolean = !isMarkedNullable

    // Since Kotlin's property has the same Type for each field, getter, and setter,
    // nullability can be determined from the returnType of KProperty.
    private fun KProperty1<*, *>.isRequiredByNullability() = returnType.isRequired()

    private fun KParameter.isRequired(): Boolean {
        val paramType = type
        val isPrimitive = when (val javaType = paramType.javaType) {
            is Class<*> -> javaType.isPrimitive
            else -> false
        }

        return !paramType.isMarkedNullable && !isOptional && !isVararg &&
                !(isPrimitive && !context.isEnabled(DeserializationFeature.FAIL_ON_NULL_FOR_PRIMITIVES))
    }

    private fun AnnotatedField.hasRequiredMarker(): Boolean? {
        val field = member as Field
        return field.kotlinProperty?.returnType?.isRequired()
    }

    private fun KFunction<*>.isGetterLike(): Boolean = parameters.size == 1
    private fun KFunction<*>.isSetterLike(): Boolean = parameters.size == 2 && returnType == UNIT_TYPE

    private fun AnnotatedMethod.getRequiredMarkerFromCorrespondingAccessor(): Boolean? {
        member.declaringClass.kotlin.declaredMemberProperties.forEach { kProperty ->
            if (kProperty.javaGetter == this.member || (kProperty as? KMutableProperty1)?.javaSetter == this.member) {
                return kProperty.isRequiredByNullability()
            }
        }
        return null
    }

    // Is the member method a regular method of the data class or
    private fun Method.getRequiredMarkerFromAccessorLikeMethod(): Boolean? = cache.kotlinFromJava(this)?.let { func ->
        when {
            func.isGetterLike() -> func.returnType.isRequired()
            // If nullToEmpty could be supported for setters,
            // a branch similar to AnnotatedParameter.hasRequiredMarker should be added.
            func.isSetterLike() -> func.valueParameters[0].isRequired()
            else -> null
        }
    }

    // This could be a setter or a getter of a class property or
    // a setter-like/getter-like method.
    private fun AnnotatedMethod.hasRequiredMarker(): Boolean? = this.getRequiredMarkerFromCorrespondingAccessor()
        ?: this.member.getRequiredMarkerFromAccessorLikeMethod()

    // TODO: implement nullIsSameAsDefault flag, which represents when TRUE that if something has a default value, it can be passed a null to default it
    //       this likely impacts this class to be accurate about what COULD be considered required
    private fun AnnotatedParameter.hasRequiredMarker(): Boolean? = when {
        nullToEmptyCollection && type.isCollectionLikeType -> false
        nullToEmptyMap && type.isMapLikeType -> false
        else -> cache.findKotlinParameter(this)?.isRequired()
    }

    override fun hasRequiredMarker(
        cfg: MapperConfig<*>,
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

    private fun getterNameFromJava(member: AnnotatedMethod): String? {
        val name = member.name

        // The reason for truncating after `-` is to truncate the random suffix
        // given after the value class accessor name.
        return when {
            name.startsWith("get") -> name.takeIf { it.contains("-") }?.let { _ ->
                name.substringAfter("get")
                    .replaceFirstChar { it.lowercase(Locale.getDefault()) }
                    .substringBefore('-')
            }
            // since 2.15: support Kotlin's way of handling "isXxx" backed properties where
            // logical property name needs to remain "isXxx" and not become "xxx" as with Java Beans
            // (see https://kotlinlang.org/docs/reference/java-to-kotlin-interop.html and
            //  https://github.com/FasterXML/jackson-databind/issues/2527 and
            //  https://github.com/FasterXML/jackson-module-kotlin/issues/340
            //  for details)
            name.startsWith("is") -> if (name.contains("-")) name.substringAfter("-") else name
            else -> null
        }
    }

    private fun getterNameFromKotlin(member: AnnotatedMethod): String? {
        val getterName = member.member.name

        return member.member.declaringClass.takeIf { it.isKotlinClass() }?.let { clazz ->
            // For edge case, methods must be compared by name, not directly.
            clazz.kotlin.memberProperties.find { it.javaGetter?.name == getterName }?.name
        }
    }

    // since 2.4
    override fun findImplicitPropertyName(config: MapperConfig<*>, member: AnnotatedMember): String? {
        if (!member.declaringClass.isKotlinClass()) return null

        return when (member) {
            is AnnotatedMethod -> if (member.parameterCount == 0) {
                if (kotlinPropertyNameAsImplicitName) {
                    // Fall back to default if it is a getter-like function
                    getterNameFromKotlin(member) ?: getterNameFromJava(member)
                } else getterNameFromJava(member)
            } else null
            is AnnotatedParameter -> findKotlinParameterName(member)
            else -> null
        }
    }

    override fun refineDeserializationType(config: MapperConfig<*>, a: Annotated, baseType: JavaType): JavaType =
        findKotlinParameter(a)?.let { param ->
            val rawType = a.rawType
            (param.type.classifier as? KClass<*>)
                ?.java
                ?.takeIf { it.isUnboxableValueClass() && it != rawType }
                ?.let { config.constructType(it) }
        } ?: baseType

    override fun findSetterInfo(config: MapperConfig<*>, ann: Annotated): JsonSetter.Value = ann.takeIf { strictNullChecks }
        ?.let { _ ->
            findKotlinParameter(ann)?.let { param ->
                if (param.requireStrictNullCheck(ann.type)) {
                    JsonSetter.Value.forContentNulls(Nulls.FAIL)
                } else {
                    null
                }
            }
        }
        ?: super.findSetterInfo(config, ann)

    override fun findPreferredCreator(
        config: MapperConfig<*>,
        valueClass: AnnotatedClass,
        declaredConstructors: List<PotentialCreator>,
        declaredFactories: List<PotentialCreator>
    ): PotentialCreator? {
        val kClass = valueClass.creatableKotlinClass() ?: return null

        val defaultCreator = kClass.primarilyConstructor()
            ?.takeIf { ctor ->
                val propertyNames = kClass.memberProperties.map { it.name }.toSet()
                ctor.isPossibleCreator(propertyNames)
            }
            ?: return null

        return declaredConstructors.find {
            // To avoid problems with constructors that include `value class` as an argument,
            // convert to `KFunction` and compare
            cache.kotlinFromJava(it.creator().annotated as Constructor<*>) == defaultCreator
        }
    }

    private fun findKotlinParameterName(param: AnnotatedParameter): String? = cache.findKotlinParameter(param)?.name

    private fun findKotlinParameter(param: Annotated) = (param as? AnnotatedParameter)
        ?.let { cache.findKotlinParameter(it) }

    companion object {
        val UNIT_TYPE: KType by lazy { Unit::class.createType() }
    }
}

private fun KParameter.markedNonNullAt(index: Int) = type.arguments.getOrNull(index)?.type?.isMarkedNullable == false

private fun KParameter.requireStrictNullCheck(type: JavaType): Boolean =
    ((type.isArrayType || type.isCollectionLikeType) && this.markedNonNullAt(0)) ||
            (type.isMapLikeType && this.markedNonNullAt(1))


// If it is not a Kotlin class or an Enum, Creator is not used
private fun AnnotatedClass.creatableKotlinClass(): KClass<*>? = annotated
    .takeIf { it.isKotlinClass() && !it.isEnum }
    ?.kotlin

// By default, the primary constructor or the only publicly available constructor may be used
private fun KClass<*>.primarilyConstructor() = primaryConstructor ?: constructors.singleOrNull()

private fun KFunction<*>.isPossibleCreator(propertyNames: Set<String>): Boolean = 0 < parameters.size
    && !isPossibleSingleString(propertyNames)
    && parameters.none { it.name == null }

private fun KFunction<*>.isPossibleSingleString(propertyNames: Set<String>): Boolean = parameters.singleOrNull()?.let {
    it.name !in propertyNames
        && it.type.javaType == String::class.java
        && !it.hasAnnotation<JsonProperty>()
} == true
