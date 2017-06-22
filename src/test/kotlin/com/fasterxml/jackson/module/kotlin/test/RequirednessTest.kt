package com.fasterxml.jackson.module.kotlin.test

import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.databind.BeanDescription
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import org.junit.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class RequirednessTest {

    private val normalCasedMapper = jacksonObjectMapper()
            .configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false)
            .configure(SerializationFeature.INDENT_OUTPUT, false)

    // ==================

    private data class SimpleDataClass(
            val nonNullableField: Int,
            val nullableField: Int?
    )

    @Test fun inferIsRequiredFlagBasingOnPropertyNullity() {
        val testClass = SimpleDataClass::class.java

        "nonNullableField".isRequiredForSerializationOf(testClass)
        "nonNullableField".isRequiredForDeserializationOf(testClass)
        "nullableField".isOptionalForDeserializationOf(testClass)
        "nullableField".isOptionalForSerializationOf(testClass)
    }

    // ==================

    private data class AnnotatedConstructorParameter(
            @JsonProperty("required")
            val requiredField: Int,

            @JsonProperty("optional")
            val optionalField: Int?
    )

    @Test fun shouldInferRequirednessOfAnnotatedConstructorParametersBasingOnNullity() {
        val testClass = AnnotatedConstructorParameter::class.java

        "required".isRequiredForDeserializationOf(testClass)
        "required".isRequiredForSerializationOf(testClass)
        "optional".isOptionalForDeserializationOf(testClass)
        "optional".isOptionalForSerializationOf(testClass)
    }

    // ==================

    private data class DataClassWithGetterFunction(
            val fieldA: Int,
            val fieldB: Int?) {

        @JsonProperty("nonNullableGetter")
        fun getNonNullable(): Int = fieldA

        @JsonProperty("nullableGetter")
        fun getNullable(): Int? = fieldB
    }

    @Test fun shouldInferRequiredenessOfAnnotatedGettersBasingOnNullity() {
        val testClass = DataClassWithGetterFunction::class.java
        "nonNullableGetter".isOptionalForDeserializationOf(testClass)
        "nullableGetter".isOptionalForDeserializationOf(testClass)
        "nonNullableGetter".isRequiredForSerializationOf(testClass)
        "nullableGetter".isOptionalForSerializationOf(testClass)
    }

    // ==================

    private data class DataClassWithSetterFunction(
            val fieldA: Int,
            val fieldB: Int?) {

        @JsonProperty("nonNullableSetter")
        fun setNonNullable(value: Int): Unit {}

        @JsonProperty("nullableSetter")
        fun setNullable(value: Int?): Unit {}
    }

    @Test fun shouldInferRequiredenessOfAnnotatedSettersBasingOnNullity() {
        val testClass = DataClassWithSetterFunction::class.java
        "nonNullableSetter".isRequiredForDeserializationOf(testClass)
        "nullableSetter".isOptionalForDeserializationOf(testClass)
        "nonNullableSetter".isOptionalForSerializationOf(testClass)
        "nullableSetter".isOptionalForSerializationOf(testClass)
    }

    // ==================

    private data class DataClassWithProperties(
            val requiredField: Int,
            val optionalField: Int?
    ) {
        @JsonProperty("requiredProperty")
        val propertyA: Int = 5

        @JsonProperty("optionalProperty")
        val propertyB: Int? = optionalField?.plus(5)
    }

    @Test fun shouldInferRequiredenessOfAnnotatedPropertiesBasingOnNullity() {
        val testClass = DataClassWithProperties::class.java
        "requiredProperty".isRequiredForDeserializationOf(testClass)
        "optionalProperty".isOptionalForDeserializationOf(testClass)
        "requiredProperty".isRequiredForSerializationOf(testClass)
        "optionalProperty".isOptionalForSerializationOf(testClass)
    }

    // ==================

    private class ClassWithCombinedSettersAndGetters {
        @JsonProperty("propA")
        fun setPropertyA(value: Int): Unit {}

        @JsonProperty("propB")
        fun setPropertyB(value: Int?): Unit {}

        @JsonProperty("propA")
        fun getPropertyA(): Int? = 5

        @JsonProperty("propB")
        fun getPropertyB(): Int = 5
    }

    @Test fun shouldInferRequirednessOfAnnotatedSetterAndGetterLikeMethod() {
        val testClass = ClassWithCombinedSettersAndGetters::class.java
        "propA".isRequiredForDeserializationOf(testClass)
        "propB".isOptionalForDeserializationOf(testClass)
        "propA".isOptionalForSerializationOf(testClass)
        "propB".isRequiredForSerializationOf(testClass)
    }

    // ==================

    private fun String.isRequiredForSerializationOf(type: Class<*>): Unit {
        assertTrue("Property $this should be required for serialization!"){
            introspectSerialization(type).isRequired(this)
        }
    }

    private fun String.isRequiredForDeserializationOf(type: Class<*>): Unit {
        assertTrue("Property $this should be required for deserialization!"){
            introspectDeserialization(type).isRequired(this)
        }
    }

    private fun String.isOptionalForSerializationOf(type: Class<*>): Unit {
        assertFalse("Property $this should be optional for serialization!"){
            introspectSerialization(type).isRequired(this)
        }
    }

    private fun String.isOptionalForDeserializationOf(type: Class<*>): Unit {
        assertFalse("Property $this should be optional for deserialization of ${type.simpleName}!"){
            introspectDeserialization(type).isRequired(this)
        }
    }

    private fun introspectSerialization(type: Class<*>): BeanDescription =
            normalCasedMapper.serializationConfig.introspect(normalCasedMapper.serializationConfig.constructType(type))

    private fun introspectDeserialization(type: Class<*>): BeanDescription =
            normalCasedMapper.deserializationConfig.introspect(normalCasedMapper.deserializationConfig.constructType(type))

    private fun BeanDescription.isRequired(propertyName: String): Boolean =
            this.findProperties().find { it.name == propertyName }!!.isRequired

}