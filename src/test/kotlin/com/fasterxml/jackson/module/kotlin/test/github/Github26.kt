package com.fasterxml.jackson.module.kotlin.test.github

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import org.junit.Test
import kotlin.test.assertEquals

data class ClassWithPrimitivesWithDefaults(val i: Int = 5, val x: Int)

class TestGithub26 {
    @Test fun testConstructorWithPrimitiveTypesDefaultedExplicitlyAndImplicitly() {
        val check1: ClassWithPrimitivesWithDefaults = jacksonObjectMapper().readValue("""{"i":3,"x":2}""")
        assertEquals(3, check1.i)
        assertEquals(2, check1.x)

        val check2: ClassWithPrimitivesWithDefaults = jacksonObjectMapper().readValue("""{}""")
        assertEquals(5, check2.i)
        assertEquals(0, check2.x)

        val check3: ClassWithPrimitivesWithDefaults = jacksonObjectMapper().readValue("""{"i": 2}""")
        assertEquals(2, check3.i)
        assertEquals(0, check3.x)

    }

}