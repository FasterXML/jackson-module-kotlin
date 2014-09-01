package com.fasterxml.jackson.module.kotlin

import com.fasterxml.jackson.databind.introspect.AnnotatedMember
import com.fasterxml.jackson.databind.introspect.Annotated
import com.fasterxml.jackson.databind.PropertyName
import com.fasterxml.jackson.databind.introspect.AnnotatedParameter
import com.fasterxml.jackson.databind.module.SimpleModule
import com.fasterxml.jackson.databind.Module.SetupContext
import com.fasterxml.jackson.databind.introspect.NopAnnotationIntrospector
import jet.runtime.typeinfo.JetValueParameter
import kotlin.jvm.internal.KotlinClass
import java.util.HashSet

public class KotlinModule() : SimpleModule(PackageVersion.VERSION) {
    class object {
        private val serialVersionUID = 1L;
    }
    val requireJsonCreatorAnnotation: Boolean = false

    val impliedClasses = HashSet<Class<*>>(setOf(
            javaClass<Pair<*, *>>(),
            javaClass<Triple<*, *, *>>()
    ))

    override public fun setupModule(context: SetupContext?) {
        super.setupModule(context!!)

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
    override public fun findNameForDeserialization(annotated: Annotated?): PropertyName? {
        // This should not do introspection here, only for explicit naming by annotations
        return null
    }

    // since 2.4
    override public fun findImplicitPropertyName(member: AnnotatedMember?): String? {
        if (member is AnnotatedParameter) {
            return findKotlinParameterName(member)
        }
        return null
    }

    override public fun hasCreatorAnnotation(member: Annotated?): Boolean {
        if (member is AnnotatedParameter) {
            // pretend some built-in Kotlin classes have the JsonCreator annotation
            // return module.impliedClasses.contains(member.getDeclaringClass()!!)
            return false
        } else {
            return false
        }
    }

    protected fun findKotlinParameterName(param: AnnotatedParameter): String? {
        if (param.getDeclaringClass()!!.getAnnotation(javaClass<KotlinClass>()) != null) {
            // TODO: this will change in the near future to full runtime type information
            return param.getAnnotation(javaClass<JetValueParameter>())?.name()
        }
        return null
    }

}
