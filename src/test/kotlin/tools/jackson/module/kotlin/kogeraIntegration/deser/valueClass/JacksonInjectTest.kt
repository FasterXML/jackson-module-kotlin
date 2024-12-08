package tools.jackson.module.kotlin.kogeraIntegration.deser.valueClass

import com.fasterxml.jackson.annotation.JacksonInject
import tools.jackson.databind.InjectableValues
import tools.jackson.databind.MapperFeature
import tools.jackson.module.kotlin.jacksonMapperBuilder
import tools.jackson.module.kotlin.jacksonObjectMapper
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test

class JacksonInjectTest {
    // This is specified as a getter because there is a possibility of problems if it is assigned to a field.
    // see: https://github.com/FasterXML/jackson-databind/issues/4218
    data class Dto(
        @get:JacksonInject("pNn")
        val pNn: Primitive,
        @get:JacksonInject("pN")
        val pN: Primitive?,
        @get:JacksonInject("nnoNn")
        val nnoNn: NonNullObject,
        @get:JacksonInject("nnoN")
        val nnoN: NonNullObject?,
        @get:JacksonInject("noNnNn")
        val noNnNn: NullableObject,
        @get:JacksonInject("noNnN")
        val noNnN: NullableObject,
        @get:JacksonInject("noNNn")
        val noNNn: NullableObject?,
        @get:JacksonInject("noNN")
        val noNN: NullableObject?
    )

    @Test
    fun test() {
        val injectables = InjectableValues.Std(
            mapOf(
                "pNn" to Primitive(0),
                "pN" to Primitive(1),
                "nnoNn" to NonNullObject("nnoNn"),
                "nnoN" to NonNullObject("nnoN"),
                "noNnNn" to NullableObject("noNnNn"),
                "noNnN" to NullableObject(null),
                "noNNn" to NullableObject("noNNn"),
                "noNN" to NullableObject(null)
            )
        )

        val reader = jacksonObjectMapper()
            .readerFor(Dto::class.java)
            .with(injectables)

        println(reader.readValue<Dto>("{}"))
    }

    data class DataBind4218FailingDto(
        @field:JacksonInject("pNn")
        val pNn: Primitive,
        @field:JacksonInject("pN")
        val pN: Primitive?
    )

    // remove if fixed
    @Test
    fun dataBind4218Failing() {
        val injectables = InjectableValues.Std(mapOf("pNn" to Primitive(0), "pN" to Primitive(1)))

        val reader = jacksonMapperBuilder()
            .enable(MapperFeature.ALLOW_FINAL_FIELDS_AS_MUTATORS)
            .build()
            .readerFor(DataBind4218FailingDto::class.java)
            .with(injectables)

        val ex = assertThrows(IllegalArgumentException::class.java) { reader.readValue<DataBind4218FailingDto>("{}") }
        assertEquals(
            "Can not set final int field tools.jackson.module.kotlin.kogeraIntegration.deser.valueClass.JacksonInjectTest\$DataBind4218FailingDto.pNn to tools.jackson.module.kotlin.kogeraIntegration.deser.valueClass.Primitive",
            ex.message
        )
    }
}
