package tools.jackson.module.kotlin.test.github

import tools.jackson.module.kotlin.KotlinFeature
import tools.jackson.module.kotlin.KotlinModule
import tools.jackson.module.kotlin.jsonMapper
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

class Github710 {
    interface I<T> {
        val foo: T
        val bAr: T get() = foo
    }

    class C(override val foo: Int) : I<Int>

    @Test
    fun test() {
        val mapper = KotlinModule.Builder().enable(KotlinFeature.KotlinPropertyNameAsImplicitName)
            .let { jsonMapper { addModule(it.build()) } }
        val result = mapper.writeValueAsString(C(1))

        assertEquals("""{"foo":1,"bAr":1}""", result)
    }
}
