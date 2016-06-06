package com.fasterxml.jackson.module.kotlin

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.databind.introspect.*
import com.fasterxml.jackson.databind.module.SimpleModule
import java.lang.reflect.Constructor
import java.lang.reflect.Method
import java.lang.reflect.Modifier
import java.util.HashSet
import kotlin.reflect.*
import kotlin.reflect.jvm.kotlinFunction

private val metadataFqName = "kotlin.Metadata"

fun Class<*>.isKotlinClass(): Boolean {
    return this.declaredAnnotations.singleOrNull { it.annotationClass.java.name == metadataFqName } != null
}

class KotlinModule() : SimpleModule(PackageVersion.VERSION) {
    companion object {
        private val serialVersionUID = 1L;
    }

    val requireJsonCreatorAnnotation: Boolean = false

    val impliedClasses = HashSet<Class<*>>(setOf(
            Pair::class.java,
            Triple::class.java
    ))

    override fun setupModule(context: SetupContext) {
        super.setupModule(context)

        context.addValueInstantiators(KotlinInstantiators());

        fun addMixin(clazz: Class<*>, mixin: Class<*>) {
            impliedClasses.add(clazz)
            context.setMixInAnnotations(clazz, mixin)
        }

        context.appendAnnotationIntrospector(KotlinNamesAnnotationIntrospector(this))

        // ranges
        addMixin(IntRange::class.java, ClosedRangeMixin::class.java)
        addMixin(CharRange::class.java, ClosedRangeMixin::class.java)
        addMixin(LongRange::class.java, ClosedRangeMixin::class.java)
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
    override fun findImplicitPropertyName(member: AnnotatedMember): String? {
        if (member is AnnotatedParameter) {
            return findKotlinParameterName(member)
        }
        return null
    }

    @Suppress("UNCHECKED_CAST")
    override fun hasCreatorAnnotation(member: Annotated): Boolean {
        // don't add a JsonCreator to any constructor if one is declared already

        if (member is AnnotatedConstructor && !member.declaringClass.isEnum) {
            // if has parameters, is a Kotlin class, and the parameters all have parameter annotations, then pretend we have a JsonCreator
            if (member.getParameterCount() > 0 && member.getDeclaringClass().isKotlinClass()) {
                val kClass = (member.getDeclaringClass() as Class<Any>).kotlin
                val kConstructor = (member.getAnnotated() as Constructor<Any>).kotlinFunction

                if (kConstructor != null) {
                    val isPrimaryConstructor = kClass.primaryConstructor == kConstructor ||
                            (kClass.primaryConstructor == null && kClass.constructors.size == 1)
                    val anyConstructorHasJsonCreator = kClass.constructors.any { it.annotations.any { it.annotationClass.java == JsonCreator::class.java } } // member.getDeclaringClass().getConstructors().any { it.getAnnotation() != null }

                    val anyCompanionMethodIsJsonCreator = member.type.rawClass.kotlin.companionObject?.declaredFunctions?.any {
                        it.annotations.any { it.annotationClass.java == JvmStatic::class.java } &&
                                it.annotations.any { it.annotationClass.java == JsonCreator::class.java }
                    } ?: false
                    val anyStaticMethodIsJsonCreator = member.type.rawClass.declaredMethods.any {
                        val isStatic = Modifier.isStatic(it.modifiers)
                        val isCreator = it.declaredAnnotations.any { it.annotationClass.java == JsonCreator::class.java }
                        isStatic && isCreator
                    }

                    // TODO:  should we do this check or not?  It could cause failures if we miss another way a property could be set
                    // val requiredProperties = kClass.declaredMemberProperties.filter {!it.returnType.isMarkedNullable }.map { it.name }.toSet()
                    // val areAllRequiredParametersInConstructor = kConstructor.parameters.all { requiredProperties.contains(it.name) }

                    val areAllParametersValid = kConstructor.parameters.size == kConstructor.parameters.count { it.name != null }

                    val isSingleStringConstructor = kConstructor.parameters.size == 1 &&
                                                    kConstructor.parameters[0].type == String::class.defaultType &&
                                                    kClass.declaredMemberProperties.none {
                                                        it.name == kConstructor.parameters[0].name && it.returnType == kConstructor.parameters[0].type
                                                    }
                    val implyCreatorAnnotation = isPrimaryConstructor
                            && !(anyConstructorHasJsonCreator || anyCompanionMethodIsJsonCreator || anyStaticMethodIsJsonCreator)
                            && areAllParametersValid
                            && !isSingleStringConstructor

                    return implyCreatorAnnotation
                }
            }
        }
        return false
    }

    @Suppress("UNCHECKED_CAST")
    protected fun findKotlinParameterName(param: AnnotatedParameter): String? {
        if (param.getDeclaringClass().isKotlinClass()) {
            val member = param.getOwner().getMember()
            val name = if (member is Constructor<*>) {
                val ctor = (member as Constructor<Any>)
                val ctorParmCount = ctor.parameterTypes.size
                val ktorParmCount = ctor.kotlinFunction?.parameters?.size ?: 0
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
                    val parmCount = temp?.parameters?.size ?: 0
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
