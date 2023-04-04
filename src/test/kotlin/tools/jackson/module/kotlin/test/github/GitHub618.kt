package tools.jackson.module.kotlin.test.github

import org.junit.Test
import tools.jackson.core.JsonGenerator
import tools.jackson.databind.SerializerProvider
import tools.jackson.databind.annotation.JsonSerialize
import tools.jackson.databind.ser.std.StdSerializer
import tools.jackson.module.kotlin.jacksonObjectMapper
import kotlin.test.assertEquals

class GitHub618 {
    @JsonSerialize(using = V.Serializer::class)
    @JvmInline
    value class V(val value: String) {
        class Serializer : StdSerializer<V>(V::class.java) {
            override fun serialize(p0: V, p1: JsonGenerator, p2: SerializerProvider) {
                p1.writeString(p0.toString())
            }
        }
    }

    data class D(val v: V?)

    @Test
    fun test() {
        val mapper = jacksonObjectMapper()
        // expected: {"v":null}, but NullPointerException thrown
        assertEquals("""{"v":null}""", mapper.writeValueAsString(D(null)))
    }
}
