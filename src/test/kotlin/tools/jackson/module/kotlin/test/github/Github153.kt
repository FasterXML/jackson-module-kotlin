package tools.jackson.module.kotlin.test.github

import com.fasterxml.jackson.annotation.JsonRootName
import tools.jackson.databind.exc.InvalidDefinitionException
import tools.jackson.dataformat.xml.XmlMapper
import tools.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper
import tools.jackson.dataformat.xml.annotation.JacksonXmlProperty
import tools.jackson.module.kotlin.kotlinModule
import tools.jackson.module.kotlin.test.expectFailure
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

class TestGithub153 {
    @JsonRootName("MyPojo")
    class MyPojo {
        @JacksonXmlElementWrapper(localName = "elements")
        @JacksonXmlProperty(localName = "element")
        var list: List<MyElement>? = null
    }

    class MyElement {
        @JacksonXmlProperty(localName = "value", isAttribute = true)
        var value: String? = null
    }

    @JsonRootName("MyPojo")
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

    val mapper = XmlMapper.builder().addModule(kotlinModule()).build()

    @Test
    fun test_class() {
        // I create a pojo from the xml using the standard classes
        val pojoFromXml = mapper.readValue(xml, MyPojo::class.java)

        // I create a xml from the pojo
        val xmlFromPojo = mapper.writeValueAsString(pojoFromXml)

        // I compare the original xml with the xml generated from the pojo
        assertEquals(xml, xmlFromPojo)
    }

    @Test
    // Conflict between the annotations that is not current resolvable.
    fun test_data_class() {
        expectFailure<InvalidDefinitionException>("Problem with conflicting annotations related to #153 has been fixed!") {
            // I create a pojo from the xml using the data classes
            val pojoFromXml = mapper.readValue(xml, MyDataPojo::class.java)

            // I create a xml from the pojo
            val xmlFromPojo = mapper.writeValueAsString(pojoFromXml)

            // I compare the original xml with the xml generated from the pojo
            assertEquals(xml, xmlFromPojo)
        }
    }
}
