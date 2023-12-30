package com.fasterxml.jackson.module.kotlin.test

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertTrue
import org.junit.Ignore
import org.junit.experimental.runners.Enclosed
import org.junit.runner.RunWith
import kotlin.test.Test

// from https://github.com/ProjectMapK/jackson-module-kogera/blob/0631cd3b07c7fb6971a00ac1f6811b4367a1720e/src/test/kotlin/io/github/projectmapk/jackson/module/kogera/zIntegration/deser/VarargTest.kt#L1
@RunWith(Enclosed::class)
class VarargDeserTest {
    @Ignore
    companion object {
        val mapper = jacksonObjectMapper()
    }

    @Ignore
    class OnlyVararg(vararg val v: Int)

    class OnlyVarargTest {
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

    @Ignore
    class HeadVararg(vararg val v: Int?, val i: Int)

    class HeadVarargTest {
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

    @Ignore
    class TailVararg(val i: Int, vararg val v: String)

    class TailVarargTest {
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

    @Ignore
    class HasDefaultVararg(vararg val v: String? = arrayOf("foo", "bar"))

    class HasDefaultVarargTest {
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
