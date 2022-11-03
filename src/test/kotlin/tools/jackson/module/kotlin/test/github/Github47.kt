package tools.jackson.module.kotlin.test.github

import tools.jackson.databind.MapperFeature
import tools.jackson.module.kotlin.jacksonMapperBuilder
import tools.jackson.module.kotlin.readValue
import org.junit.Test
import kotlin.test.assertEquals

class TestGithub47 {

    class ConfigItem(val configItemId: String)

    @Test
    fun testCaseInsensitivePropertyNames() {
        val mapper = _root_ide_package_.tools.jackson.module.kotlin.jacksonMapperBuilder()
            .configure(MapperFeature.ACCEPT_CASE_INSENSITIVE_PROPERTIES, true)
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
