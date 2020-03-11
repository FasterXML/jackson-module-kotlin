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
    }

    @Test
    fun builder_Defaults() {
        val module = KotlinModule.Builder().build()

        assertEquals(512, module.reflectionCacheSize)
        assertFalse(module.nullToEmptyCollection)
        assertFalse(module.nullToEmptyMap)
        assertFalse(module.nullIsSameAsDefault)
        assertEquals(DISABLED, module.singletonSupport)
    }

    @Test
    fun builder_SetAll() {
        val module = KotlinModule.Builder().apply {
            reflectionCacheSize(123)
            nullToEmptyCollection(true)
            nullToEmptyMap(true)
            nullIsSameAsDefault(true)
        }.build()

        assertEquals(123, module.reflectionCacheSize)
        assertTrue(module.nullToEmptyCollection)
        assertTrue(module.nullToEmptyMap)
        assertTrue(module.nullIsSameAsDefault)
    }

    @Test
    fun builder_NullToEmptyCollection() {
        val module = KotlinModule.Builder().apply {
            nullToEmptyCollection(true)
        }.build()

        assertEquals(512, module.reflectionCacheSize)
        assertTrue(module.nullToEmptyCollection)
        assertFalse(module.nullToEmptyMap)
        assertFalse(module.nullIsSameAsDefault)
    }

    @Test
    fun builder_NullToEmptyMap() {
        val module = KotlinModule.Builder().apply {
            nullToEmptyMap(true)
        }.build()

        assertEquals(512, module.reflectionCacheSize)
        assertFalse(module.nullToEmptyCollection)
        assertTrue(module.nullToEmptyMap)
        assertFalse(module.nullIsSameAsDefault)
    }

    @Test
    fun builder_NullIsSameAsDefault() {
        val module = KotlinModule.Builder().apply {
            nullIsSameAsDefault(true)
        }.build()

        assertEquals(512, module.reflectionCacheSize)
        assertFalse(module.nullToEmptyCollection)
        assertFalse(module.nullToEmptyMap)
        assertTrue(module.nullIsSameAsDefault)
    }

    @Test
    fun builder_EnableCanonicalSingletonSupport() {
        val module = KotlinModule.Builder().apply {
            enableExperimentalSingletonSupport(CANONICALIZE)
        }.build()

        assertEquals(512, module.reflectionCacheSize)
        assertFalse(module.nullToEmptyCollection)
        assertFalse(module.nullToEmptyMap)
        assertFalse(module.nullIsSameAsDefault)
        assertEquals(CANONICALIZE, module.singletonSupport)
    }
}
