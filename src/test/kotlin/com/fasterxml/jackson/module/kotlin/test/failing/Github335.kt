package com.fasterxml.jackson.module.kotlin.test.failing

import com.fasterxml.jackson.annotation.JsonSubTypes
import com.fasterxml.jackson.annotation.JsonSubTypes.Type
import com.fasterxml.jackson.annotation.JsonTypeInfo
import com.fasterxml.jackson.annotation.JsonTypeInfo.As
import com.fasterxml.jackson.annotation.JsonTypeInfo.Id
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import org.junit.Test
import kotlin.test.assertNull
import kotlin.test.fail

class Github335Test {
    val mapper = jacksonObjectMapper()

    interface Payload
    data class UniquePayload(val data: String) : Payload

    data class MyEntity(
            val type: String?,
            @JsonTypeInfo(use = Id.NAME, include = As.EXTERNAL_PROPERTY, property = "type")
            @JsonSubTypes(Type(value = UniquePayload::class, name = "UniquePayload"))
            val payload: Payload?
    )

    @Test
    fun serializeAndDeserializeTypeable() {
        val oldEntity = MyEntity(null, null)
        val json = mapper.writeValueAsString(oldEntity)
        val newEntity = mapper.readValue<MyEntity>(json)

        try {
            // Returns false: "null" String instead of null
            assertNull(newEntity.type)
            fail("GitHub #335 has been fixed!")
        } catch (e: AssertionError) {
            // Remove this try/catch and the `fail()` call above when this issue is fixed
        }
    }
}
