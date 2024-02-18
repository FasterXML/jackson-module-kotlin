package tools.jackson.module.kotlin.kogeraIntegration.deser.valueClass.defaultArgument

import com.fasterxml.jackson.annotation.JsonCreator
import tools.jackson.module.kotlin.jacksonObjectMapper
import tools.jackson.module.kotlin.kogeraIntegration.deser.valueClass.NonNullObject
import tools.jackson.module.kotlin.readValue
import org.junit.Assert.assertEquals
import org.junit.Test

class NonNullObjectTest {
    companion object {
        val mapper = jacksonObjectMapper()
    }

    data class ByConstructor(
        val nn: NonNullObject = NonNullObject("foo"),
        val nNn: NonNullObject? = NonNullObject("bar"),
        val nN: NonNullObject? = null
    )

    @Test
    fun byConstructorTest() {
        assertEquals(ByConstructor(), mapper.readValue<ByConstructor>("{}"))
    }

    data class ByFactory(val nn: NonNullObject, val nNn: NonNullObject?, val nN: NonNullObject?) {
        companion object {
            @JvmStatic
            @JsonCreator
            fun creator(
                nn: NonNullObject = NonNullObject("foo"),
                nNn: NonNullObject? = NonNullObject("bar"),
                nN: NonNullObject? = null
            ) = ByFactory(nn, nNn, nN)
        }
    }

    @Test
    fun byFactoryTest() {
        assertEquals(ByFactory.creator(), mapper.readValue<ByFactory>("{}"))
    }
}
