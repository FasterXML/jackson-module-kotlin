package tools.jackson.module.kotlin

import tools.jackson.core.JsonParser
import tools.jackson.databind.DatabindException
import tools.jackson.databind.DeserializationContext
import tools.jackson.databind.deser.std.StdDeserializer
import tools.jackson.databind.module.SimpleModule
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import kotlin.test.assertEquals

class ReadValuesTest {
    class MyStrDeser : StdDeserializer<String>(String::class.java) {
        override fun deserialize(
            p: JsonParser,
            ctxt: DeserializationContext
        ): String? = p.valueAsString.takeIf { it != "bar" }
    }

    @Nested
    inner class CheckTypeMismatchTest {
        val mapper = jacksonMapperBuilder().addModule(
            object : SimpleModule() {
                init {
                    addDeserializer(String::class.java, MyStrDeser())
                }
            }
        ).build()!!

        @Test
        fun readValuesJsonParserNext() {
            val src = mapper.createParser(""""foo"${"\n"}"bar"""")
            val itr = mapper.readValues<String>(src)

            assertEquals("foo", itr.next())
            assertThrows<DatabindException> {
                itr.next()
            }
        }

        @Test
        fun readValuesJsonParserNextValue() {
            val src = mapper.createParser(""""foo"${"\n"}"bar"""")
            val itr = mapper.readValues<String>(src)

            assertEquals("foo", itr.nextValue())
            assertThrows<DatabindException> {
                itr.nextValue()
            }
        }

        @Test
        fun readValuesTypedJsonParser() {
            val reader = mapper.reader()
            val src = reader.createParser(""""foo"${"\n"}"bar"""")
            val itr = reader.readValuesTyped<String>(src)

            assertEquals("foo", itr.next())
            assertThrows<DatabindException> {
                itr.next()
            }
        }
    }
}
