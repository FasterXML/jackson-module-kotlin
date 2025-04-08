package tools.jackson.module.kotlin.test.github

import tools.jackson.databind.json.JsonMapper
import tools.jackson.module.kotlin.KotlinFeature
import tools.jackson.module.kotlin.KotlinModule
import tools.jackson.module.kotlin.convertValue
import org.junit.jupiter.api.Test
import kotlin.test.assertNull

class GitHub757 {
    @Test
    fun test() {
        val kotlinModule = KotlinModule.Builder()
            .enable(KotlinFeature.NewStrictNullChecks)
            .build()
        val mapper = JsonMapper.builder()
            .addModule(kotlinModule)
            .build()
        val convertValue = mapper.convertValue<String?>(null)
        assertNull(convertValue)
    }
}
