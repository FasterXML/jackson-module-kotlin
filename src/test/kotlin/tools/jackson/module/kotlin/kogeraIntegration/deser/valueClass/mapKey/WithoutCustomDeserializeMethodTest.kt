package tools.jackson.module.kotlin.kogeraIntegration.deser.valueClass.mapKey

import tools.jackson.module.kotlin.defaultMapper
import tools.jackson.module.kotlin.readValue
import tools.jackson.module.kotlin.kogeraIntegration.deser.valueClass.NonNullObject
import tools.jackson.module.kotlin.kogeraIntegration.deser.valueClass.NullableObject
import tools.jackson.module.kotlin.kogeraIntegration.deser.valueClass.Primitive
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import tools.jackson.databind.DatabindException
import tools.jackson.databind.DeserializationContext
import tools.jackson.databind.exc.InvalidDefinitionException
import tools.jackson.databind.module.SimpleModule
import tools.jackson.module.kotlin.jacksonMapperBuilder
import java.lang.reflect.InvocationTargetException
import tools.jackson.databind.KeyDeserializer as JacksonKeyDeserializer

class WithoutCustomDeserializeMethodTest {
    companion object {
        val throwable = IllegalArgumentException("test")
    }

    @Nested
    inner class DirectDeserialize {
        @Test
        fun primitive() {
            val result = defaultMapper.readValue<Map<Primitive, String?>>("""{"1":null}""")
            assertEquals(mapOf(Primitive(1) to null), result)
        }

        @Test
        fun nonNullObject() {
            val result = defaultMapper.readValue<Map<NonNullObject, String?>>("""{"foo":null}""")
            assertEquals(mapOf(NonNullObject("foo") to null), result)
        }

        @Test
        fun nullableObject() {
            val result = defaultMapper.readValue<Map<NullableObject, String?>>("""{"bar":null}""")
            assertEquals(mapOf(NullableObject("bar") to null), result)
        }
    }

    data class Dst(
        val p: Map<Primitive, String?>,
        val nn: Map<NonNullObject, String?>,
        val n: Map<NullableObject, String?>
    )

    @Test
    fun wrapped() {
        val src = """
            {
              "p":{"1":null},
              "nn":{"foo":null},
              "n":{"bar":null}
            }
        """.trimIndent()
        val result = defaultMapper.readValue<Dst>(src)
        val expected = Dst(
            mapOf(Primitive(1) to null),
            mapOf(NonNullObject("foo") to null),
            mapOf(NullableObject("bar") to null)
        )

        assertEquals(expected, result)
    }

    @JvmInline
    value class HasCheckConstructor(val value: Int) {
        init {
            if (value < 0) throw throwable
        }
    }

    @Test
    fun callConstructorCheckTest() {
        val e = assertThrows<InvocationTargetException> {
            defaultMapper.readValue<Map<HasCheckConstructor, String?>>("""{"-1":null}""")
        }
        assertTrue(e.cause === throwable)
    }

    data class Wrapped(val first: String, val second: String) {
        class KeyDeserializer : JacksonKeyDeserializer() {
            override fun deserializeKey(key: String, ctxt: DeserializationContext) =
                key.split("-").let { Wrapped(it[0], it[1]) }
        }
    }

    @JvmInline
    value class Wrapper(val w: Wrapped)

    @Test
    fun wrappedCustomObject() {
        // If a type that cannot be deserialized is specified, the default is an error.
        val thrown = assertThrows<DatabindException> {
            defaultMapper.readValue<Map<Wrapper, String?>>("""{"foo-bar":null}""")
        }
        assertTrue(thrown.cause is InvalidDefinitionException)

        val mapper = jacksonMapperBuilder()
            .addModule(
                object : SimpleModule() {
                    init { addKeyDeserializer(Wrapped::class.java, Wrapped.KeyDeserializer()) }
                }
            )
            .build()

        val result = mapper.readValue<Map<Wrapper, String?>>("""{"foo-bar":null}""")
        val expected = mapOf(Wrapper(Wrapped("foo", "bar")) to null)

        assertEquals(expected, result)
    }
}
