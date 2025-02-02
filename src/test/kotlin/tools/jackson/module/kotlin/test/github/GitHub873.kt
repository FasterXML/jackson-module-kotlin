package tools.jackson.module.kotlin.test.github

import tools.jackson.module.kotlin.defaultMapper
import tools.jackson.module.kotlin.readValue
import com.fasterxml.jackson.annotation.JsonValue
import kotlin.test.Test

class GitHub873 {
    @JvmInline
    value class Person(
        val properties: Map<String, Any>,
    )

    data class TimestampedPerson(
        val timestamp: Long,
        val person: Person,
    )

    @Test
    fun `should serialize value class`() {

        val person = Person(
            mapOf(
                "id" to "123",
                "updated" to "2023-11-22 12:11:23",
                "login" to "2024-01-15",
            ),
        )

        val serialized = defaultMapper.writeValueAsString(
            TimestampedPerson(
                123L,
                Person(person.properties),
            )
        )

        val deserialized = defaultMapper.readValue<TimestampedPerson>(serialized)

        assert(
            deserialized == TimestampedPerson(
                123L,
                Person(person.properties),
            )
        )
    }

    @JvmInline
    value class MapAsJsonValue(val value: String) {
        @get:JsonValue
        val jsonValue get() = mapOf("key" to value)
    }

    data class JsonValueWrapper(val value: MapAsJsonValue)

    @Test
    fun `JsonValue is serialized in the same way`() {
        val data = JsonValueWrapper(MapAsJsonValue("value"))
        val json = defaultMapper.writeValueAsString(data)

        assert("""{"value":{"key":"value"}}""" == json)
    }
}
