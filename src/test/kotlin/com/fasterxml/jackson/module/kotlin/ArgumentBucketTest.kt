package com.fasterxml.jackson.module.kotlin

import com.fasterxml.jackson.annotation.JsonCreator
import org.junit.jupiter.api.Nested
import kotlin.reflect.KFunction
import kotlin.reflect.full.functions
import kotlin.reflect.full.hasAnnotation
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class ArgumentBucketTest {
    data class Constructor(val foo: String, val bar: String)

    data class Method(val foo: String, val bar: String) {
        companion object {
            @JvmStatic
            @JsonCreator
            fun of(foo: String, bar: String): Method = Method(foo, bar)
        }
    }

    @Nested
    inner class Normal {
        @Test
        fun constructorTest() {
            val function: KFunction<*> = ::Constructor
            val params = function.parameters
            val generator = BucketGenerator.forConstructor(params.size)
            val bucket = generator.generate()

            assertTrue(bucket.isEmpty())
            assertEquals(0, bucket.size)
            assertFalse(bucket.isFullInitialized)

            bucket[params[0]] = "foo"

            assertFalse(bucket.isEmpty())
            assertEquals(1, bucket.size)
            assertFalse(bucket.isFullInitialized)
            assertEquals("foo", bucket[params[0]])

            bucket[params[1]] = "bar"

            assertFalse(bucket.isEmpty())
            assertEquals(2, bucket.size)
            assertTrue(bucket.isFullInitialized)
            assertEquals("bar", bucket[params[1]])
        }

        @Test
        fun methodTest() {
            val function: KFunction<*> = Method.Companion::class.functions.first { it.hasAnnotation<JsonCreator>() }
            val params = function.parameters
            val generator = BucketGenerator.forMethod(params.size, params[0], Method)
            val bucket = generator.generate()

            assertFalse(bucket.isEmpty())
            assertEquals(1, bucket.size)
            assertEquals(Method.Companion, bucket[params[0]])
            assertFalse(bucket.isFullInitialized)

            bucket[params[1]] = "foo"

            assertFalse(bucket.isEmpty())
            assertEquals(2, bucket.size)
            assertFalse(bucket.isFullInitialized)
            assertEquals("foo", bucket[params[1]])

            bucket[params[2]] = "bar"

            assertFalse(bucket.isEmpty())
            assertEquals(3, bucket.size)
            assertTrue(bucket.isFullInitialized)
            assertEquals("bar", bucket[params[2]])
        }
    }

    // After support, add a case with a value class.
}
