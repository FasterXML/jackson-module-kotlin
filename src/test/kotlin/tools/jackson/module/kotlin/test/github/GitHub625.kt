package tools.jackson.module.kotlin.test.github

import com.fasterxml.jackson.annotation.JsonInclude
import tools.jackson.module.kotlin.jacksonObjectMapper
import tools.jackson.module.kotlin.testPrettyWriter
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals

class GitHub625 {
    @JvmInline
    value class Primitive(val v: Int)

    @JvmInline
    value class NonNullObject(val v: String)

    @JvmInline
    value class NullableObject(val v: String?)

    @JsonInclude(value = JsonInclude.Include.NON_NULL, content = JsonInclude.Include.NON_NULL)
    data class Dto(
        val primitive: Primitive? = null,
        val nonNullObject: NonNullObject? = null,
        val nullableObject: NullableObject? = null
    ) {
        fun getPrimitiveGetter(): Primitive? = null
        fun getNonNullObjectGetter(): NonNullObject? = null
        fun getNullableObjectGetter(): NullableObject? = null
    }

    @Test
    fun test() {
        val mapper = jacksonObjectMapper()
        val dto = Dto()
        assertEquals("{}", mapper.writeValueAsString(dto))
    }

    @JsonInclude(value = JsonInclude.Include.NON_EMPTY, content = JsonInclude.Include.NON_NULL)
    data class FailingDto(
        val nullableObject1: NullableObject = NullableObject(null),
        val nullableObject2: NullableObject? = NullableObject(null),
        val map: Map<Any, Any?> = mapOf("nullableObject" to NullableObject(null),)
    ) {
        fun getNullableObjectGetter1(): NullableObject = NullableObject(null)
        fun getNullableObjectGetter2(): NullableObject? = NullableObject(null)
        fun getMapGetter(): Map<Any, Any?> = mapOf("nullableObject" to NullableObject(null))
    }

    @Test
    fun failing() {
        val writer = jacksonObjectMapper().testPrettyWriter()
        val json = writer.writeValueAsString(FailingDto())

        assertNotEquals("{ }", json)
        assertEquals(
            """
                {
                  "nullableObject1" : null,
                  "nullableObject2" : null,
                  "map" : {
                    "nullableObject" : null
                  },
                  "mapGetter" : {
                    "nullableObject" : null
                  },
                  "nullableObjectGetter2" : null,
                  "nullableObjectGetter1" : null
                }
            """.trimIndent(),
            json
        )
    }
}
