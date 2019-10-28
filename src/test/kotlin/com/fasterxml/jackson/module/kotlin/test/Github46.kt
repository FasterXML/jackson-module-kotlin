package com.fasterxml.jackson.module.kotlin.test

import com.fasterxml.jackson.module.kotlin.*
import org.junit.Assert
import org.junit.Test
import kotlin.reflect.full.primaryConstructor
import kotlin.test.assertEquals

class TestGithub46 {
    @Test fun `map 32 properties`() {
        // given
        val json = """{"prop1":true,"prop2":true,"prop3":true,"prop4":true,"prop5":true,"prop6":true,"prop7":true,"prop8":true,"prop9":true,"prop10":true,"prop11":true,"prop12":true,"prop13":true,"prop14":true,"prop15":true,"prop16":true,"prop17":true,"prop18":true,"prop19":true,"prop20":true,"prop21":true,"prop22":true,"prop23":true,"prop24":true,"prop25":true,"prop26":true,"prop27":true,"prop28":true,"prop29":true,"prop30":true,"prop31":true,"prop32":true}"""
        val mapper = jacksonObjectMapper()

        // when
        val data: TestData = mapper.readValue(json)
        assertEquals(TestData(true, true, true, true, true, true, true, true, true, true, true, true, true, true, true, true, true, true, true, true, true, true, true, true, true, true, true, true, true, true, true, true),
                data)
        val rejson = mapper.writeValueAsString(data)

        // then
        Assert.assertEquals(json, rejson)
    }

    @Test fun byReflectionDo32() {
        val constructor = TestData::class.primaryConstructor!!
        val data = constructor.callBy(
                constructor.parameters.map {
                    it to true
                }.toMap()
        )
        assertEquals(TestData(true, true, true, true, true, true, true, true, true, true, true, true, true, true, true, true, true, true, true, true, true, true, true, true, true, true, true, true, true, true, true, true),
                data)
    }

    data class TestData(
            var prop1: Boolean = false,
            var prop2: Boolean = false,
            var prop3: Boolean = false,
            var prop4: Boolean = false,
            var prop5: Boolean = false,
            var prop6: Boolean = false,
            var prop7: Boolean = false,
            var prop8: Boolean = false,
            var prop9: Boolean = false,
            var prop10: Boolean = false,
            var prop11: Boolean = false,
            var prop12: Boolean = false,
            var prop13: Boolean = false,
            var prop14: Boolean = false,
            var prop15: Boolean = false,
            var prop16: Boolean = false,
            var prop17: Boolean = false,
            var prop18: Boolean = false,
            var prop19: Boolean = false,
            var prop20: Boolean = false,
            var prop21: Boolean = false,
            var prop22: Boolean = false,
            var prop23: Boolean = false,
            var prop24: Boolean = false,
            var prop25: Boolean = false,
            var prop26: Boolean = false,
            var prop27: Boolean = false,
            var prop28: Boolean = false,
            var prop29: Boolean = false,
            var prop30: Boolean = false,
            var prop31: Boolean = false,
            var prop32: Boolean = false
    )
}