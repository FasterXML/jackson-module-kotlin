package com.fasterxml.jackson.module.kotlin.test.github

import com.fasterxml.jackson.dataformat.xml.XmlMapper
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement
import com.fasterxml.jackson.module.kotlin.KotlinModule
import org.junit.Ignore
import org.junit.Test
import kotlin.test.assertEquals


class TestGithub153 {
    @JacksonXmlRootElement(localName = "MyPojo")
    class MyPojo {
        @JacksonXmlElementWrapper(localName = "elements")
        @JacksonXmlProperty(localName = "element")
        var list: List<MyElement>? = null
    }

    class MyElement {
        @JacksonXmlProperty(localName = "value", isAttribute = true)
        var value: String? = null
    }

    @JacksonXmlRootElement(localName = "MyPojo")
    data class MyDataPojo (
            @JacksonXmlElementWrapper(localName = "elements")
            @JacksonXmlProperty(localName = "element")
            val elements : List<MyDataElement>
    )

    data class MyDataElement (
            @JacksonXmlProperty(localName = "value", isAttribute = true)
            var value: String
    )

    val xml = """<MyPojo><elements><element value="e1"/></elements></MyPojo>"""

    val mapper = XmlMapper()
        .registerModule(KotlinModule())

    @Test
    fun test_class() {

        // I create a pojo from the xml using the standard classes
        val pojoFromXml = mapper.readValue(xml, MyPojo::class.java)

        //I create a xml from the pojo
        val xmlFromPojo = mapper.writeValueAsString(pojoFromXml)

        // I compare the original xml with the xml generated from the pojo
        assertEquals(xml, xmlFromPojo)

    }

    @Test
    @Ignore("Conflict between the annotations that is not current resolvable.")
    fun test_data_class() {

        // I create a pojo from the xml using the data classes
        val pojoFromXml = mapper.readValue(xml, MyDataPojo::class.java)

        //I create a xml from the pojo
        val xmlFromPojo = mapper.writeValueAsString(pojoFromXml)

        // I compare the original xml with the xml generated from the pojo
        assertEquals(xml, xmlFromPojo)

    }

}