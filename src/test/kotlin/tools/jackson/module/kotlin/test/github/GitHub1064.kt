package tools.jackson.module.kotlin.test.github

import com.fasterxml.jackson.annotation.JsonSetter
import com.fasterxml.jackson.annotation.Nulls
import tools.jackson.databind.exc.InvalidNullException
import tools.jackson.module.kotlin.KotlinFeature
import tools.jackson.module.kotlin.jsonMapper
import tools.jackson.module.kotlin.kotlinModule
import tools.jackson.module.kotlin.readValue
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class GitHub1064 {
    private val mapper = jsonMapper { addModule(kotlinModule()) }

    private data class Response(
        val unwrapped: List<Map<String, Any?>>?,
        val wrapped: Map<String, List<Map<String, Any?>>>?,
        val plain: Map<String, Any?>?,
    )

    private data class NonNullMapValues(val values: List<Map<String, Any>>)
    private data class NullableListValues(val values: List<List<Int?>>)
    private data class NonNullListValues(val values: List<List<Int>>)

    private data class NullableInnerList(val values: List<List<Int>?>)
    private data class NestedArrays(val values: Array<Array<Int?>>)
    private data class SameJavaType(
        val nullable: List<List<String?>>,
        val nonNull: List<List<String>>,
    )
    private data class ExplicitSkip(
        @param:JsonSetter(contentNulls = Nulls.SKIP)
        val values: List<Map<String, Any>>,
    )

    @Test
    fun nullableMapValueInList() {
        val value = mapper.readValue<Response>("""{"unwrapped":[{"target":null}]}""")
        assertEquals(listOf(mapOf("target" to null)), value.unwrapped)
    }

    @Test
    fun nullableMapValueInWrappedList() {
        val value = mapper.readValue<Response>("""{"wrapped":{"source":[{"target":null}]}}""")
        assertEquals(mapOf("source" to listOf(mapOf("target" to null))), value.wrapped)
    }

    @Test
    fun nullableMapValue() {
        val value = mapper.readValue<Response>("""{"plain":{"target":null}}""")
        assertEquals(mapOf("target" to null), value.plain)
    }

    @Test
    fun nonNullMapInList() {
        assertFailsWith<InvalidNullException> {
            mapper.readValue<Response>("""{"unwrapped":[null]}""")
        }
    }

    @Test
    fun nonNullMapValueInList() {
        assertFailsWith<InvalidNullException> {
            mapper.readValue<NonNullMapValues>("""{"values":[{"target":null}]}""")
        }
    }

    @Test
    fun nullableListValueInList() {
        val value = mapper.readValue<NullableListValues>("""{"values":[[null]]}""")
        assertEquals(listOf(listOf(null)), value.values)
    }

    @Test
    fun nonNullListValueInList() {
        assertFailsWith<InvalidNullException> {
            mapper.readValue<NonNullListValues>("""{"values":[[null]]}""")
        }
    }

    @Test
    fun nonNullContentOfNullableInnerList() {
        val nullableInner = mapper.readValue<NullableInnerList>("""{"values":[null]}""")
        assertEquals(listOf(null), nullableInner.values)
        assertFailsWith<InvalidNullException> {
            mapper.readValue<NullableInnerList>("""{"values":[[null]]}""")
        }
    }

    @Test
    fun nullableElementInNestedArrays() {
        val value = mapper.readValue<NestedArrays>("""{"values":[[null]]}""")
        assertEquals(listOf(null), value.values.single().toList())
    }

    @Test
    fun metadataDoesNotLeakBetweenPropertiesWithTheSameJavaType() {
        val value = mapper.readValue<SameJavaType>("""{"nullable":[[null]],"nonNull":[["x"]]}""")
        assertEquals(listOf(listOf(null)), value.nullable)

        val failure = assertFailsWith<InvalidNullException> {
            mapper.readValue<SameJavaType>("""{"nullable":[[null]],"nonNull":[[null]]}""")
        }
        assertEquals("nonNull", failure.propertyName.simpleName)
    }

    @Test
    fun explicitContentNullHandlingTakesPrecedenceOverInference() {
        val value = mapper.readValue<ExplicitSkip>("""{"values":[null,{"target":null}]}""")
        assertEquals(listOf(emptyMap()), value.values)
    }

    @Test
    fun inferredStrictNullChecksTakePrecedenceOverDefaults() {
        val configuredMapper = jsonMapper {
            addModule(kotlinModule())
            changeDefaultNullHandling { it.withContentNulls(Nulls.SKIP) }
        }
        assertFailsWith<InvalidNullException> {
            configuredMapper.readValue<NonNullMapValues>("""{"values":[{"target":null}]}""")
        }
        val value = configuredMapper.readValue<Response>("""{"unwrapped":[{"target":null}]}""")
        assertEquals(listOf(emptyMap<String, Any?>()), value.unwrapped)
    }

    @Test
    fun disablingStrictNullChecksDisablesNestedInference() {
        val laxMapper = jsonMapper {
            addModule(kotlinModule { disable(KotlinFeature.StrictNullChecks) })
        }
        val value = laxMapper.readValue<NonNullMapValues>("""{"values":[{"target":null}]}""")
        assertEquals(listOf<Map<String, Any?>>(mapOf("target" to null)), value.values)
    }

    @Test
    fun explicitContentNullsInJavaBeanStillApplyAtBothLevels() {
        for (javaMapper in listOf(jsonMapper(), mapper)) {
            assertFailsWith<InvalidNullException> {
                javaMapper.readValue<GitHub1064ExplicitContentNulls>("""{"values":[{"target":null}]}""")
            }
        }
    }
}
