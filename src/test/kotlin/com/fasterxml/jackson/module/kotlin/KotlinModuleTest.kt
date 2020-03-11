package com.fasterxml.jackson.module.kotlin

import org.junit.Assert.*
import org.junit.Test

class KotlinModuleTest {
    @Test
    fun builder_Defaults() {
        val module = KotlinModule.Builder().build()

        assertEquals(512, module.reflectionCacheSize)
        assertFalse(module.nullToEmptyCollection)
        assertFalse(module.nullToEmptyMap)
        assertFalse(module.nullIsSameAsDefault)
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
}
