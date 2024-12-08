package com.fasterxml.jackson.module.kotlin

import org.junit.jupiter.api.Test
import kotlin.test.assertNotNull

class ReflectionCacheTest {
    @Test
    fun serializeEmptyCache() {
        val cache = ReflectionCache(100)
        val serialized = jdkSerialize(cache)
        val deserialized = jdkDeserialize<ReflectionCache>(serialized)

        assertNotNull(deserialized)
        // Deserialized instance also do not raise exceptions
        deserialized.kotlinFromJava(ReflectionCacheTest::class.java.getDeclaredMethod("serializeEmptyCache"))
    }

    @Test
    fun serializeNotEmptyCache() {
        val method = ReflectionCacheTest::class.java.getDeclaredMethod("serializeNotEmptyCache")

        val cache = ReflectionCache(100).apply { kotlinFromJava(method) }
        val serialized = jdkSerialize(cache)
        val deserialized = jdkDeserialize<ReflectionCache>(serialized)

        assertNotNull(deserialized)
        // Deserialized instance also do not raise exceptions
        deserialized.kotlinFromJava(method)
    }
}
