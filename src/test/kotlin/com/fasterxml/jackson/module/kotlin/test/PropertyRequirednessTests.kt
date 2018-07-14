package com.fasterxml.jackson.module.kotlin.test

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.databind.BeanDescription
import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import org.junit.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue


class PropertyRequirednessTests {

    private data class TestParamClass(val foo: String = "bar")

    private class TestClass {
        fun setA(value: Int): Unit {}
        fun setB(value: Int = 5): Unit {}
        fun setC(value: Int?): Unit {}
        fun setD(value: Int? = 5): Unit {}

        fun getE(): Int = 5
        fun getF(): Int? = 5

        val g: Int = 5
        val h: Int? = 5

        fun setI(value: TestParamClass): Unit {}
        fun setJ(value: TestParamClass = TestParamClass()): Unit {}
        fun setK(value: TestParamClass?): Unit {}
        fun setL(value: TestParamClass? = TestParamClass()): Unit {}
    }

    @Test fun shouldHandleFalseFailOnNullForPrimitives() {
        val mapper = jacksonObjectMapper().configure(DeserializationFeature.FAIL_ON_NULL_FOR_PRIMITIVES, false)
        val testClass = TestClass::class.java
        "a".isOptionalForDeserializationOf(testClass, mapper)
        "b".isOptionalForDeserializationOf(testClass, mapper)
        "c".isOptionalForDeserializationOf(testClass, mapper)
        "d".isOptionalForDeserializationOf(testClass, mapper)

        "e".isRequiredForSerializationOf(testClass, mapper)
        "f".isOptionalForSerializationOf(testClass, mapper)

        "g".isRequiredForDeserializationOf(testClass, mapper)
        "g".isRequiredForSerializationOf(testClass, mapper)

        "h".isOptionalForSerializationOf(testClass, mapper)
        "h".isOptionalForDeserializationOf(testClass, mapper)

        "i".isRequiredForDeserializationOf(testClass, mapper)
        "j".isOptionalForDeserializationOf(testClass, mapper)
        "k".isOptionalForDeserializationOf(testClass, mapper)
        "l".isOptionalForDeserializationOf(testClass, mapper)
    }

    @Test fun shouldHandleTrueFailOnNullForPrimitives() {
        val mapper = jacksonObjectMapper().configure(DeserializationFeature.FAIL_ON_NULL_FOR_PRIMITIVES, true)
        val testClass = TestClass::class.java
        "a".isRequiredForDeserializationOf(testClass, mapper)
        "b".isOptionalForDeserializationOf(testClass, mapper)
        "c".isOptionalForDeserializationOf(testClass, mapper)
        "d".isOptionalForDeserializationOf(testClass, mapper)

        "g".isRequiredForDeserializationOf(testClass, mapper)

        "h".isOptionalForDeserializationOf(testClass, mapper)

        "i".isRequiredForDeserializationOf(testClass, mapper)
        "j".isOptionalForDeserializationOf(testClass, mapper)
        "k".isOptionalForDeserializationOf(testClass, mapper)
        "l".isOptionalForDeserializationOf(testClass, mapper)
    }

    // ---

    private data class TestDataClass(
            val a: Int,
            val b: Int?,
            val c: Int = 5,
            val d: Int? = 5,
            val e: TestParamClass,
            val f: TestParamClass?,
            val g: TestParamClass = TestParamClass(),
            val h: TestParamClass? = TestParamClass()
    )

    @Test fun shouldHandleFalseFailOnNullForPrimitivesForDataClasses() {
        val mapper = jacksonObjectMapper().configure(DeserializationFeature.FAIL_ON_NULL_FOR_PRIMITIVES, false)
        val testClass = TestDataClass::class.java

        "a".isOptionalForDeserializationOf(testClass, mapper)
        "a".isRequiredForSerializationOf(testClass, mapper)

        "b".isOptionalForDeserializationOf(testClass, mapper)
        "b".isOptionalForSerializationOf(testClass, mapper)

        "c".isOptionalForDeserializationOf(testClass, mapper)
        "c".isRequiredForSerializationOf(testClass, mapper)

        "d".isOptionalForDeserializationOf(testClass, mapper)
        "d".isOptionalForSerializationOf(testClass, mapper)

        "e".isRequiredForDeserializationOf(testClass, mapper)
        "e".isRequiredForSerializationOf(testClass, mapper)

        "f".isOptionalForSerializationOf(testClass, mapper)
        "f".isOptionalForDeserializationOf(testClass, mapper)

        "g".isOptionalForDeserializationOf(testClass, mapper)
        "g".isRequiredForSerializationOf(testClass, mapper)

        "h".isOptionalForSerializationOf(testClass, mapper)
        "h".isOptionalForDeserializationOf(testClass, mapper)
    }

