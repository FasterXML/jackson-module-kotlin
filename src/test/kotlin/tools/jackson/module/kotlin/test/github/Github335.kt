package tools.jackson.module.kotlin.test.github

import tools.jackson.annotation.JsonSubTypes
import tools.jackson.annotation.JsonSubTypes.Type
import tools.jackson.annotation.JsonTypeInfo
import tools.jackson.annotation.JsonTypeInfo.As
import tools.jackson.annotation.JsonTypeInfo.Id
import tools.jackson.module.kotlin.jacksonObjectMapper
import tools.jackson.module.kotlin.readValue
import org.junit.Test
import kotlin.test.assertEquals

class Github335Test {
    val mapper = _root_ide_package_.tools.jackson.module.kotlin.jacksonObjectMapper()

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

        assertEquals(oldEntity, newEntity)
    }
}
