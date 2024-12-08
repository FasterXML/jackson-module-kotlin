package tools.jackson.module.kotlin.test.github

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonInclude
import tools.jackson.module.kotlin.jsonMapper
import tools.jackson.module.kotlin.kotlinModule
import tools.jackson.module.kotlin.readValue
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

class GitHub841 {
    object Foo {
        override fun toString(): String = "Foo()"

        @JvmStatic
        @JsonCreator
        fun deserialize(): Foo {
            return Foo
        }
    }

    private val mapper = jsonMapper {
        changeDefaultPropertyInclusion { it.withValueInclusion(JsonInclude.Include.NON_ABSENT) }
        addModule(kotlinModule())
    }

    @Test
    fun shouldDeserializeSimpleObject() {
        val value = Foo
        val serialized = mapper.writeValueAsString(value)
        val deserialized = mapper.readValue<Foo>(serialized)

        assertEquals(value, deserialized)
    }
}
