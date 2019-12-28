package com.fasterxml.jackson.module.kotlin

import com.fasterxml.jackson.databind.MapperFeature
import com.fasterxml.jackson.databind.module.SimpleModule

private val metadataFqName = "kotlin.Metadata"

fun Class<*>.isKotlinClass(): Boolean {
    return declaredAnnotations.any { it.annotationClass.java.name == metadataFqName }
}

class KotlinModule(val reflectionCacheSize: Int = 512, val nullToEmptyCollection: Boolean = false, val nullToEmptyMap: Boolean = false) : SimpleModule(PackageVersion.VERSION) {
    companion object {
        const val serialVersionUID = 1L
    }

    override fun setupModule(context: SetupContext) {
        super.setupModule(context)

        if (!context.isEnabled(MapperFeature.USE_ANNOTATIONS)) {
            throw IllegalStateException("The Jackson Kotlin module requires USE_ANNOTATIONS to be true or it cannot function")
        }

        val cache = ReflectionCache(reflectionCacheSize)

        context.addValueInstantiators(KotlinInstantiators(cache, nullToEmptyCollection, nullToEmptyMap))

        // [module-kotlin#225]: keep Kotlin singletons as singletons
        context.addDeserializerModifier(KotlinBeanDeserializerModifier)

        fun addMixIn(clazz: Class<*>, mixin: Class<*>) {
            context.setMixIn(clazz, mixin)
        }

        context.insertAnnotationIntrospector(KotlinAnnotationIntrospector(context, cache, nullToEmptyCollection, nullToEmptyMap))
        context.appendAnnotationIntrospector(KotlinNamesAnnotationIntrospector(this, cache))

        // ranges
        addMixIn(IntRange::class.java, ClosedRangeMixin::class.java)
        addMixIn(CharRange::class.java, ClosedRangeMixin::class.java)
        addMixIn(LongRange::class.java, ClosedRangeMixin::class.java)
        addMixIn(ClosedRange::class.java, ClosedRangeMixin::class.java)
    }
}
