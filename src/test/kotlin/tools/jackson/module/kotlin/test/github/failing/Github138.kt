package tools.jackson.module.kotlin.test.github.failing

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import tools.jackson.databind.exc.InvalidDefinitionException
import tools.jackson.dataformat.xml.XmlMapper
import tools.jackson.dataformat.xml.annotation.JacksonXmlProperty
import tools.jackson.dataformat.xml.annotation.JacksonXmlRootElement
import tools.jackson.dataformat.xml.annotation.JacksonXmlText
import tools.jackson.module.kotlin.KotlinModule
import tools.jackson.module.kotlin.readValue
import tools.jackson.module.kotlin.test.expectFailure
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
        val xmlMapper = XmlMapper.builder().addModule(KotlinModule()).build()
        expectFailure<InvalidDefinitionException>("GitHub #138 has been fixed!") {
            xmlMapper.readValue<Sms>(xml)
        }
    }
}
