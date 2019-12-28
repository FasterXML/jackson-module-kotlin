package com.fasterxml.jackson.module.kotlin

import com.fasterxml.jackson.databind.BeanDescription
import com.fasterxml.jackson.databind.DeserializationConfig
import com.fasterxml.jackson.databind.JsonDeserializer
import com.fasterxml.jackson.databind.deser.BeanDeserializerModifier

// [module-kotlin#225]: keep Kotlin singletons as singletons
object KotlinBeanDeserializerModifier : BeanDeserializerModifier() {
    override fun modifyDeserializer(
            config: DeserializationConfig,
            beanDesc: BeanDescription,
            deserializer: JsonDeserializer<*>
    ): JsonDeserializer<out Any> {
        val modifiedFromParent = super.modifyDeserializer(config, beanDesc, deserializer)

        val objectSingletonInstance = objectSingletonInstance(beanDesc.beanClass)
        return if (objectSingletonInstance != null) {
            KotlinObjectSingletonDeserializer(objectSingletonInstance, modifiedFromParent)
        } else {
            modifiedFromParent
        }
    }
}

private fun objectSingletonInstance(beanClass: Class<*>): Any? = if (!beanClass.isKotlinClass()) {
    null
} else {
    beanClass.kotlin.objectInstance
}
