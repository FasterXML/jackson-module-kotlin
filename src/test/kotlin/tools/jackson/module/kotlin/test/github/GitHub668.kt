package tools.jackson.module.kotlin.test.github

import com.fasterxml.jackson.annotation.JsonProperty
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import tools.jackson.databind.BeanDescription
import tools.jackson.databind.ObjectMapper
import tools.jackson.module.kotlin.defaultMapper
import tools.jackson.module.kotlin.jacksonObjectMapper

class GitHub668 {
    private inline fun <reified T : Any> ObjectMapper.introspectDeserialization(): BeanDescription =
        _deserializationContext().introspectBeanDescription(_deserializationContext().constructType(T::class.java))

    private fun BeanDescription.isRequired(propertyName: String): Boolean =
        this.findProperties().find { it.name == propertyName }?.isRequired ?: false

    data class AffectByAccessor(
        @get:JsonProperty(required = true)
        var a: String?,
        @field:JsonProperty(required = true)
        var b: String?,
        @set:JsonProperty(required = true)
        var c: String?
    ) {
        @get:JsonProperty(required = true)
        var x: String? = null
        @field:JsonProperty(required = true)
        var y: String? = null
        @JvmField
        @field:JsonProperty(required = true)
        var z: String? = null
    }

    @Test
    fun affectByAccessorTestDeser() {
        val desc = defaultMapper.introspectDeserialization<AffectByAccessor>()

        assertTrue(desc.isRequired("a"))
        assertTrue(desc.isRequired("b"))
        assertTrue(desc.isRequired("c"))
        assertTrue(desc.isRequired("x"))
        assertTrue(desc.isRequired("y"))
        assertTrue(desc.isRequired("z"))
    }
}
