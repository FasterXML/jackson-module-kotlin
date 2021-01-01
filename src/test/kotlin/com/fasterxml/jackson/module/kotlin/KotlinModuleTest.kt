package com.fasterxml.jackson.module.kotlin

import com.fasterxml.jackson.module.kotlin.SingletonSupport.CANONICALIZE
import com.fasterxml.jackson.module.kotlin.SingletonSupport.DISABLED
import org.junit.Assert.*
import org.junit.Test
import kotlin.reflect.full.memberProperties
import kotlin.reflect.full.primaryConstructor

class KotlinModuleTest {
    /**
     * Ensure that the main constructor and the Builder have the same default settings.
     */
    @Test
    fun constructorAndBuilderCreateSameDefaultObject() {
        val constructorModule = KotlinModule()
        val builderModule = KotlinModule.Builder().build()


        assertEquals(KotlinModule::class.primaryConstructor?.parameters?.size, KotlinModule.Builder::class.memberProperties.size)
        assertEquals(constructorModule.reflectionCacheSize, builderModule.reflectionCacheSize)
        assertEquals(constructorModule.nullToEmptyCollection, builderModule.nullToEmptyCollection)
        assertEquals(constructorModule.nullToEmptyMap, builderModule.nullToEmptyMap)
        assertEquals(constructorModule.nullIsSameAsDefault, builderModule.nullIsSameAsDefault)
        assertEquals(constructorModule.singletonSupport, builderModule.singletonSupport)
        assertEquals(constructorModule.strictNullChecks, builderModule.strictNullChecks)
    }

    @Test
    fun builder_Defaults() {
        val module = KotlinModule.Builder().build()

        assertEquals(512, module.reflectionCacheSize)
        assertFalse(module.nullToEmptyCollection)
        assertFalse(module.nullToEmptyMap)
        assertFalse(module.nullIsSameAsDefault)
        assertEquals(DISABLED, module.singletonSupport)
        assertFalse(module.strictNullChecks)
    }

    @Test
    fun builder_SetAll() {
        val module = KotlinModule.Builder().apply {
            reflectionCacheSize(123)
            nullToEmptyCollection(true)
            nullToEmptyMap(true)
            nullIsSameAsDefault(true)
            singletonSupport(CANONICALIZE)
            strictNullChecks(true)
        }.build()

        assertEquals(123, module.reflectionCacheSize)
        assertTrue(module.nullToEmptyCollection)
        assertTrue(module.nullToEmptyMap)
        assertTrue(module.nullIsSameAsDefault)
        assertEquals(CANONICALIZE, module.singletonSupport)
        assertTrue(module.strictNullChecks)
    }

    @Test
    fun builder_NullToEmptyCollection() {
        val module = KotlinModule.Builder().apply {
            nullToEmptyCollection(true)
        }.build()

        assertTrue(module.nullToEmptyCollection)
    }

    @Test
    fun builder_NullToEmptyMap() {
        val module = KotlinModule.Builder().apply {
            nullToEmptyMap(true)
        }.build()

        assertTrue(module.nullToEmptyMap)
    }

    @Test
    fun builder_NullIsSameAsDefault() {
        val module = KotlinModule.Builder().apply {
            nullIsSameAsDefault(true)
        }.build()

        assertTrue(module.nullIsSameAsDefault)
    }

    @Test
    fun builder_EnableCanonicalSingletonSupport() {
        val module = KotlinModule.Builder().apply {
            singletonSupport(CANONICALIZE)
        }.build()

        assertEquals(CANONICALIZE, module.singletonSupport)
    }

    @Test
    fun builder_EnableStrictNullChecks() {
        val module = KotlinModule.Builder().apply {
            strictNullChecks(true)
        }.build()

        assertTrue(module.strictNullChecks)
    }
}