    @Test fun shouldHandleTrueFailOnNullForPrimitivesForDataClasses() {
        val mapper = jacksonObjectMapper().configure(DeserializationFeature.FAIL_ON_NULL_FOR_PRIMITIVES, true)
        val testClass = TestDataClass::class.java

        "a".isRequiredForDeserializationOf(testClass, mapper)
        "a".isRequiredForSerializationOf(testClass, mapper)

        "b".isOptionalForDeserializationOf(testClass, mapper)
        "b".isOptionalForSerializationOf(testClass, mapper)

        "c".isOptionalForDeserializationOf(testClass, mapper)
        "c".isRequiredForSerializationOf(testClass, mapper)

        "d".isOptionalForDeserializationOf(testClass, mapper)
        "d".isOptionalForSerializationOf(testClass, mapper)

        "e".isRequiredForDeserializationOf(testClass, mapper)
        "e".isRequiredForSerializationOf(testClass, mapper)

        "f".isOptionalForSerializationOf(testClass, mapper)
        "f".isOptionalForDeserializationOf(testClass, mapper)

        "g".isOptionalForDeserializationOf(testClass, mapper)
        "g".isRequiredForSerializationOf(testClass, mapper)

        "h".isOptionalForSerializationOf(testClass, mapper)
        "h".isOptionalForDeserializationOf(testClass, mapper)
    }

    private data class Github114TestObjA(val id: String, val prop: String) {
        companion object {
            @JsonCreator
            @JvmStatic
            fun parse(@JsonProperty("id") id: String,
                      @JsonProperty("name") name: String?) = Github114TestObjA(id, name ?: "yes")
        }
    }

    private data class Github114TestObjB(val id: String, val prop: String) {
        @JsonCreator constructor(
                @JsonProperty("id") id: String,
                @JsonProperty("name") name: String?,
                @JsonProperty("nameFallBack") nameFallBack: String) : this(id, name ?: nameFallBack)
    }

    @Test fun shouldHandleJsonCreatorAnnotationsOnCompanionObjectMethodCorrectly() {
        val mapper = jacksonObjectMapper().configure(DeserializationFeature.FAIL_ON_NULL_FOR_PRIMITIVES, false)
        val testClassA = Github114TestObjA::class.java

        "id".isRequiredForDeserializationOf(testClassA, mapper)
        "name".isOptionalForDeserializationOf(testClassA, mapper)
    }

    @Test fun shouldHandleJsonCreatorAnnotationsOnSecondaryConstructorCorrectly() {
        val mapper = jacksonObjectMapper().configure(DeserializationFeature.FAIL_ON_NULL_FOR_PRIMITIVES, false)
        val testClassB = Github114TestObjB::class.java

        "id".isRequiredForDeserializationOf(testClassB, mapper)
        "name".isOptionalForDeserializationOf(testClassB, mapper)
        "nameFallBack".isRequiredForDeserializationOf(testClassB, mapper)
    }

    private fun String.isRequiredForSerializationOf(type: Class<*>, mapper: ObjectMapper): Unit {
        assertTrue("Property $this should be required for serialization!"){
            introspectSerialization(type, mapper).isRequired(this)
        }
    }

    private fun String.isRequiredForDeserializationOf(type: Class<*>, mapper: ObjectMapper): Unit {
        assertTrue("Property $this should be required for deserialization!"){
            introspectDeserialization(type, mapper).isRequired(this)
        }
    }

    private fun String.isOptionalForSerializationOf(type: Class<*>, mapper: ObjectMapper): Unit {
        assertFalse("Property $this should be optional for serialization!"){
            introspectSerialization(type, mapper).isRequired(this)
        }
    }

    private fun String.isOptionalForDeserializationOf(type: Class<*>, mapper: ObjectMapper): Unit {
        assertFalse("Property $this should be optional for deserialization of ${type.simpleName}!"){
            introspectDeserialization(type, mapper).isRequired(this)
        }
    }

    private fun introspectSerialization(type: Class<*>, mapper: ObjectMapper): BeanDescription =
            mapper.serializationConfig.introspect(mapper.serializationConfig.constructType(type))

    private fun introspectDeserialization(type: Class<*>, mapper: ObjectMapper): BeanDescription =
            mapper.deserializationConfig.introspect(mapper.deserializationConfig.constructType(type))

    private fun BeanDescription.isRequired(propertyName: String): Boolean =
            this.findProperties()
                    .find { it.name == propertyName }!!.isRequired
}