package com.fasterxml.jackson.module.kotlin.test

import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.databind.BeanDescription
import com.fasterxml.jackson.databind.BeanProperty
import com.fasterxml.jackson.databind.DeserializationConfig
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.JsonDeserializer
import com.fasterxml.jackson.databind.deser.BeanDeserializerModifier
import com.fasterxml.jackson.databind.deser.ContextualDeserializer
import com.fasterxml.jackson.databind.module.SimpleModule
import com.fasterxml.jackson.databind.type.CollectionType
import com.fasterxml.jackson.module.kotlin.InjectsDefaultValue
import com.fasterxml.jackson.module.kotlin.MissingKotlinParameterException
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import org.junit.Test
import kotlin.test.assertEquals


class InjectsDefaultValueTests {

    private data class TestClass(@get:InjectsDefaultValue val foo: List<Int>)

    @Test
    fun nonNullCaseStillWorks() {
        val mapper = jacksonObjectMapper()
        assertEquals(listOf(1,2), mapper.readValue("""{"foo": [1,2]}""", TestClass::class.java).foo)
    }

    @Test
    fun shouldAllowForInjectingDefaultValuesWhenNull() {
        val mapper = jacksonObjectMapper()
        mapper.registerModule(NullAwareDeserializationModule())
        assertEquals(emptyList(), mapper.readValue("{}", TestClass::class.java).foo)
        assertEquals(emptyList(), mapper.readValue("""{"foo": null}""", TestClass::class.java).foo)

    }

    @Test(expected = MissingKotlinParameterException::class)
    fun shouldThrowIfDefaultValueIsNotInjected() {
        val mapper = jacksonObjectMapper()
        mapper.readValue("""{"foo": null}""", TestClass::class.java)
    }

    class NullAwareDeserializationModule: SimpleModule() {
        init {
            @Suppress("UNCHECKED_CAST")
            setDeserializerModifier(object: BeanDeserializerModifier() {
                override fun modifyCollectionDeserializer(config: DeserializationConfig?, type: CollectionType?, beanDesc: BeanDescription?, deserializer: JsonDeserializer<*>): JsonDeserializer<*>? {
                    return object : JsonDeserializer<Collection<Any>>(), ContextualDeserializer {
                        override fun deserialize(jp: JsonParser, ctx: DeserializationContext?): Collection<Any>? {
                            return deserializer.deserialize(jp, ctx) as Collection<Any>?
                        }

                        override fun createContextual(ctx: DeserializationContext?, property: BeanProperty?): JsonDeserializer<*>? {
                            val contextualDeserializer = (deserializer as ContextualDeserializer).createContextual(ctx, property)
                            return modifyCollectionDeserializer(config, type, beanDesc, contextualDeserializer)
                        }

                        override fun getNullValue(ctx: DeserializationContext?): Collection<Any>? {
                            return when {
                                type?.isTypeOrSubTypeOf(Set::class.java) == true -> emptySet()
                                type?.isTypeOrSubTypeOf(List::class.java) == true -> emptyList()
                                else -> throw UnsupportedOperationException("Unexpected collection type value: $type")
                            }
                        }
                    }
                }
            })
        }
    }
}
