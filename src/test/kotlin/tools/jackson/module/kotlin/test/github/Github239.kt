package tools.jackson.module.kotlin.test.github

import tools.jackson.annotation.JsonSubTypes
import tools.jackson.annotation.JsonTypeInfo
import tools.jackson.annotation.JsonTypeName
import tools.jackson.databind.ObjectMapper
import tools.jackson.module.kotlin.kotlinModule
import tools.jackson.module.kotlin.readValue
import org.junit.Test
import kotlin.test.assertEquals


class TestGithub239 {

    @JsonTypeInfo(use = JsonTypeInfo.Id.NAME)
    sealed class Github239Either {

        @JsonTypeName("a")
        data class A(var field: String = "") : Github239Either()

        @JsonTypeName("b")
        data class B(var otherField: String = "") : Github239Either()

    }

    @JsonTypeInfo(use = JsonTypeInfo.Id.NAME)
    @JsonSubTypes(
            JsonSubTypes.Type(Github239EitherCustomized.A::class, name = "a"),
            JsonSubTypes.Type(Github239EitherCustomized.B::class, name = "b")
    )
    sealed class Github239EitherCustomized {

        data class A(var field: String = "") : Github239EitherCustomized()

        data class B(var otherField: String = "") : Github239EitherCustomized()

    }

    val json = """[
        {
            "@type": "a",
            "field": "value"
        },
        {
            "@type": "b",
            "otherField": "1234"
        }
    ]"""

    val mapper = ObjectMapper()
        .registerModule(_root_ide_package_.tools.jackson.module.kotlin.kotlinModule())

    @Test
    fun test_implicit_subclasses() {

        val array = mapper.readValue<Array<Github239Either>>(json)

        assertEquals(2, array.size)
        assertEquals(Github239Either.A("value"), array[0])
        assertEquals(Github239Either.B("1234"), array[1])

    }

    @Test
    fun test_explicit_subclasses() {

        val array = mapper.readValue<Array<Github239EitherCustomized>>(json)

        assertEquals(2, array.size)
        assertEquals(Github239EitherCustomized.A("value"), array[0])
        assertEquals(Github239EitherCustomized.B("1234"), array[1])

    }
}
