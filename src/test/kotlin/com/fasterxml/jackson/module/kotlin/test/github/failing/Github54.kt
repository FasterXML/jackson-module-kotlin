package com.fasterxml.jackson.module.kotlin.test.github.failing

import com.fasterxml.jackson.annotation.JsonIdentityInfo
import com.fasterxml.jackson.annotation.ObjectIdGenerators
import com.fasterxml.jackson.databind.deser.UnresolvedForwardReference
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import com.fasterxml.jackson.module.kotlin.test.expectFailure
import org.junit.Test

class TestGithub54 {
    @Test
    fun testDeserWithIdentityInfo() {
        val mapper = jacksonObjectMapper()

        val entity1 = Entity1("test_entity1")
        val entity2 = Entity2("test_entity2", entity1 = entity1)
        val rootEntity1 = Entity1("root_entity1", entity2 = entity2)

        entity1.parent = rootEntity1
        entity1.entity2 = entity2

        val json = mapper.writeValueAsString(entity1)
        expectFailure<UnresolvedForwardReference>("GitHub #54 has been fixed!") {
            mapper.readValue<Entity1>(json)
        }

    }
}

@JsonIdentityInfo(generator = ObjectIdGenerators.IntSequenceGenerator::class)
data class Entity1(val name: String, var entity2: Entity2? = null, var parent: Entity1? = null)

@JsonIdentityInfo(generator = ObjectIdGenerators.IntSequenceGenerator::class)
class Entity2(val name: String, var entity1: Entity1? = null)
