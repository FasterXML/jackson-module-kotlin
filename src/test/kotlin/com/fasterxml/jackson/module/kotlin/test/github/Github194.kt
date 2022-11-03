package com.fasterxml.jackson.module.kotlin.test.github

import com.fasterxml.jackson.annotation.JsonIdentityInfo
import com.fasterxml.jackson.annotation.ObjectIdGenerators
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import org.junit.Test
import java.util.UUID
import kotlin.test.assertEquals

class TestGithub194 {
    val id: UUID = UUID.fromString("149800a6-7855-4e09-9185-02e442da8013")
    val json = """{"id": "$id", "name": "Foo"}"""

    @Test
    fun testIdentityInfo() {
        val mapper = jacksonObjectMapper()
        val value = mapper.readValue(json, WithIdentity::class.java)
        assertEquals(id, value.id)
        assertEquals(id.toString(), value.idString)
        assertEquals("Foo", value.name)
    }

    @JsonIdentityInfo(
            property = "id",
            scope = WithIdentity::class,
            generator = ObjectIdGenerators.PropertyGenerator::class
    )
    class WithIdentity(val id: UUID,
                       val idString: String = id.toString(),
                       val name: String)

    @Test
    fun testIdentityInfo_WithDefaultId() {
        val mapper = jacksonObjectMapper()
        val value = mapper.readValue(json, WithIdentityAndDefaultId::class.java)
        assertEquals(id, value.id)
        assertEquals(id.toString(), value.idString)
        assertEquals("Foo", value.name)
    }

    @JsonIdentityInfo(
            property = "id",
            scope = WithIdentityAndDefaultId::class,
            generator = ObjectIdGenerators.PropertyGenerator::class
    )
    class WithIdentityAndDefaultId(val id: UUID,
                                   val idString: String = id.toString(),
                                   val name: String)
}
