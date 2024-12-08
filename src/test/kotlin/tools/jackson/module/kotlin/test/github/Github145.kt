package tools.jackson.module.kotlin.test.github

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonProperty
import tools.jackson.databind.ObjectMapper
import tools.jackson.module.kotlin.jacksonObjectMapper
import tools.jackson.module.kotlin.readValue
import org.junit.jupiter.api.Test

@Suppress("UNUSED_VARIABLE")
class TestGithub145 {
    private val objectMapper = jacksonObjectMapper()

    @Test
    fun workingTestWithoutKotlinModule() {
        class Person1(
            @JsonProperty("preName") val preName: String,
            @JsonProperty("lastName") val lastName: String
        ) {
            constructor(preNameAndLastName: String) : this(
                preNameAndLastName.substringBefore(","),
                preNameAndLastName.substringAfter(",")
            )
        }

        val objectMapper = ObjectMapper()
        val personA =
            objectMapper.readValue("""{"preName":"TestPreName","lastName":"TestLastname"}""", Person1::class.java)
        val personB = objectMapper.readValue(""""TestPreName,TestLastname"""", Person1::class.java)
    }

    @Test
    fun testPerson2() {
        class Person2(
            @JsonProperty("preName") val preName: String,
            @JsonProperty("lastName") val lastName: String
        ) {
            @JsonCreator
            constructor(preNameAndLastName: String) : this(
                preNameAndLastName.substringBefore(","),
                preNameAndLastName.substringAfter(",")
            )
        }

        val person1String = objectMapper.readValue<Person2>(""""TestPreName,TestLastname"""")
        val person1Json = objectMapper.readValue<Person2>("""{"preName":"TestPreName","lastName":"TestLastname"}""")
    }

    @Test
    fun testPerson3() {
        class Person3(val preName: String, val lastName: String) {
            @JsonCreator
            constructor(preNameAndLastName: String) : this(
                preNameAndLastName.substringBefore(","),
                preNameAndLastName.substringAfter(",")
            )
        }

        val person2String = objectMapper.readValue<Person3>(""""TestPreName,TestLastname"""")
        val person2Json = objectMapper.readValue<Person3>("""{"preName":"TestPreName","lastName":"TestLastname"}""")
    }

    @Test
    fun testPerson4() {
        class Person4(preNameAndLastName: String) {
            val preName: String
            val lastName: String

            init {
                this.preName = preNameAndLastName.substringBefore(",")
                this.lastName = preNameAndLastName.substringAfter(",")
            }
        }

        val person4String = objectMapper.readValue<Person4>(""""TestPreName,TestLastname"""")
        // person4 does not have parameter bound constructor, only string
    }

    @Test
    fun testPerson5() {
        class Person5 @JsonCreator constructor(
            @JsonProperty("preName") val preName: String,
            @JsonProperty("lastName") val lastName: String
        ) {
            @JsonCreator
            constructor(preNameAndLastName: String) :
                    this(preNameAndLastName.substringBefore(","), preNameAndLastName.substringAfter(","))
        }

        val person5String = objectMapper.readValue<Person5>(""""TestPreName,TestLastname"""")
        val person5Json = objectMapper.readValue<Person5>("""{"preName":"TestPreName","lastName":"TestLastname"}""")
    }

    // Cannot have companion object in class declared within function
    class Person6 private constructor(val preName: String, val lastName: String) {
        private constructor(preNameAndLastName: String) : this(
            preNameAndLastName.substringBefore(","),
            preNameAndLastName.substringAfter(",")
        )

        companion object {
            @JsonCreator
            @JvmStatic
            fun createFromJson(preNameAndLastName: String): Person6 {
                return Person6(preNameAndLastName)
            }

            @JsonCreator
            @JvmStatic
            fun createFromData(preName: String, lastName: String): Person6 {
                return Person6(preName, lastName)
            }
        }
    }

    @Test
    fun testPerson6() {
        val person6String = objectMapper.readValue<Person6>(""""TestPreName,TestLastname"""")
        val person6Json = objectMapper.readValue<Person6>("""{"preName":"TestPreName","lastName":"TestLastname"}""")
    }

}
