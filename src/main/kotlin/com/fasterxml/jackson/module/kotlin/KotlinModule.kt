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
import com.fasterxml.jackson.annotation.JsonCreator

public class KotlinModule(val requireJsonCreatorAnnotation: Boolean = true) : SimpleModule(PackageVersion.VERSION) {
    class object {
        private val serialVersionUID = 1L;
    }

    override public fun setupModule(context: SetupContext?) {
        super.setupModule(context)
        context!!.appendAnnotationIntrospector(KotlinNamesAnnotationIntrospector(requireJsonCreatorAnnotation))
    }
}

internal class KotlinNamesAnnotationIntrospector(val requireJsonCreatorAnnotation: Boolean) : NopAnnotationIntrospector() {
    class object {
        val impliedClasses = setOf("kotlin.Pair",
                "kotlin.Triple")
    }
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
            return impliedClasses.contains(member.getDeclaringClass()!!.getName())
        } else {
            return false
        }
    }

    protected fun findKotlinParameterName(param: AnnotatedParameter): String? {
        if (param.getDeclaringClass()!!.getAnnotation(javaClass<KotlinClass>()) != null) {
            if (!requireJsonCreatorAnnotation ||
                    param.getOwner()!!.hasAnnotation(javaClass<JsonCreator>()) ||
                    impliedClasses.contains(param.getDeclaringClass()!!.getName())) {
                // TODO: this will change in the near future to full runtime type information
                return param.getAnnotation(javaClass<JetValueParameter>())?.name()
            }
        }
        return null
    }
}
