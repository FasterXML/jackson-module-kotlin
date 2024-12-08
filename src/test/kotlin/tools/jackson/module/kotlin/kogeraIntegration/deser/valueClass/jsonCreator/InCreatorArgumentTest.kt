package tools.jackson.module.kotlin.kogeraIntegration.deser.valueClass.jsonCreator

import com.fasterxml.jackson.annotation.JsonCreator
import tools.jackson.module.kotlin.jacksonObjectMapper
import tools.jackson.module.kotlin.kogeraIntegration.deser.valueClass.NonNullObject
import tools.jackson.module.kotlin.kogeraIntegration.deser.valueClass.NullableObject
import tools.jackson.module.kotlin.kogeraIntegration.deser.valueClass.Primitive
import tools.jackson.module.kotlin.readValue
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

private fun Primitive.modify(): Primitive = Primitive(v + 100)
private fun NonNullObject.modify(): NonNullObject = NonNullObject("$v-creator")
private fun NullableObject.modify(): NullableObject = NullableObject(v!! + "-creator")

class InCreatorArgumentTest {
    data class Dst(
        val pNn: Primitive,
        val pN: Primitive?,
        val nnoNn: NonNullObject,
        val nnoN: NonNullObject?,
        val noNn: NullableObject,
        val noN: NullableObject?
    ) {
        companion object {
            @JvmStatic
            @JsonCreator
            fun creator(
                pNn: Primitive,
                pN: Primitive?,
                nnoNn: NonNullObject,
                nnoN: NonNullObject?,
                noNn: NullableObject,
                noN: NullableObject?
            ) = Dst(
                pNn.modify(),
                pN?.modify(),
                nnoNn.modify(),
                nnoN?.modify(),
                noNn.modify(),
                noN?.modify()
            )
        }
    }

    @Test
    fun test() {
        val mapper = jacksonObjectMapper()
        val base = Dst(
            Primitive(1),
            Primitive(2),
            NonNullObject("nnoNn"),
            NonNullObject("nnoN"),
            NullableObject("noNn"),
            NullableObject("noN")
        )
        val result = mapper.readValue<Dst>(mapper.writeValueAsString(base))

        assertEquals(
            base.copy(
                pNn = base.pNn.modify(),
                pN = base.pN?.modify(),
                nnoNn = base.nnoNn.modify(),
                nnoN = base.nnoN?.modify(),
                noNn = base.noNn.modify(),
                noN = base.noN?.modify()
            ),
            result
        )
    }
}
