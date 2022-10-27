package com.fasterxml.jackson.module.kotlin.test.github.failing

import tools.jackson.databind.exc.MismatchedInputException
import com.fasterxml.jackson.dataformat.xml.XmlMapper
import com.fasterxml.jackson.module.kotlin.KotlinModule
import com.fasterxml.jackson.module.kotlin.test.expectFailure
import kotlin.test.Test
import kotlin.test.assertEquals

class TestGithub396 {
    /**
     * Succeeds in Jackson 2.11.x, but fails in Jackson 2.12.0
     * See https://github.com/FasterXML/jackson-module-kotlin/issues/396
     */
    @Test
    fun testMissingConstructor() {
        val mapper = XmlMapper.builder()
                .addModule(KotlinModule())
                .build()

        val xml = "<product><stuff></stuff></product>"
        expectFailure<MismatchedInputException>("GitHub #396 has been fixed!") {
            val product: Product = mapper.readValue(xml, Product::class.java)

            assertEquals(Product(null), product)
        }
    }

    private data class Stuff(val str: String?)
    private data class Product(val stuff: Stuff?)
}
