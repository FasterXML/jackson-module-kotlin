package com.fasterxml.jackson.module.kotlin.test.github

import com.fasterxml.jackson.databind.BeanDescription
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.KotlinFeature
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import kotlin.test.Test
import kotlin.test.assertTrue

class GitHub922 {
    private inline fun <reified T : Any> ObjectMapper.introspectSerialization(): BeanDescription =
        serializationConfig.introspect(serializationConfig.constructType(T::class.java))

    private inline fun <reified T : Any> ObjectMapper.introspectDeserialization(): BeanDescription =
        deserializationConfig.introspect(deserializationConfig.constructType(T::class.java))

    private fun BeanDescription.isRequired(propertyName: String): Boolean =
        this.findProperties().first { it.name == propertyName }.isRequired

    @Test
    fun `nullToEmpty does not override specification by Java annotation`() {
        val mapper = jacksonObjectMapper {
            enable(KotlinFeature.NullToEmptyCollection)
            enable(KotlinFeature.NullToEmptyMap)
        }

        val desc = mapper.introspectDeserialization<GitHub922RequiredCollectionsDtoJava>()

        assertTrue(desc.isRequired("list"))
        assertTrue(desc.isRequired("map"))
    }
}
