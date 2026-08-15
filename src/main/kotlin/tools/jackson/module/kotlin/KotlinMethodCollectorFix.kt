package tools.jackson.module.kotlin

import tools.jackson.databind.introspect.AnnotatedClass
import tools.jackson.databind.introspect.AnnotatedMethod
import tools.jackson.databind.introspect.MemberKey
import tools.jackson.databind.util.ClassUtil
import java.lang.reflect.Method
import java.lang.reflect.Modifier
import java.lang.reflect.Proxy

/**
 * Re-selects conflicting zero-arg methods for Kotlin classes so Jackson consistently
 * prefers the more specific return type (for example `List` over `Collection`).
 */
internal object KotlinMethodCollectorFix {
    fun correctedMemberMethods(annotatedClass: AnnotatedClass): Iterable<AnnotatedMethod> {
        if (!annotatedClass.rawType.isKotlinClass()) {
            return annotatedClass.memberMethods()
        }

        val originalMethods = annotatedClass.memberMethods().asIterable().toList()
        if (originalMethods.isEmpty()) {
            return emptyList()
        }

        val selectedMethods = collectKotlinAwareMethods(annotatedClass.rawType)
        val rebuilt = ArrayList<AnnotatedMethod>(originalMethods.size)

        for (annotatedMethod in originalMethods) {
            val member = annotatedMethod.member as Method
            val key = MemberKey(member)
            val preferred = selectedMethods[key]
            rebuilt.add(
                if (preferred == null || preferred == member) {
                    annotatedMethod
                } else {
                    AnnotatedMethod(
                        annotatedClass,
                        preferred,
                        annotatedMethod._annotationMap(),
                        null,
                    )
                },
            )
        }

        return rebuilt
    }

    private fun collectKotlinAwareMethods(rawClass: Class<*>): Map<MemberKey, Method> =
        selectKotlinAwareMethods(ClassUtil.getClassMethods(rawClass).asList())

    internal fun selectKotlinAwareMethods(methods: Iterable<Method>): Map<MemberKey, Method> {
        val selected = LinkedHashMap<MemberKey, Method>()
        for (method in methods) {
            if (!isIncludableMemberMethod(method)) {
                continue
            }
            val key = MemberKey(method)
            val existing = selected[key]
            if (existing == null || shouldReplaceForKotlin(existing, method)) {
                selected[key] = method
            }
        }
        return selected
    }

    private fun isIncludableMemberMethod(method: Method): Boolean {
        if (Modifier.isStatic(method.modifiers) || method.isSynthetic || method.isBridge) {
            return false
        }
        return method.parameterCount <= 2
    }

    internal fun shouldReplaceForKotlin(current: Method, replace: Method): Boolean {
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
}
