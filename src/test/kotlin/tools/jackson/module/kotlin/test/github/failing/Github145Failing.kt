package tools.jackson.module.kotlin.test.github.failing

import org.junit.Test

import com.fasterxml.jackson.annotation.JsonCreator

import tools.jackson.module.kotlin.jacksonObjectMapper
import tools.jackson.module.kotlin.readValue

/**
 * Class containing now-failing tests from [com.fasterxml.jackson.module.kotlin.test.github.TestGithub145],
 * since "Rewrite Bean Property Introspection logic in Jackson 2.x #4515" from
 * [jackson-databind#4515](https://github.com/FasterXML/jackson-databind/issues/4515) was completed.
 */
@Suppress("UNUSED_VARIABLE")
class Github145Failing {

    private val objectMapper = jacksonObjectMapper()

    // Cannot have companion object in class declared within function
    class Person7 constructor(val preName: String, val lastName: String) {
        private constructor(preNameAndLastName: String) : this(
            preNameAndLastName.substringBefore(","),
            preNameAndLastName.substringAfter(",")
        )

        companion object {
            @JsonCreator
            @JvmStatic
            fun createFromJson(preNameAndLastName: String): Person7 {
                return Person7(preNameAndLastName)
            }
        }
    }

    @Test
    fun testPerson7() {
        val person7Json = objectMapper.readValue<Person7>("""{"preName":"TestPreName","lastName":"TestLastname"}""")
    }
}