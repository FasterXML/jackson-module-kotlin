package tools.jackson.module.kotlin

import tools.jackson.databind.util.ClassUtil
import tools.jackson.databind.introspect.AnnotatedClass
import tools.jackson.databind.introspect.AnnotatedMember
import tools.jackson.databind.introspect.AnnotatedMethod
import tools.jackson.databind.introspect.AnnotatedMethodMap
import tools.jackson.databind.introspect.MemberKey
import java.lang.reflect.Method
import java.lang.reflect.Modifier
import java.lang.reflect.Proxy

/**
 * Re-selects conflicting zero-arg methods for Kotlin classes so Jackson consistently
 * prefers the more specific return type (for example `List` over `Collection`).
 */
internal object KotlinMethodCollectorFix {
    private val memberMethodsField = AnnotatedClass::class.java.getDeclaredField("_memberMethods").apply {
        isAccessible = true
    }

    fun fixMethodMap(annotatedClass: AnnotatedClass) {
        if (!annotatedClass.rawType.isKotlinClass()) {
            return
        }

        annotatedClass.memberMethods()

        @Suppress("UNCHECKED_CAST")
        val originalMap = memberMethodsField.get(annotatedClass) as AnnotatedMethodMap?
        if (originalMap == null || originalMap.size() == 0) {
            return
        }

        val selectedMethods = collectKotlinAwareMethods(annotatedClass.rawType)
        val rebuilt = LinkedHashMap<MemberKey, AnnotatedMethod>(originalMap.size())

        for (annotatedMethod in originalMap) {
            val member = annotatedMethod.member as Method
            val key = MemberKey(member)
            val preferred = selectedMethods[key]
            rebuilt[key] = if (preferred == null || preferred == member) {
                annotatedMethod
            } else {
                AnnotatedMethod(
                    annotatedMethod.typeContext,
                    preferred,
                    annotatedMethod.annotationMap,
                    null,
                )
            }
        }

        memberMethodsField.set(annotatedClass, AnnotatedMethodMap(rebuilt))
    }

    private fun collectKotlinAwareMethods(rawClass: Class<*>): Map<MemberKey, Method> {
        val methods = LinkedHashMap<MemberKey, Method>()
        for (method in ClassUtil.getClassMethods(rawClass)) {
            if (!isIncludableMemberMethod(method)) {
                continue
            }
            val key = MemberKey(method)
            val existing = methods[key]
            if (existing == null || shouldReplaceForKotlin(existing, method)) {
                methods[key] = method
            }
        }
        return methods
    }

    private fun isIncludableMemberMethod(method: Method): Boolean {
        if (Modifier.isStatic(method.modifiers) || method.isSynthetic || method.isBridge) {
            return false
        }
        return method.parameterCount <= 2
    }

    private fun shouldReplaceForKotlin(current: Method, replace: Method): Boolean {
        if (accessLevel(replace) > accessLevel(current)) {
            return true
        }
        if (Proxy.isProxyClass(current.declaringClass) && !Proxy.isProxyClass(replace.declaringClass)) {
            return true
        }
        if (Modifier.isAbstract(current.modifiers) && !Modifier.isAbstract(replace.modifiers)) {
            return true
        }

        val currentReturnType = current.returnType
        val replaceReturnType = replace.returnType
        if (currentReturnType != replaceReturnType) {
            if (currentReturnType.isAssignableFrom(replaceReturnType)) {
                return true
            }
            if (replaceReturnType.isAssignableFrom(currentReturnType)) {
                return false
            }
        }
        return false
    }

    private fun accessLevel(method: Method): Int =
        minOf(accessLevel(method.modifiers), accessLevel(method.declaringClass.modifiers))

    private fun accessLevel(modifiers: Int): Int = when {
        Modifier.isPublic(modifiers) -> 3
        Modifier.isProtected(modifiers) -> 2
        Modifier.isPrivate(modifiers) -> 0
        else -> 1
    }

    private val AnnotatedMethod.typeContext
        get() = AnnotatedMember::class.java.getDeclaredField("_typeContext").let {
            it.isAccessible = true
            it.get(this) as tools.jackson.databind.introspect.TypeResolutionContext
        }

    private val AnnotatedMethod.annotationMap
        get() = _annotationMap()
}
