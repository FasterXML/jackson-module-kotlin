package tools.jackson.module.kotlin.test.github

import tools.jackson.dataformat.xml.XmlMapper
import tools.jackson.module.kotlin.kotlinModule
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

class Github396 {
    /**
     * Succeeds in Jackson 2.11.x, but fails in Jackson 2.12.0.
     * But succeeds again in 2.15.0.
     *
     * See https://github.com/FasterXML/jackson-module-kotlin/issues/396
     */
    @Test
    fun testMissingConstructor() {
        val mapper = XmlMapper.builder()
                .addModule(kotlinModule())
                .build()

        val xml = "<product><stuff></stuff></product>"
        val product: Product = mapper.readValue(xml, Product::class.java)

        assertEquals(Product(Stuff(null)), product)
    }

    private data class Stuff(val str: String?)
    private data class Product(val stuff: Stuff?)
}
