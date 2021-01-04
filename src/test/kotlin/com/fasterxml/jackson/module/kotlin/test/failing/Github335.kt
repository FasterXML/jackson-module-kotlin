package com.fasterxml.jackson.module.kotlin.test.failing

import com.fasterxml.jackson.annotation.JsonSubTypes
import com.fasterxml.jackson.annotation.JsonSubTypes.Type
import com.fasterxml.jackson.annotation.JsonTypeInfo
import com.fasterxml.jackson.annotation.JsonTypeInfo.As
import com.fasterxml.jackson.annotation.JsonTypeInfo.Id
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import com.fasterxml.jackson.module.kotlin.test.expectFailure
import org.junit.Test
import kotlin.test.assertNull

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

        expectFailure<AssertionError>("GitHub #335 has been fixed!") {
            // newEntity.type is the string "null" instead of the null value
            assertNull(newEntity.type)
        }
    }
}
