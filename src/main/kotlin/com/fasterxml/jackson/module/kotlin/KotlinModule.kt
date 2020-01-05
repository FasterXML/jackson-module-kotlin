package com.fasterxml.jackson.module.kotlin

import com.fasterxml.jackson.databind.MapperFeature
import com.fasterxml.jackson.databind.module.SimpleModule
import kotlin.reflect.KClass

private val metadataFqName = "kotlin.Metadata"

fun Class<*>.isKotlinClass(): Boolean {
    return declaredAnnotations.any { it.annotationClass.java.name == metadataFqName }
}

class KotlinModule constructor (
    val reflectionCacheSize: Int = 512,
    val nullToEmptyCollection: Boolean = false,
    val nullToEmptyMap: Boolean = false,
    val nullisSameAsDefault: Boolean = false
) : SimpleModule(PackageVersion.VERSION) {

    @Deprecated(level = DeprecationLevel.HIDDEN, message = "For ABI compatibility")
    constructor(
        reflectionCacheSize: Int = 512,
        nullToEmptyCollection: Boolean = false,
        nullToEmptyMap: Boolean = false
    ) : this(reflectionCacheSize, nullToEmptyCollection, nullToEmptyMap, false)

    companion object {
        const val serialVersionUID = 1L
    }

    private val ignoredClassesForImplyingJsonCreator = emptySet<KClass<*>>()

    override fun setupModule(context: SetupContext) {
        super.setupModule(context)

        if (!context.isEnabled(MapperFeature.USE_ANNOTATIONS)) {
            throw IllegalStateException("The Jackson Kotlin module requires USE_ANNOTATIONS to be true or it cannot function")
        }

        val cache = ReflectionCache(reflectionCacheSize)

        context.addValueInstantiators(KotlinInstantiators(cache, nullToEmptyCollection, nullToEmptyMap, nullisSameAsDefault))

        // [module-kotlin#225]: keep Kotlin singletons as singletons
        context.addBeanDeserializerModifier(KotlinBeanDeserializerModifier)

        context.insertAnnotationIntrospector(KotlinAnnotationIntrospector(context, cache, nullToEmptyCollection, nullToEmptyMap, nullisSameAsDefault))
        context.appendAnnotationIntrospector(KotlinNamesAnnotationIntrospector(this, cache, ignoredClassesForImplyingJsonCreator))

        context.addDeserializers(KotlinDeserializers())
        context.addSerializers(KotlinSerializers())

        fun addMixIn(clazz: Class<*>, mixin: Class<*>) {
            context.setMixInAnnotations(clazz, mixin)
        }

        // ranges
        addMixIn(IntRange::class.java, ClosedRangeMixin::class.java)
        addMixIn(CharRange::class.java, ClosedRangeMixin::class.java)
        addMixIn(LongRange::class.java, ClosedRangeMixin::class.java)
        addMixIn(ClosedRange::class.java, ClosedRangeMixin::class.java)
    }
}


