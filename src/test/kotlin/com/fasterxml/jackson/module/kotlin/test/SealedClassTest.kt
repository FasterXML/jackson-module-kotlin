package com.fasterxml.jackson.module.kotlin.test

import com.fasterxml.jackson.annotation.JsonTypeInfo
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.test.SealedClassTest.SuperClass.B
import org.junit.Test
import kotlin.test.assertTrue

class SealedClassTest {
    private val mapper = jacksonObjectMapper()

    /**
     * Json of a Serialized B-Object.
     */
    private val jsonB = """{"@type":"SealedClassTest${"$"}SuperClass${"$"}B"}"""

    /**
     * Tests that the @JsonSubTypes-Annotation is not necessary when working with Sealed-Classes.
     */
    @Test
    fun sealedClassWithoutSubTypes() {
        val result = mapper.readValue(jsonB, SuperClass::class.java)
        assertTrue { result is B }
    }

    @JsonTypeInfo(use = JsonTypeInfo.Id.NAME)
    sealed class SuperClass {
        class A : SuperClass()
        class B : SuperClass()
    }
}
