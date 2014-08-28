package com.fasterxml.jackson.module.kotlin

import com.fasterxml.jackson.databind.introspect.AnnotatedMember
import com.fasterxml.jackson.databind.introspect.Annotated
import com.fasterxml.jackson.databind.PropertyName
import com.fasterxml.jackson.databind.introspect.AnnotatedParameter
import com.fasterxml.jackson.databind.introspect.AnnotatedConstructor
import com.fasterxml.jackson.databind.introspect.AnnotatedMethod
import com.fasterxml.jackson.databind.module.SimpleModule
import com.fasterxml.jackson.databind.Module.SetupContext
import com.fasterxml.jackson.databind.introspect.NopAnnotationIntrospector
import jet.runtime.typeinfo.JetValueParameter
import kotlin.jvm.internal.KotlinClass
import com.fasterxml.jackson.annotation.JsonCreator

public class KotlinModule : SimpleModule(PackageVersion.VERSION) {
    class object {
        private val serialVersionUID = 1L;
    }

    override public fun setupModule(context: SetupContext?) {
        super.setupModule(context)
        context!!.appendAnnotationIntrospector(KotlinNamesAnnotationIntrospector())
    }
}

internal class KotlinNamesAnnotationIntrospector : NopAnnotationIntrospector() {
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

    override public fun hasCreatorAnnotation(member: Annotated?): Boolean{
        // TODO: possibly we can make assumption about data class in the future to pretend we have this annotation
        return super.hasCreatorAnnotation(member)
    }

    protected fun findKotlinParameterName(param: AnnotatedParameter): String? {
        if (param.getDeclaringClass()!!.getAnnotation(javaClass<KotlinClass>()) != null &&
             param.getOwner()!!.hasAnnotation(javaClass<JsonCreator>())) {
            // TODO: this will change in the near future to full runtime type information
            return param.getAnnotation(javaClass<JetValueParameter>())?.name()
        }
        return null
    }
}
