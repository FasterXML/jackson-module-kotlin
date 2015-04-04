package com.fasterxml.jackson.module.kotlin

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.databind.Module.SetupContext
import com.fasterxml.jackson.databind.introspect.*
import com.fasterxml.jackson.databind.module.SimpleModule
import jet.runtime.typeinfo.JetValueParameter
import java.util.HashSet
import kotlin.jvm.internal.KotlinClass

public class KotlinModule() : SimpleModule(PackageVersion.VERSION) {
    companion object {
        private val serialVersionUID = 1L;
    }
    val requireJsonCreatorAnnotation: Boolean = false

    val impliedClasses = HashSet<Class<*>>(setOf(
            javaClass<Pair<*, *>>(),
            javaClass<Triple<*, *, *>>()
    ))

    override public fun setupModule(context: SetupContext) {
        super.setupModule(context)

        fun addMixin(clazz: Class<*>, mixin: Class<*>) {
            impliedClasses.add(clazz)
            context.setMixInAnnotations(clazz, mixin)
        }

        context.appendAnnotationIntrospector(KotlinNamesAnnotationIntrospector(this))

        // ranges
        addMixin(javaClass<IntRange>(), javaClass<RangeMixin<*>>())
        addMixin(javaClass<DoubleRange>(), javaClass<RangeMixin<*>>())
        addMixin(javaClass<CharRange>(), javaClass<RangeMixin<*>>())
        addMixin(javaClass<ByteRange>(), javaClass<RangeMixin<*>>())
        addMixin(javaClass<ShortRange>(), javaClass<RangeMixin<*>>())
        addMixin(javaClass<LongRange>(), javaClass<RangeMixin<*>>())
        addMixin(javaClass<FloatRange>(), javaClass<RangeMixin<*>>())
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

    override public fun hasCreatorAnnotation(member: Annotated): Boolean {
        // don't add a JsonCreator to any constructor if one is declared already

        if (member is AnnotatedConstructor) {
            // if has parameters, is a Kotlin class, and the parameters all have parameter annotations, then pretend we have a JsonCreator
            if (member.getParameterCount() > 0 &&  member.getDeclaringClass().getAnnotation(javaClass<KotlinClass>()) != null) {
                val anyConstructorHasJsonCreator = member.getDeclaringClass().getConstructors().any { it.getAnnotation(javaClass<JsonCreator>()) != null }
                val anyStaticHasJsonCreator = member.getContextClass().getStaticMethods().any() { it.getAnnotation(javaClass<JsonCreator>()) != null }
                val areAllParametersValid = (member.getAnnotated().getParameterAnnotations().all { it.any { it.annotationType() ==  javaClass<JetValueParameter>() }})
                return !(anyConstructorHasJsonCreator || anyStaticHasJsonCreator) && areAllParametersValid
            }
            else {
                return false
            }
        } else {
            return false
        }
    }

    protected fun findKotlinParameterName(param: AnnotatedParameter): String? {
        if (param.getDeclaringClass().getAnnotation(javaClass<KotlinClass>()) != null) {
            // TODO: this will change in the near future to full runtime type information
            return param.getAnnotation(javaClass<JetValueParameter>())?.name()
        }
        return null
    }

}
