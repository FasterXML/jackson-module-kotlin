package com.fasterxml.jackson.module.kotlin.test.github

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.KotlinFeature
import com.fasterxml.jackson.module.kotlin.KotlinModule
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
            .let { ObjectMapper().registerModule(it.build()) }
        val result = mapper.writeValueAsString(C(1))

        assertEquals("""{"foo":1,"bAr":1}""", result)
    }
}
