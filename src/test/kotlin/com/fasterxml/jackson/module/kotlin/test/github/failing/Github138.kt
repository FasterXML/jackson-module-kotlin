package com.fasterxml.jackson.module.kotlin.test.github.failing

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.databind.exc.InvalidDefinitionException
import com.fasterxml.jackson.dataformat.xml.XmlMapper
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlText
import com.fasterxml.jackson.module.kotlin.readValue
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import com.fasterxml.jackson.module.kotlin.test.expectFailure
import org.junit.Test

class TestGithub138 {
    @JacksonXmlRootElement(localName = "sms")
    @JsonIgnoreProperties(ignoreUnknown = true)
    data class Sms(
            @JacksonXmlProperty(localName = "Phone", isAttribute = true)
            val phone: String?,

            @JacksonXmlText
            val text: String? = ""
    )

    @Test
    fun testDeserProblem() {
        val xml = """<sms Phone="435242423412" Id="43234324">Lorem ipsum</sms>"""
        val xmlMapper = XmlMapper().registerKotlinModule()
        expectFailure<InvalidDefinitionException>("GitHub #138 has been fixed!") {
            xmlMapper.readValue<Sms>(xml)
        }
    }
}
