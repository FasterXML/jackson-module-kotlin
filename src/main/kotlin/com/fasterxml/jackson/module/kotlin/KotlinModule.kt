package com.fasterxml.jackson.module.kotlin

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.databind.Module.SetupContext
import com.fasterxml.jackson.databind.introspect.*
import com.fasterxml.jackson.databind.module.SimpleModule
import jet.runtime.typeinfo.JetValueParameter
import java.lang.reflect.Constructor
import java.lang.reflect.Method
import java.util.HashSet
import kotlin.jvm.internal.KotlinClass
import kotlin.platform.platformStatic
import kotlin.reflect.declaredFunctions
import kotlin.reflect.functions
import kotlin.reflect.jvm.internal.KClassImpl
import kotlin.reflect.jvm.internal.impl.descriptors.ClassDescriptor
import kotlin.reflect.jvm.internal.impl.types.TypeSubstitution
import kotlin.reflect.jvm.internal.impl.incremental.components.NoLookupLocation
import kotlin.reflect.jvm.internal.impl.name.Name
import kotlin.reflect.jvm.javaMethod
import kotlin.reflect.jvm.kotlin
import kotlin.reflect.jvm.kotlinFunction
import kotlin.reflect.memberProperties
import kotlin.reflect.primaryConstructor

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

    private val jsonCreator = javaClass<JsonCreator>()

    @suppress("UNCHECKED_CAST")
    override public fun hasCreatorAnnotation(member: Annotated): Boolean {
        // don't add a JsonCreator to any constructor if one is declared already

        if (member is AnnotatedConstructor) {
            // if has parameters, is a Kotlin class, and the parameters all have parameter annotations, then pretend we have a JsonCreator
            if (member.getParameterCount() > 0 &&  member.getDeclaringClass().getAnnotation(javaClass<KotlinClass>()) != null) {
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

    @suppress("UNCHECKED_CAST")
    protected fun findKotlinParameterName(param: AnnotatedParameter): String? {
        if (param.getDeclaringClass().getAnnotation(javaClass<KotlinClass>()) != null) {
            val kClass = (param.getDeclaringClass() as Class<Any>).kotlin

            val member = param.getOwner().getMember()
            val name = if (member is Constructor<*>) {
               (member as Constructor<Any>).kotlinFunction?.parameters?.get(param.index)?.name
            } else if (member is Method) {
                val temp = member.kotlinFunction
                if (temp == null && member.getAnnotation(javaClass<platformStatic>()) != null) {
                    // TODO: is returning null for static functions
                    // this is a huge work around for https://youtrack.jetbrains.com/issue/KT-8800

                    val kClassImpl = kClass as KClassImpl<Any>

                    val descriptor = KotlinReflectWorkaround.getCompanionClassDescriptor(kClassImpl)
                    if (descriptor != null) {
                        val members = descriptor.getMemberScope(TypeSubstitution.EMPTY)
                        val function = members.getFunctions(Name.identifier(member.name), NoLookupLocation.FROM_REFLECTION).filter {  // TODO: ask about LookupLocations ...
                            if (member.getParameterTypes().size() == it.valueParameters.size() &&
                                KotlinReflectWorkaround.getClassDescriptor(member.returnType.kotlin as KClassImpl).defaultType == it.returnType) {
                                val hasSameParamTypes = member.getParameterTypes().zip(it.valueParameters).all {
                                    KotlinReflectWorkaround.getClassDescriptor(it.first.kotlin as KClassImpl).defaultType == it.second.type
                                }
                                hasSameParamTypes
                            }
                            else {
                                false
                            }
                        }.firstOrNull()
                        function?.valueParameters?.get(param.index)?.name?.asString()
                    } else {
                        null
                    }
                } else {
                    temp?.parameters?.get(param.index)?.name
                }
            } else {
                null
            }
            return name
        }
        return null
    }

}
