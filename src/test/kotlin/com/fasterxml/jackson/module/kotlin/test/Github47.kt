package com.fasterxml.jackson.module.kotlin.test

import com.fasterxml.jackson.databind.MapperFeature
import com.fasterxml.jackson.module.kotlin.*
import org.junit.Test
import kotlin.test.assertEquals

class Github47 {

    class ConfigItem(val configItemId: String)

    @Test
    fun testCaseInsensitivePropertyNames() {
        val mapper = jacksonMapperBuilder()
            .enable(MapperFeature.ACCEPT_CASE_INSENSITIVE_PROPERTIES)
                .build()

        val jsonWithMismtachedPropertyName = """
                    {
                        "configItemID": "test"
                    }
                   """

        val item: ConfigItem = mapper.readValue<ConfigItem>(jsonWithMismtachedPropertyName)
        assertEquals("test", item.configItemId)
    }
}