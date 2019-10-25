package com.fasterxml.jackson.module.kotlin.test

import com.shadow.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.shadow.fasterxml.jackson.module.kotlin.readValue
import org.junit.Assert
import org.junit.Test

class NullToDefault {

	private fun createMapper() = jacksonObjectMapper()

	private data class TestClass(val sku: Int = -1,
								 val text: String,
								 val name: String = "",
								 val images: String?,
								 val language: String = "uk",
								 val attribute: Int = 0,
								 val order: Int = -1)

	@Test
	fun shouldUseDefault() {
		val item = createMapper().readValue<TestClass>("{\n" +
				"            \"sku\": \"974\",\n" +
				"            \"text\": \"plain\",\n" +
				"            \"name\": null,\n" +
				"            \"images\": null,\n" +
				"            \"attribute\": \"19\",\n" +
				"            \"new_item\": \"Composition: 100% polyester; 1.75 \"" +
				"        }")

		Assert.assertTrue(item.sku == 974)
		Assert.assertTrue(item.text == "plain")
		Assert.assertTrue(item.name != null)
		Assert.assertTrue(item.images == null)
		Assert.assertTrue(item.language == "uk")
		Assert.assertTrue(item.attribute == 19)
		Assert.assertTrue(item.order == -1)
	}

	@Test
	fun errorIfNotDefault() {
		val item = createMapper().readValue<TestClass>("{\n" +
				"            \"sku\": \"974\",\n" +
				"            \"text\": null,\n" +
				"            \"attribute\": \"19\",\n" +
				"            \"name\": null,\n" +
				"            \"new_item\": \"Composition: 100% polyester; 1.75 \"" +
				"        }")

		Assert.assertTrue(item.sku == 974)
		Assert.assertTrue(item.language == "uk")
		Assert.assertTrue(item.attribute == 19)
		Assert.assertTrue(item.name != null)
		Assert.assertTrue(item.order == -1)
	}
}