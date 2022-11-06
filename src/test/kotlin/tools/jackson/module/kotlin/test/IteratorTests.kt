package tools.jackson.module.kotlin.test

import tools.jackson.core.type.TypeReference
import tools.jackson.databind.ObjectMapper
import tools.jackson.databind.SerializationFeature
import tools.jackson.databind.annotation.JsonSerialize
import tools.jackson.module.kotlin.jacksonMapperBuilder
import org.junit.Test
import kotlin.test.assertEquals

class TestIteratorSubclass {
    class TinyPerson(val name: String, val age: Int)
    class KotlinPersonIterator(private val personList: List<TinyPerson>) : Iterator<TinyPerson> by personList.iterator()

    val mapper: ObjectMapper = jacksonMapperBuilder().disable(SerializationFeature.INDENT_OUTPUT)
            .build()

    @Test
    fun testKotlinIteratorWithTypeRef() {
        val expectedJson = """[{"name":"Fred","age":10},{"name":"Max","age":11}]"""
        val people = KotlinPersonIterator(listOf(TinyPerson("Fred", 10), TinyPerson("Max", 11)))
        val typeRef = object : TypeReference<Iterator<TinyPerson>>() {}
        val kotlinJson = mapper.writerFor(typeRef).writeValueAsString(people)
        assertEquals(expectedJson, kotlinJson)
    }

    @Test
    fun testKotlinIterator() {
        val expectedJson = """[{"name":"Fred","age":10},{"name":"Max","age":11}]"""
        val people = KotlinPersonIterator(listOf(TinyPerson("Fred", 10), TinyPerson("Max", 11)))
        val kotlinJson = mapper.writeValueAsString(people)
        assertEquals(expectedJson, kotlinJson)
    }

    class Company(
            val name: String,
            @Suppress("PLATFORM_CLASS_MAPPED_TO_KOTLIN") @JsonSerialize(`as` = java.util.Iterator::class) val people: KotlinPersonIterator
    )

    @Test
    fun testKotlinIteratorAsField() {
        val expectedJson = """{"name":"KidVille","people":[{"name":"Fred","age":10},{"name":"Max","age":11}]}"""
        val people = KotlinPersonIterator(listOf(TinyPerson("Fred", 10), TinyPerson("Max", 11)))
        val company = Company("KidVille", people)
        val kotlinJson = mapper.writeValueAsString(company)
        assertEquals(expectedJson, kotlinJson)
    }

}
