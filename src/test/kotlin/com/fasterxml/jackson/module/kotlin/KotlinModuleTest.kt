package com.fasterxml.jackson.module.kotlin

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.KotlinFeature.*
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.test.assertNotNull

class KotlinModuleTest {
    @Test
    fun builder_Defaults() {
        val module = KotlinModule.Builder().build()

        assertEquals(512, module.reflectionCacheSize)
        assertFalse(module.nullToEmptyCollection)
        assertFalse(module.nullToEmptyMap)
        assertFalse(module.nullIsSameAsDefault)
        assertEquals(SingletonSupport.DISABLED, module.singletonSupport)
        assertFalse(module.strictNullChecks)
        assertFalse(module.kotlinPropertyNameAsImplicitName)
        assertFalse(module.useJavaDurationConversion)
    }

    @Test
    fun builder_SetAll() {
        val module = KotlinModule.Builder().apply {
            withReflectionCacheSize(123)
            enable(NullToEmptyCollection)
            enable(NullToEmptyMap)
            enable(NullIsSameAsDefault)
            enable(SingletonSupport)
            enable(StrictNullChecks)
            enable(KotlinPropertyNameAsImplicitName)
            enable(UseJavaDurationConversion)
        }.build()

        assertEquals(123, module.reflectionCacheSize)
        assertTrue(module.nullToEmptyCollection)
        assertTrue(module.nullToEmptyMap)
        assertTrue(module.nullIsSameAsDefault)
        assertEquals(SingletonSupport.CANONICALIZE, module.singletonSupport)
        assertTrue(module.strictNullChecks)
        assertTrue(module.kotlinPropertyNameAsImplicitName)
        assertTrue(module.useJavaDurationConversion)
    }

    @Test
    fun builder_NullToEmptyCollection() {
        val module = KotlinModule.Builder().apply {
            enable(NullToEmptyCollection)
        }.build()

        assertTrue(module.nullToEmptyCollection)
    }

    @Test
    fun builder_NullToEmptyMap() {
        val module = KotlinModule.Builder().apply {
            enable(NullToEmptyMap)
        }.build()

        assertTrue(module.nullToEmptyMap)
    }

    @Test
    fun builder_NullIsSameAsDefault() {
        val module = KotlinModule.Builder().apply {
            enable(NullIsSameAsDefault)
        }.build()

        assertTrue(module.nullIsSameAsDefault)
    }

    @Test
    fun builder_EnableCanonicalSingletonSupport() {
        val module = KotlinModule.Builder().apply {
            enable(SingletonSupport)
        }.build()

        assertEquals(SingletonSupport.CANONICALIZE, module.singletonSupport)
    }

    @Test
    fun builder_EnableStrictNullChecks() {
        val module = KotlinModule.Builder().apply {
            enable(StrictNullChecks)
        }.build()

        assertTrue(module.strictNullChecks)
    }

    @Test
    fun jdkSerializabilityTest() {
        val module = KotlinModule.Builder().apply {
            withReflectionCacheSize(123)
            enable(NullToEmptyCollection)
            enable(NullToEmptyMap)
            enable(NullIsSameAsDefault)
            enable(SingletonSupport)
            enable(StrictNullChecks)
        }.build()

        val serialized = jdkSerialize(module)
        val deserialized = jdkDeserialize<KotlinModule>(serialized)

        assertNotNull(deserialized)
        assertEquals(123, deserialized.reflectionCacheSize)
        assertTrue(deserialized.nullToEmptyCollection)
        assertTrue(deserialized.nullToEmptyMap)
        assertTrue(deserialized.nullIsSameAsDefault)
        assertEquals(SingletonSupport.CANONICALIZE, deserialized.singletonSupport)
        assertTrue(deserialized.strictNullChecks)
    }

    @Test
    fun findAndRegisterModulesTest() {
        val mapper = ObjectMapper().findAndRegisterModules()
        assertTrue(mapper.registeredModuleIds.contains("com.fasterxml.jackson.module.kotlin.KotlinModule"))
    }
}
