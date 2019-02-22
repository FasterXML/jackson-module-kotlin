package com.fasterxml.jackson.module.kotlin

import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.databind.*
import com.fasterxml.jackson.databind.deser.Deserializers
import com.fasterxml.jackson.databind.jsontype.TypeDeserializer
import com.fasterxml.jackson.databind.type.*

class KotlinObjectInstanceDeserializers : Deserializers {
    class StaticDeserializer(val content: Any) : JsonDeserializer<Any>() {
        override fun deserialize(p: JsonParser?, ctxt: DeserializationContext?): Any {
            return content
        }
    }

    override fun findCollectionLikeDeserializer(type: CollectionLikeType?, config: DeserializationConfig?, beanDesc: BeanDescription?, elementTypeDeserializer: TypeDeserializer?, elementDeserializer: JsonDeserializer<*>?): JsonDeserializer<*>? {
        return null
    }

    override fun findMapDeserializer(type: MapType?, config: DeserializationConfig?, beanDesc: BeanDescription?, keyDeserializer: KeyDeserializer?, elementTypeDeserializer: TypeDeserializer?, elementDeserializer: JsonDeserializer<*>?): JsonDeserializer<*>? {
        return null
    }

    override fun findBeanDeserializer(type: JavaType, config: DeserializationConfig, beanDesc: BeanDescription?): JsonDeserializer<*>? {
        if (!type.rawClass.isKotlinClass()) {
            return null
        }

        val instanceOrNull = try {
            type.rawClass.kotlin.objectInstance
        } catch (ex: IllegalAccessException) {
            // handle private class access
            val instanceField = type.rawClass.fields.firstOrNull { it.name == "INSTANCE" } ?: throw ex
            val accessible = instanceField.isAccessible
            if ((!accessible && config.isEnabled(MapperFeature.CAN_OVERRIDE_ACCESS_MODIFIERS)) ||
                    (accessible && config.isEnabled(MapperFeature.OVERRIDE_PUBLIC_ACCESS_MODIFIERS))
            ) {
                instanceField.isAccessible = true
            }
            instanceField.get(null) ?: throw ex
        }

        return instanceOrNull?.let { StaticDeserializer(it) }
    }

    override fun findTreeNodeDeserializer(nodeType: Class<out JsonNode>?, config: DeserializationConfig?, beanDesc: BeanDescription?): JsonDeserializer<*>? {
        return null
    }

    override fun findReferenceDeserializer(refType: ReferenceType?, config: DeserializationConfig?, beanDesc: BeanDescription?, contentTypeDeserializer: TypeDeserializer?, contentDeserializer: JsonDeserializer<*>?): JsonDeserializer<*>? {
        return null
    }

    override fun findMapLikeDeserializer(type: MapLikeType?, config: DeserializationConfig?, beanDesc: BeanDescription?, keyDeserializer: KeyDeserializer?, elementTypeDeserializer: TypeDeserializer?, elementDeserializer: JsonDeserializer<*>?): JsonDeserializer<*>? {
        return null
    }

    override fun findEnumDeserializer(type: Class<*>?, config: DeserializationConfig?, beanDesc: BeanDescription?): JsonDeserializer<*>? {
        return null
    }

    override fun findArrayDeserializer(type: ArrayType?, config: DeserializationConfig?, beanDesc: BeanDescription?, elementTypeDeserializer: TypeDeserializer?, elementDeserializer: JsonDeserializer<*>?): JsonDeserializer<*>? {
        return null
    }

    override fun findCollectionDeserializer(type: CollectionType?, config: DeserializationConfig?, beanDesc: BeanDescription?, elementTypeDeserializer: TypeDeserializer?, elementDeserializer: JsonDeserializer<*>?): JsonDeserializer<*>? {
        return null
    }
}
