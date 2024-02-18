package tools.jackson.module.kotlin.kogeraIntegration.deser.valueClass.jsonCreator

import com.fasterxml.jackson.annotation.JsonCreator
import tools.jackson.module.kotlin.jacksonObjectMapper
import tools.jackson.module.kotlin.readValue
import org.junit.Assert.assertEquals
import org.junit.Test

// Test for Creator that can be handled by the Jackson mechanism.
class HandledByJacksonTest {
    @JvmInline
    value class PrimitiveMultiParamCreator(val value: Int) {
        companion object {
            // Avoiding unboxing by making the return value of Creator nullable
            @JvmStatic
            @JsonCreator
            fun creator(first: Int, second: Int): PrimitiveMultiParamCreator? =
                PrimitiveMultiParamCreator(first + second)
        }
    }

    @Test
    fun primitiveNullableCreatorTest() {
        val mapper = jacksonObjectMapper()
        val r: PrimitiveMultiParamCreator = mapper.readValue("""{"first":1,"second":2}""")
        assertEquals(PrimitiveMultiParamCreator(3), r)
    }

    @JvmInline
    value class NullableObjectMiltiParamCreator(val value: Int?) {
        companion object {
            // Avoiding unboxing by making the return value of Creator nullable
            @JvmStatic
            @JsonCreator
            fun creator(first: Int, second: Int): NullableObjectMiltiParamCreator? =
                NullableObjectMiltiParamCreator(first + second)
        }
    }

    @Test
    fun nullableObjectNullableCreatorTest() {
        val mapper = jacksonObjectMapper()
        val r: NullableObjectMiltiParamCreator = mapper.readValue("""{"first":1,"second":2}""")
        assertEquals(NullableObjectMiltiParamCreator(3), r)
    }
}
