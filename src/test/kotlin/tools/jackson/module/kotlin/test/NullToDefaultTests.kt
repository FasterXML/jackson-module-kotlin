package tools.jackson.module.kotlin.test

import tools.jackson.databind.json.JsonMapper
import tools.jackson.module.kotlin.KotlinFeature.NullIsSameAsDefault
import tools.jackson.module.kotlin.MissingKotlinParameterException
import tools.jackson.module.kotlin.kotlinModule
import tools.jackson.module.kotlin.readValue
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

class TestNullToDefault {
	private fun createMapper(allowDefaultingByNull: Boolean) = JsonMapper.builder()
		.addModule(kotlinModule {
            configure(
                NullIsSameAsDefault,
                allowDefaultingByNull
            )
        })
		.build()

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

        assertEquals(-1, item.sku)
        assertEquals("plain", item.text)
        assertEquals("some image", item.images)
        assertEquals("uk", item.language)
		assertTrue(item.temperature == 24.7)
		assertEquals(true, item.canBeProcessed)
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

        assertEquals(0, item.sku)
        assertEquals("plain", item.text)
        assertEquals("image1", item.images)
        assertEquals("pl", item.language)
        assertTrue(item.temperature == 0.0)
        assertEquals(false, item.canBeProcessed)
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

        assertEquals(974, item.sku)
        assertEquals("plain", item.text)
        assertEquals(null, item.images)
        assertEquals("pl", item.language)
		assertTrue(item.temperature == 36.6)
        assertEquals(false, item.canBeProcessed)
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

		assertEquals(0, item.sku)
		assertEquals("plain", item.text)
		assertTrue(item.temperature == 0.0)
		assertEquals(false, item.canBeProcessed)
	}

    @Test
    fun shouldUseDefaultsWhenNotProvidingAnyValueForNotNullPrimitives() {
        val item = createMapper(false).readValue<TestClassWithNotNullPrimitives>(
            """{
					"text": "plain"
				}"""
        )

        assertEquals(-1, item.sku)
        assertEquals("plain", item.text)
        assertTrue(item.temperature == 24.7)
        assertEquals(true, item.canBeProcessed)
    }

    @Test
    fun shouldUseDefaultValueWhenItsNotPresentEvenWhenDefaultingByNullIsDisabled() {
        val item = createMapper(false).readValue<TestClass>(
            """{
					"text": "plain"
				}"""
        )

        assertEquals(-1, item.sku)
        assertEquals("plain", item.text)
        assertEquals("some image", item.images)
        assertEquals("uk", item.language)
		assertTrue(item.temperature == 24.7)
        assertEquals(true, item.canBeProcessed)
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
        assertEquals(expectedResult, outerDataClassInstance)
    }
}
