package com.fasterxml.jackson.module.kotlin

import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

internal class ArgumentBucketTest {
    data class Data(val foo: Int, val bar: Int, val baz: Int)
    private val parameters = ::Data.parameters
    private val generator = BucketGenerator.forConstructor(parameters)

    @Test
    fun setTest() {
        val bucket = generator.generate()

        assertTrue(bucket.isEmpty())
        assertNull(bucket[parameters[0]])

        // set will succeed.
        bucket[0] = 0
        assertEquals(1, bucket.size)
        assertEquals(0, bucket[parameters[0]])

        // If set the same key multiple times, the original value will not be rewritten.
        bucket[0] = 1
        assertEquals(1, bucket.size)
        assertEquals(0, bucket[parameters[0]])
    }

    @Test
    fun isFullInitializedTest() {
        val bucket = generator.generate()

        assertFalse(bucket.isFullInitialized())

        (parameters.indices).forEach { bucket[it] = it }

        assertTrue(bucket.isFullInitialized())
    }

    @Test
    fun containsValueTest() {
        val bucket = generator.generate()

        assertFalse(bucket.containsValue(null))
        bucket[0] = null
        assertTrue(bucket.containsValue(null))

        assertFalse(bucket.containsValue(1))
        bucket[1] = 1
        assertTrue(bucket.containsValue(1))
    }
}
