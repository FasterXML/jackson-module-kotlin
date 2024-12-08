package com.fasterxml.jackson.module.kotlin.test

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
 import org.junit.jupiter.api.Assertions.assertEquals
 import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

// from https://github.com/ProjectMapK/jackson-module-kogera/blob/7872116052c9a4744c6d4e84ddd5cab6bb525024/src/test/kotlin/io/github/projectmapk/jackson/module/kogera/zIntegration/deser/VarargTest.kt
class VarargDeserTest {
    val mapper = jacksonObjectMapper()

    class OnlyVararg(vararg val v: Int)

    @Nested
    inner class OnlyVarargTest {
        @Test
        fun hasArgs() {
            val r = mapper.readValue<OnlyVararg>("""{"v":[1,2,3]}""")
            assertEquals(listOf(1, 2, 3), r.v.asList())
        }

        @Test
        fun empty() {
            val r = mapper.readValue<OnlyVararg>("""{"v":[]}""")
            assertTrue(r.v.isEmpty())
        }

        @Test
        fun undefined() {
            val r = mapper.readValue<OnlyVararg>("""{}""")
            assertTrue(r.v.isEmpty())
        }
    }

    class HeadVararg(vararg val v: Int?, val i: Int)

    @Nested
    inner class HeadVarargTest {
        @Test
        fun hasArgs() {
            val r = mapper.readValue<HeadVararg>("""{"i":0,"v":[1,2,null]}""")
            assertEquals(listOf(1, 2, null), r.v.asList())
            assertEquals(0, r.i)
        }

        @Test
        fun empty() {
            val r = mapper.readValue<HeadVararg>("""{"i":0,"v":[]}""")
            assertTrue(r.v.isEmpty())
            assertEquals(0, r.i)
        }

        @Test
        fun undefined() {
            val r = mapper.readValue<HeadVararg>("""{"i":0}""")
            assertTrue(r.v.isEmpty())
            assertEquals(0, r.i)
        }
    }

    class TailVararg(val i: Int, vararg val v: String)

    @Nested
    inner class TailVarargTest {
        @Test
        fun hasArgs() {
            val r = mapper.readValue<TailVararg>("""{"i":0,"v":["foo","bar","baz"]}""")
            assertEquals(listOf("foo", "bar", "baz"), r.v.asList())
            assertEquals(0, r.i)
        }

        @Test
        fun empty() {
            val r = mapper.readValue<TailVararg>("""{"i":0,"v":[]}""")
            assertTrue(r.v.isEmpty())
            assertEquals(0, r.i)
        }

        @Test
        fun undefined() {
            val r = mapper.readValue<TailVararg>("""{"i":0}""")
            assertTrue(r.v.isEmpty())
            assertEquals(0, r.i)
        }
    }

    class HasDefaultVararg(vararg val v: String? = arrayOf("foo", "bar"))

    @Nested
    inner class HasDefaultVarargTest {
        @Test
        fun hasArgs() {
            val r = mapper.readValue<HasDefaultVararg>("""{"v":["foo","bar",null]}""")
            assertEquals(listOf("foo", "bar", null), r.v.asList())
        }

        @Test
        fun empty() {
            val r = mapper.readValue<HasDefaultVararg>("""{"v":[]}""")
            assertTrue(r.v.isEmpty())
        }

        @Test
        fun undefined() {
            val r = mapper.readValue<HasDefaultVararg>("""{}""")
            assertEquals(listOf("foo", "bar"), r.v.asList())
        }
    }
}
