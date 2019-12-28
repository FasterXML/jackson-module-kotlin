package com.fasterxml.jackson.module.kotlin.test

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.KotlinModule
import com.fasterxml.jackson.module.kotlin.MissingKotlinParameterException
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import org.junit.Assert
import org.junit.Ignore
import org.junit.Test
import org.junit.internal.runners.statements.ExpectException

class TestNullToDefault {

	private fun createMapper(allowDefaultingByNull: Boolean) = ObjectMapper().registerModule(KotlinModule(nullisSameAsDefault = allowDefaultingByNull))

	private data class TestClass(val sku: Int = -1,
								 val text: String,
								 val name: String = "",
								 val images: String?,
								 val language: String = "uk",
								 val attribute: Int = 0,
								 val order: Int = -1)

	@Test
	fun shouldUseNullAsDefault() {
		val item = createMapper(true).readValue<TestClass>(
				"""{
					"sku": "974",
					"text": "plain",
					"name": null,
					"images": null,
					"attribute": "19"     
				}""")

		Assert.assertTrue(item.sku == 974)
		Assert.assertTrue(item.text == "plain")
		@Suppress("SENSELESS_COMPARISON")
		Assert.assertTrue(item.name != null)
		Assert.assertTrue(item.images == null)
		Assert.assertTrue(item.language == "uk")
		Assert.assertTrue(item.attribute == 19)
		Assert.assertTrue(item.order == -1)
	}

	@Test(expected = MissingKotlinParameterException::class)
	fun shouldNotUseNullAsDefault() {
		val item = createMapper(false).readValue<TestClass>(
				"""{
					"sku": "974",
					"text": "plain",
					"name": null,
					"images": null,
					"attribute": "19"     
				}""")

		Assert.assertTrue(item.sku == 974)
		Assert.assertTrue(item.text == "plain")
		@Suppress("SENSELESS_COMPARISON")
		Assert.assertTrue(item.name != null)
		Assert.assertTrue(item.images == null)
		Assert.assertTrue(item.language == "uk")
		Assert.assertTrue(item.attribute == 19)
		Assert.assertTrue(item.order == -1)
	}

	@Test(expected = MissingKotlinParameterException::class)
	fun errorIfNotDefault() {
		val item = createMapper(true).readValue<TestClass>(
				"""{
						"sku": "974",
						"text": null,
						"attribute": "19",
						"name": null     
 				}""")

		Assert.assertTrue(item.sku == 974)
		Assert.assertTrue(item.language == "uk")
		Assert.assertTrue(item.attribute == 19)
		@Suppress("SENSELESS_COMPARISON")
		Assert.assertTrue(item.name != null)
		Assert.assertTrue(item.order == -1)
	}
}