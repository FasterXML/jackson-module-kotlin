package tools.jackson.module.kotlin.test.github

import tools.jackson.databind.BeanDescription
import tools.jackson.databind.ObjectMapper
import tools.jackson.module.kotlin.KotlinFeature
import tools.jackson.module.kotlin.jacksonObjectMapper
import kotlin.test.Test
import kotlin.test.assertTrue

class GitHub922 {
    private inline fun <reified T : Any> ObjectMapper.introspectSerialization(): BeanDescription =
        _serializationContext().introspectBeanDescription(_serializationContext().constructType(T::class.java))

    private inline fun <reified T : Any> ObjectMapper.introspectDeserialization(): BeanDescription =
        _deserializationContext().introspectBeanDescription(_deserializationContext().constructType(T::class.java))

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
