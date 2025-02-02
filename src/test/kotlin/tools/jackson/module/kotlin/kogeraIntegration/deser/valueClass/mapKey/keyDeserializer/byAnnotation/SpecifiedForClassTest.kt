package tools.jackson.module.kotlin.kogeraIntegration.deser.valueClass.mapKey.keyDeserializer.byAnnotation

import tools.jackson.databind.DeserializationContext
import tools.jackson.module.kotlin.defaultMapper
import tools.jackson.module.kotlin.jacksonObjectMapper
import tools.jackson.module.kotlin.readValue
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import tools.jackson.databind.annotation.JsonDeserialize
import tools.jackson.databind.KeyDeserializer as JacksonKeyDeserializer

class SpecifiedForClassTest {
    @JsonDeserialize(keyUsing = Value.KeyDeserializer::class)
    @JvmInline
    value class Value(val v: Int) {
        class KeyDeserializer : JacksonKeyDeserializer() {
            override fun deserializeKey(key: String, ctxt: DeserializationContext) = Value(key.toInt() + 100)
        }
    }

    @Test
    fun directDeserTest() {
        val result = defaultMapper.readValue<Map<Value, String?>>("""{"1":null}""")

        assertEquals(mapOf(Value(101) to null), result)
    }

    data class Wrapper(val v: Map<Value, String?>)

    @Test
    fun paramDeserTest() {
        val mapper = jacksonObjectMapper()
        val result = mapper.readValue<Wrapper>("""{"v":{"1":null}}""")

        assertEquals(Wrapper(mapOf(Value(101) to null)), result)
    }
}
