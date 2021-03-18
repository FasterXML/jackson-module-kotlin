package com.fasterxml.jackson.module.kotlin.test

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonSubTypes
import com.fasterxml.jackson.annotation.JsonTypeInfo
import com.fasterxml.jackson.annotation.JsonValue
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.test.SealedClassTest.SuperClass.B
import org.junit.Test
import kotlin.test.assertEquals
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

    /**
     * Tests that we can use JsonTypeInfo.Id.DEDUCTION to deduct sealed types without the need for explicit fields.
     */
    @Test
    fun sealedClassWithoutTypeDiscriminator() {
        val serializedSingle = """{"request":"single"}"""
        val single = mapper.readValue(serializedSingle, SealedRequest::class.java)
        assertTrue(single is SealedRequest.SingleRequest)
        assertEquals("single", single.request)

        val serializedBatch = """[{"request":"first"},{"request":"second"}]"""
        val batch = mapper.readValue(serializedBatch, SealedRequest::class.java)
        assertTrue(batch is SealedRequest.BatchRequest)
        assertEquals(2, batch.requests.size)
        assertEquals("first", batch.requests[0].request)
        assertEquals("second", batch.requests[1].request)
    }

    @JsonTypeInfo(use = JsonTypeInfo.Id.NAME)
    sealed class SuperClass {
        class A : SuperClass()
        class B : SuperClass()
    }

    @JsonTypeInfo(use = JsonTypeInfo.Id.DEDUCTION)
    sealed class SealedRequest {
        data class SingleRequest(val request: String) : SealedRequest()
        data class BatchRequest @JsonCreator constructor(@get:JsonValue val requests: List<SingleRequest>): SealedRequest()
    }
}
