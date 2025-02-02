package com.fasterxml.jackson.module.kotlin.test.github

import com.fasterxml.jackson.module.kotlin.defaultMapper
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import kotlin.test.Test

class GitHub873 {
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
    value class Person(
        val properties: Map<String, Any>,
    )

    data class TimestampedPerson(
        val timestamp: Long,
        val person: Person,
    )
}
