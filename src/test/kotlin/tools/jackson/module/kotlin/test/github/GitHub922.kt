package tools.jackson.module.kotlin.test.github

import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.annotation.OptBoolean
import tools.jackson.databind.BeanDescription
import tools.jackson.databind.ObjectMapper
import tools.jackson.module.kotlin.KotlinFeature
import tools.jackson.module.kotlin.defaultMapper
import tools.jackson.module.kotlin.jacksonObjectMapper
import kotlin.reflect.full.memberProperties
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class GitHub922 {
    companion object {
        val nullToEmptyMapper = jacksonObjectMapper {
            enable(KotlinFeature.NullToEmptyCollection)
            enable(KotlinFeature.NullToEmptyMap)
        }
    }

    private inline fun <reified T : Any> ObjectMapper.introspectSerialization(): BeanDescription =
        _serializationContext().introspectBeanDescription(_serializationContext().constructType(T::class.java))

    private inline fun <reified T : Any> ObjectMapper.introspectDeserialization(): BeanDescription =
        _deserializationContext().introspectBeanDescription(_deserializationContext().constructType(T::class.java))

    private fun BeanDescription.isRequired(propertyName: String): Boolean =
        this.findProperties().first { it.name == propertyName }.isRequired

    @Test
    fun `nullToEmpty does not override specification by Java annotation`() {
        val defaultDesc = defaultMapper.introspectDeserialization<GitHub922RequiredCollectionsDtoJava>()

        assertTrue(defaultDesc.isRequired("list"))
        assertTrue(defaultDesc.isRequired("map"))

        val nullToEmptyDesc = nullToEmptyMapper.introspectDeserialization<GitHub922RequiredCollectionsDtoJava>()

        assertTrue(nullToEmptyDesc.isRequired("list"))
        assertTrue(nullToEmptyDesc.isRequired("map"))
    }

    data class RequiredCollectionsDto1(
        @JsonProperty(required = true) val list: List<String>,
        @JsonProperty(required = true) val map: Map<String, String>
    )

    data class RequiredCollectionsDto2(
        @JsonProperty(isRequired = OptBoolean.TRUE) val list: List<String>,
        @JsonProperty(isRequired = OptBoolean.TRUE) val map: Map<String, String>
    )

    @Test
    fun `nullToEmpty does not override specification by annotation`() {
        val defaultDesc1 = defaultMapper.introspectDeserialization<RequiredCollectionsDto1>()

        assertTrue(defaultDesc1.isRequired("list"))
        assertTrue(defaultDesc1.isRequired("map"))

        val nullToEmptyDesc1 = nullToEmptyMapper.introspectDeserialization<RequiredCollectionsDto1>()

        assertTrue(nullToEmptyDesc1.isRequired("list"))
        assertTrue(nullToEmptyDesc1.isRequired("map"))

        val defaultDesc2 = defaultMapper.introspectDeserialization<RequiredCollectionsDto2>()

        assertTrue(defaultDesc2.isRequired("list"))
        assertTrue(defaultDesc2.isRequired("map"))

        val nullToEmptyDesc2 = nullToEmptyMapper.introspectDeserialization<RequiredCollectionsDto2>()

        assertTrue(nullToEmptyDesc2.isRequired("list"))
        assertTrue(nullToEmptyDesc2.isRequired("map"))
    }

    data class CollectionsDto(val list: List<String>, val map: Map<String, String>)

    @Test
    fun `nullToEmpty does not affect for serialization`() {
        val defaultDesc = defaultMapper.introspectSerialization<CollectionsDto>()

        assertTrue(defaultDesc.isRequired("list"))
        assertTrue(defaultDesc.isRequired("map"))

        val nullToEmptyDesc = nullToEmptyMapper.introspectSerialization<CollectionsDto>()

        assertTrue(nullToEmptyDesc.isRequired("list"))
        assertTrue(nullToEmptyDesc.isRequired("map"))
    }

    class SetterCollectionsDto {
        lateinit var list: List<String>
        lateinit var map: Map<String, String>
    }

    @Test
    fun `nullToEmpty does not affect for setter`() {
        val defaultDesc = defaultMapper.introspectDeserialization<SetterCollectionsDto>()

        assertTrue(defaultDesc.isRequired("list"))
        assertTrue(defaultDesc.isRequired("map"))

        val nullToEmptyDesc = nullToEmptyMapper.introspectDeserialization<SetterCollectionsDto>()

        assertTrue(nullToEmptyDesc.isRequired("list"))
        assertTrue(nullToEmptyDesc.isRequired("map"))
    }

    class FieldCollectionsDto {
        @JvmField
        var list: List<String> = emptyList()
        @JvmField
        var map: Map<String, String> = emptyMap()
    }

    @Test
    fun `nullToEmpty does not affect for field`() {
        val defaultDesc = defaultMapper.introspectDeserialization<FieldCollectionsDto>()

        assertTrue(defaultDesc.isRequired("list"))
        assertTrue(defaultDesc.isRequired("map"))

        val nullToEmptyDesc = nullToEmptyMapper.introspectDeserialization<FieldCollectionsDto>()

        assertTrue(nullToEmptyDesc.isRequired("list"))
        assertTrue(nullToEmptyDesc.isRequired("map"))
    }

    // isRequired_required_nullability_expected
    @Suppress("PropertyName")
    data class IsRequiredDto(
        // region: isRequired takes precedence
        @JsonProperty(isRequired = OptBoolean.FALSE, required = false)
        val FALSE_false_nullable_false: String?,
        @JsonProperty(isRequired = OptBoolean.FALSE, required = false)
        val FALSE_false_nonNull_false: String,
        @JsonProperty(isRequired = OptBoolean.FALSE, required = true)
        val FALSE_true_nullable_false: String?,
        @JsonProperty(isRequired = OptBoolean.FALSE, required = true)
        val FALSE_true_nonNull_false: String,
        @JsonProperty(isRequired = OptBoolean.TRUE, required = false)
        val TRUE_false_nullable_true: String?,
        @JsonProperty(isRequired = OptBoolean.TRUE, required = false)
        val TRUE_false_nonNull_true: String,
        @JsonProperty(isRequired = OptBoolean.TRUE, required = true)
        val TRUE_true_nullable_true: String?,
        @JsonProperty(isRequired = OptBoolean.TRUE, required = true)
        val TRUE_true_nonNull_true: String,
        // endregion
        // region: If isRequired is the default, only overrides by required = true will work.
        @JsonProperty(isRequired = OptBoolean.DEFAULT, required = false)
        val DEFAULT_false_nullable_false: String?,
        @JsonProperty(isRequired = OptBoolean.DEFAULT, required = false)
        val DEFAULT_false_nonNull_true: String,
        @JsonProperty(isRequired = OptBoolean.DEFAULT, required = true)
        val DEFAULT_true_nullable_true: String?,
        @JsonProperty(isRequired = OptBoolean.DEFAULT, required = true)
        val DEFAULT_true_nonNull_true: String,
        // endregion
    )

    @Test
    fun `JsonProperty properly overrides required`() {
        val desc = defaultMapper.introspectDeserialization<IsRequiredDto>()

        IsRequiredDto::class.memberProperties.forEach { prop ->
            val name = prop.name
            val expected = name.split("_").last().toBoolean()

            assertEquals(expected, desc.isRequired(name), name)
        }
    }
}
