package com.fasterxml.jackson.module.kotlin

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.databind.Module.SetupContext
import com.fasterxml.jackson.databind.introspect.*
import com.fasterxml.jackson.databind.module.SimpleModule
import java.lang.reflect.Constructor
import java.lang.reflect.Method
import java.util.HashSet
import kotlin.jvm.internal.KotlinClass
import kotlin.reflect.*
import kotlin.reflect.jvm.kotlinFunction

public class KotlinModule() : SimpleModule(PackageVersion.VERSION) {
    companion object {
        private val serialVersionUID = 1L;
    }

    val requireJsonCreatorAnnotation: Boolean = false

    val impliedClasses = HashSet<Class<*>>(setOf(
            Pair::class.java,
            Triple::class.java
    ))

    override public fun setupModule(context: SetupContext) {
        super.setupModule(context)

        fun addMixin(clazz: Class<*>, mixin: Class<*>) {
            impliedClasses.add(clazz)
            context.setMixInAnnotations(clazz, mixin)
        }

        context.appendAnnotationIntrospector(KotlinNamesAnnotationIntrospector(this))

        // ranges
        addMixin(IntRange::class.java, RangeMixin::class.java)
        addMixin(DoubleRange::class.java, RangeMixin::class.java)
        addMixin(CharRange::class.java, RangeMixin::class.java)
        addMixin(ByteRange::class.java, RangeMixin::class.java)
        addMixin(ShortRange::class.java, RangeMixin::class.java)
        addMixin(LongRange::class.java, RangeMixin::class.java)
        addMixin(FloatRange::class.java, RangeMixin::class.java)
    }
}


internal class KotlinNamesAnnotationIntrospector(val module: KotlinModule) : NopAnnotationIntrospector() {
    /*
    override public fun findNameForDeserialization(annotated: Annotated?): PropertyName? {
        // This should not do introspection here, only for explicit naming by annotations
        return null
    }
    */

    // since 2.4
    override public fun findImplicitPropertyName(member: AnnotatedMember): String? {
        if (member is AnnotatedParameter) {
            return findKotlinParameterName(member)
        }
        return null
    }

    private val jsonCreator = JsonCreator::class.java

    @Suppress("UNCHECKED_CAST")
    override public fun hasCreatorAnnotation(member: Annotated): Boolean {
        // don't add a JsonCreator to any constructor if one is declared already

        if (member is AnnotatedConstructor) {
            // if has parameters, is a Kotlin class, and the parameters all have parameter annotations, then pretend we have a JsonCreator
            if (member.getParameterCount() > 0 && member.getDeclaringClass().getAnnotation(KotlinClass::class.java) != null) {
                val kClass = (member.getDeclaringClass() as Class<Any>).kotlin
                val kConstructor = (member.getAnnotated() as Constructor<Any>).kotlinFunction

                if (kConstructor != null) {
                    val isPrimaryConstructor = kClass.primaryConstructor == kConstructor
                    val anyConstructorHasJsonCreator = kClass.constructors.any { it.annotations.any { it.annotationType() == jsonCreator } } // member.getDeclaringClass().getConstructors().any { it.getAnnotation() != null }
                    val anyStaticHasJsonCreator = member.getContextClass().getStaticMethods().any() { it.getAnnotation(jsonCreator) != null }
                    val areAllParametersValid = kConstructor.parameters.size() == kConstructor.parameters.count { it.name != null }
                    val implyCreatorAnnotation = isPrimaryConstructor && !(anyConstructorHasJsonCreator || anyStaticHasJsonCreator) && areAllParametersValid

                    return implyCreatorAnnotation
                }
            }
        }
        return false
    }

    @Suppress("UNCHECKED_CAST")
    protected fun findKotlinParameterName(param: AnnotatedParameter): String? {
        if (param.getDeclaringClass().getAnnotation(KotlinClass::class.java) != null) {
            val member = param.getOwner().getMember()
            val name = if (member is Constructor<*>) {
                val ctor = (member as Constructor<Any>)
                val ctorParmCount = ctor.parameterTypes.size()
                val ktorParmCount = ctor.kotlinFunction?.parameters?.size() ?: 0
                if (ktorParmCount > 0 && ktorParmCount == ctorParmCount) {
                    ctor.kotlinFunction?.parameters?.get(param.index)?.name
                } else {
                    null
                }
            } else if (member is Method) {
                try {
                    val temp = member.kotlinFunction

                    val firstParamKind = temp?.parameters?.firstOrNull()?.kind
                    val idx = if (firstParamKind != KParameter.Kind.VALUE) param.index + 1 else param.index
                    val parmCount = temp?.parameters?.size() ?: 0
                    if (parmCount > idx) {
                        temp?.parameters?.get(idx)?.name
                    }
                    else {
                        null
                    }
                }
                catch (ex: KotlinReflectionInternalError) {
                    null
                }
            } else {
                null
            }
            return name
        }
        return null
    }

}
