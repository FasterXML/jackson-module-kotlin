package com.fasterxml.jackson.module.kotlin.test

import com.fasterxml.jackson.annotation.JsonTypeInfo
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.KotlinModule
import org.junit.Test
import java.io.StringWriter
import kotlin.test.assertSame

class ObjectDeserializationTest {
    object TestObject

    @JsonTypeInfo(use = JsonTypeInfo.Id.CLASS)
    sealed class Parent {
        object ChildObject : Parent()
    }

    private object PrivateTestObject

    @JsonTypeInfo(use = JsonTypeInfo.Id.CLASS)
    private sealed class PrivateParent {
        object ChildObject : PrivateParent()
    }

    @Test
    fun shouldDeserializeIntoStaticReference() {
        val mapper = createMapper()

        val writer = StringWriter()

        mapper.writeValue(writer, TestObject)

        assertSame(mapper.readValue(writer.toString(), TestObject::class.java), TestObject)
    }

    @Test
    fun shouldDeserializeIntoStaticReferencePolymorphic() {
        val mapper = createMapper()

        val writer = StringWriter()

        mapper.writeValue(writer, Parent.ChildObject)

        assertSame(mapper.readValue(writer.toString(), Parent::class.java), Parent.ChildObject)
    }

    @Test
    fun shouldDeserializeIntoStaticReferencePrivate() {
        val mapper = createMapper()

        val writer = StringWriter()

        mapper.writeValue(writer, PrivateTestObject)

        assertSame(mapper.readValue(writer.toString(), PrivateTestObject::class.java), PrivateTestObject)
    }

    @Test
    fun shouldDeserializeIntoStaticReferencePolymorphicPrivate() {
        val mapper = createMapper()

        val writer = StringWriter()

        mapper.writeValue(writer, PrivateParent.ChildObject)

        assertSame(mapper.readValue(writer.toString(), PrivateParent::class.java), PrivateParent.ChildObject)
    }

    private fun createMapper(): ObjectMapper {
        return ObjectMapper().registerModule(KotlinModule())
    }
}
