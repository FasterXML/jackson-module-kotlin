package tools.jackson.module.kotlin.test.github

import kotlin.test.assertEquals
import org.junit.jupiter.api.Test
import java.util.UUID
import com.fasterxml.jackson.annotation.JsonIdentityInfo
import com.fasterxml.jackson.annotation.ObjectIdGenerators
import tools.jackson.databind.MapperFeature
import tools.jackson.module.kotlin.jacksonMapperBuilder
import tools.jackson.module.kotlin.jacksonObjectMapper

class TestGithub194 {
    val mapperWithFinalFieldsAsMutators = jacksonMapperBuilder()
        .enable(MapperFeature.ALLOW_FINAL_FIELDS_AS_MUTATORS)
        .build()

    val id: UUID = UUID.fromString("149800a6-7855-4e09-9185-02e442da8013")
    val json = """{"id": "$id", "name": "Foo"}"""

    @Test
    fun testIdentityInfo() {
        val mapper = jacksonObjectMapper()
        val value = mapperWithFinalFieldsAsMutators.readValue(json, WithIdentity::class.java)
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
        val value = mapperWithFinalFieldsAsMutators.readValue(json, WithIdentityAndDefaultId::class.java)
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
