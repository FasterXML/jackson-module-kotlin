package com.fasterxml.jackson.module.kotlin.test.failing

import com.fasterxml.jackson.databind.exc.MismatchedInputException
import com.fasterxml.jackson.dataformat.xml.XmlMapper
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class TestGithub396 {
    /**
     * Succeeds in Jackson 2.11.x, but fails in Jackson 2.12.0
     * See https://github.com/FasterXML/jackson-module-kotlin/issues/396
     */
    @Test
    fun testMissingConstructor() {
        val mapper = XmlMapper().registerKotlinModule()

        val xml = "<product><stuff></stuff></product>"
        try {
            val product: Product = mapper.readValue(xml, Product::class.java)

            assertEquals(Product(null), product)
            fail("GitHub #396 has been fixed!")
        } catch (e: MismatchedInputException) {
            // Remove this try/catch and the `fail()` call above when this issue is fixed
        }
    }

    private data class Stuff(val str: String?)
    private data class Product(val stuff: Stuff?)
}
