package com.fasterxml.jackson.module.kotlin.test.requiredness

import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.databind.BeanDescription
import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import org.junit.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class BaseTests {

    private val primitivesDefaultsMapper = jacksonObjectMapper()
            .configure(DeserializationFeature.FAIL_ON_NULL_FOR_PRIMITIVES, false)

    private val noPrimitiveDefaultsMapper = jacksonObjectMapper()
            .configure(DeserializationFeature.FAIL_ON_NULL_FOR_PRIMITIVES, true)

    // ==================

    private data class SimpleDataClass(
            val nonNullableField: Int,
            val nullableField: Int?
    )

    @Test fun inferIsRequiredFlagBasingOnPropertyNullity() {
        val testClass = SimpleDataClass::class.java

//        "nonNullableField".isRequiredForSerializationOf(testClass, primitivesDefaultsMapper)
        "nonNullableField".isOptionalForDeserializationOf(testClass, primitivesDefaultsMapper)

//        "nullableField".isOptionalForDeserializationOf(testClass, primitivesDefaultsMapper)
//        "nullableField".isOptionalForSerializationOf(testClass, primitivesDefaultsMapper)
    }

    @Test fun inferIsRequiredFlagBasingOnPropertyNullityDisallowPrimitivesDefaults() {
        val testClass = SimpleDataClass::class.java

        "nonNullableField".isRequiredForSerializationOf(testClass, noPrimitiveDefaultsMapper)
        "nonNullableField".isRequiredForDeserializationOf(testClass, noPrimitiveDefaultsMapper)
        "nullableField".isOptionalForDeserializationOf(testClass, noPrimitiveDefaultsMapper)
        "nullableField".isOptionalForSerializationOf(testClass, noPrimitiveDefaultsMapper)
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

    private data class T(val abc: Int=0)

    private data class ClassWithPrimitivesWithDefaults(val i: Int = 5, val x: Int, val t: T, val k: Int? = 3)

    @Test fun XYZshouldInferRequirednessOfClassWithPrimitivesWithDefaults() {
        val testClass = ClassWithPrimitivesWithDefaults::class.java
        "i".isOptionalForDeserializationOf(testClass)
        "x".isRequiredForDeserializationOf(testClass)
        "k".isOptionalForDeserializationOf(testClass)
        "i".isRequiredForSerializationOf(testClass)
        "x".isRequiredForSerializationOf(testClass)
        "k".isOptionalForSerializationOf(testClass)
    }

    @Test fun SshouldInferRequirednessOfClassWithPrimitivesWithDefaults() {
        val testClass = ClassWithPrimitivesWithDefaults::class.java
        "t".isRequiredForDeserializationOf(testClass)
    }

    @Test fun DEshouldInferRequirednessOfClassWithPrimitivesWithDefaults() {
        val testClass = ClassWithPrimitivesWithDefaults::class.java
        "t".isRequiredForSerializationOf(testClass)
    }
    // ==================



    private fun String.isRequiredForSerializationOf(type: Class<*>, mapper: ObjectMapper = noPrimitiveDefaultsMapper): Unit {
        assertTrue("Property $this should be required for serialization!"){
            introspectSerialization(type, mapper).isRequired(this)
        }
    }

    private fun String.isRequiredForDeserializationOf(type: Class<*>, mapper: ObjectMapper = noPrimitiveDefaultsMapper): Unit {
        assertTrue("Property $this should be required for deserialization!"){
            introspectDeserialization(type, mapper).isRequired(this)
        }
    }

    private fun String.isOptionalForSerializationOf(type: Class<*>, mapper: ObjectMapper = noPrimitiveDefaultsMapper): Unit {
        assertFalse("Property $this should be optional for serialization!"){
            introspectSerialization(type, mapper).isRequired(this)
        }
    }

    private fun String.isOptionalForDeserializationOf(type: Class<*>, mapper: ObjectMapper = noPrimitiveDefaultsMapper): Unit {
        assertFalse("Property $this should be optional for deserialization of ${type.simpleName}!"){
            introspectDeserialization(type, mapper).isRequired(this)
        }
    }

    private fun introspectSerialization(type: Class<*>, mapper: ObjectMapper): BeanDescription =
            mapper.serializationConfig.introspect(mapper.serializationConfig.constructType(type))

    private fun introspectDeserialization(type: Class<*>, mapper: ObjectMapper): BeanDescription =
            mapper.deserializationConfig.introspect(mapper.deserializationConfig.constructType(type))

    private fun BeanDescription.isRequired(propertyName: String): Boolean =
            this.findProperties().find { it.name == propertyName }!!.isRequired

}