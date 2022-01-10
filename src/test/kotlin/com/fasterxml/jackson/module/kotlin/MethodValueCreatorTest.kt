package com.fasterxml.jackson.module.kotlin

import io.mockk.spyk
import io.mockk.verify
import org.junit.Test
import kotlin.reflect.KFunction
import kotlin.reflect.full.functions
import kotlin.test.assertEquals

class MethodValueCreatorTest {
    data class Data(val value: Int) {
        companion object {
            fun target(value: Int = -1) = Data(value)
        }
    }

    companion object {
        private val targetFunction: KFunction<*> = spyk(Data.Companion::class.functions.first { it.name == "target" })
        private val methodValueCreator = MethodValueCreator.of(targetFunction)!!
    }

    @Test
    fun withDefaultValue() {
        val actual = methodValueCreator.generateBucket().let { methodValueCreator.callBy(it) }
        assertEquals(Data(-1), actual)
        verify(exactly = 1) { targetFunction.callBy(any()) }
    }

    @Test
    fun withoutDefaultValue() {
        val actual = methodValueCreator.generateBucket().let {
            it[1] = 1
            methodValueCreator.callBy(it)
        }
        assertEquals(Data(1), actual)
        // If the argument is fully initialized, call is used instead of callBy
        verify(exactly = 1) { targetFunction.call(*anyVararg()) }
        verify(exactly = 0) { targetFunction.callBy(any()) }
    }
}
