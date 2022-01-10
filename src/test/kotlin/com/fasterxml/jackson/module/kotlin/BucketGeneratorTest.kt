package com.fasterxml.jackson.module.kotlin

import org.junit.Assert.assertArrayEquals
import org.junit.Test
import kotlin.reflect.full.functions
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class BucketGeneratorTest {
    data class Data(val foo: Int, val bar: Int, val baz: Int) {
        companion object {
            fun creator(foo: Int, bar: Int, baz: Int) = Data(foo, bar, baz)
        }
    }

    @Test
    fun constructorTest() {
        val generator = BucketGenerator.forConstructor(::Data.parameters)

        val bucket1: ArgumentBucket = generator.generate()

        // In the case of constructor, the initial value will be empty.
        assertTrue(bucket1.isEmpty())
        assertArrayEquals(Array<Any?>(3) { null }, bucket1.actualValues)

        // Set one value
        bucket1[0] = 0
        assertFalse(bucket1.isEmpty())

        val bucket2: ArgumentBucket = generator.generate()

        // The initial value has not changed even after multiple regenerations.
        assertTrue(bucket2.isEmpty())
        assertArrayEquals(Array<Any?>(3) { null }, bucket2.actualValues)
    }

    @Test
    fun methodTest() {
        // KFunction needs to be retrieved in a way that requires instance parameters.
        val generator: BucketGenerator = Data.Companion::class.functions.first { it.name == "creator" }
            .let { BucketGenerator.forMethod(it.parameters, Data.Companion) }

        val bucket1: ArgumentBucket = generator.generate()
        val expectedValues = Array<Any?>(4) { null }.apply { this[0] = Data.Companion }

        assertEquals(1, bucket1.size)
        assertArrayEquals(expectedValues, bucket1.actualValues)

        // Set one value
        bucket1[1] = 0
        assertEquals(2, bucket1.size)

        val bucket2: ArgumentBucket = generator.generate()

        // The initial value has not changed even after multiple regenerations.
        assertEquals(1, bucket2.size)
        assertArrayEquals(expectedValues, bucket2.actualValues)
    }
}
