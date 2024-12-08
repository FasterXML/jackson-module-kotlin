package com.fasterxml.jackson.module.kotlin.test

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.KotlinFeature.NullIsSameAsDefault
import com.fasterxml.jackson.module.kotlin.MissingKotlinParameterException
import com.fasterxml.jackson.module.kotlin.kotlinModule
import com.fasterxml.jackson.module.kotlin.readValue
import org.junit.Assert
import org.junit.Test
import org.junit.jupiter.api.assertThrows

class TestNullToDefault {

    private fun createMapper(allowDefaultingByNull: Boolean) = ObjectMapper()
        .registerModule(kotlinModule { configure(NullIsSameAsDefault, allowDefaultingByNull) })

    private data class TestClass(
        val sku: Int? = -1,
        val text: String,
        val images: String? = "some image",
        val language: String = "uk",
        val temperature: Double? = 24.7,
        val canBeProcessed: Boolean? = true,
    )

    data class TestClassWithNotNullPrimitives(
        val sku: Int = -1,
        val text: String,
        val temperature: Double = 24.7,
        val canBeProcessed: Boolean = true,
    )

    @Test
    fun shouldUseNullAsDefault() {
        val item = createMapper(true).readValue<TestClass>(
            """{
					"sku": null,
					"text": "plain",
					"images": null,
					"language": null,
					"temperature": null,
					"canBeProcessed": null
				}"""
        )

        Assert.assertEquals(-1, item.sku)
        Assert.assertEquals("plain", item.text)
        Assert.assertEquals("some image", item.images)
        Assert.assertEquals("uk", item.language)
		Assert.assertTrue(item.temperature == 24.7)
		Assert.assertEquals(true, item.canBeProcessed)
    }

    @Test
    fun shouldUseRealValuesInsteadOfDefaultsWhenProvided() {
        val item = createMapper(true).readValue<TestClass>(
            """{
					"sku": "0",
					"text": "plain",
					"images": "image1",
					"language": "pl",
					"temperature": "0.0",
					"canBeProcessed": "false"
				}"""
        )

        Assert.assertEquals(0, item.sku)
        Assert.assertEquals("plain", item.text)
        Assert.assertEquals("image1", item.images)
        Assert.assertEquals("pl", item.language)
        Assert.assertTrue(item.temperature == 0.0)
        Assert.assertEquals(false, item.canBeProcessed)
    }

    @Test
    fun shouldNotUseNullAsDefault() {
        val item = createMapper(false).readValue<TestClass>(
            """{
					"sku": "974",
					"text": "plain",
					"images": null,
					"language": "pl",
					"temperature": "36.6",
					"canBeProcessed": "false"
				}"""
        )

        Assert.assertEquals(974, item.sku)
        Assert.assertEquals("plain", item.text)
        Assert.assertEquals(null, item.images)
        Assert.assertEquals("pl", item.language)
		Assert.assertTrue(item.temperature == 36.6)
        Assert.assertEquals(false, item.canBeProcessed)
    }

	@Test
	fun shouldUseDefaultPrimitiveValuesInsteadOfDefaultsWhenProvidingNullForNotNullPrimitives() {
		val item = createMapper(false).readValue<TestClassWithNotNullPrimitives>(
			"""{
					"sku": null,
					"text": "plain",
					"temperature": null,
					"canBeProcessed": null
				}"""
		)

		Assert.assertEquals(0, item.sku)
		Assert.assertEquals("plain", item.text)
		Assert.assertTrue(item.temperature == 0.0)
		Assert.assertEquals(false, item.canBeProcessed)
	}

    @Test
    fun shouldUseDefaultsWhenNotProvidingAnyValueForNotNullPrimitives() {
        val item = createMapper(false).readValue<TestClassWithNotNullPrimitives>(
            """{
					"text": "plain"
				}"""
        )

        Assert.assertEquals(-1, item.sku)
        Assert.assertEquals("plain", item.text)
        Assert.assertTrue(item.temperature == 24.7)
        Assert.assertEquals(true, item.canBeProcessed)
    }

    @Test
    fun shouldUseDefaultValueWhenItsNotPresentEvenWhenDefaultingByNullIsDisabled() {
        val item = createMapper(false).readValue<TestClass>(
            """{
					"text": "plain"
				}"""
        )

        Assert.assertEquals(-1, item.sku)
        Assert.assertEquals("plain", item.text)
        Assert.assertEquals("some image", item.images)
        Assert.assertEquals("uk", item.language)
		Assert.assertTrue(item.temperature == 24.7)
        Assert.assertEquals(true, item.canBeProcessed)
    }

    @Test
    fun shouldThrowExceptionWhenProvidedNullForNotNullFieldWithoutDefault() {
        assertThrows<MissingKotlinParameterException> {
            createMapper(true).readValue<TestClass>(
                """{
						"text": null
 				}"""
            )
        }
    }

    @Test
    fun shouldUseDefaultValueForNullNestedDataClasses() {
        data class InnerDataClass(val someString: String = "someString")
        data class OuterDataClass(val innerClass: InnerDataClass = InnerDataClass())

        val outerDataClassInstance = createMapper(true).readValue<OuterDataClass>(
            """{
					"innerClass": null
				}"""
        )

        val expectedResult = OuterDataClass(InnerDataClass("someString"))
        Assert.assertEquals(expectedResult, outerDataClassInstance)
    }
}
