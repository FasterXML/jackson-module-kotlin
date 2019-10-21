package com.fasterxml.jackson.module.kotlin

import com.fasterxml.jackson.databind.BeanDescription
import com.fasterxml.jackson.databind.DeserializationConfig
import com.fasterxml.jackson.databind.JsonDeserializer
import com.fasterxml.jackson.databind.deser.BeanDeserializerModifier

object KotlinBeanDeserializerModifier: BeanDeserializerModifier() {
    override fun modifyDeserializer(
            config: DeserializationConfig,
            beanDesc: BeanDescription,
            deserializer: JsonDeserializer<*>
    ) = super.modifyDeserializer(config, beanDesc, deserializer)
            .maybeSingletonDeserializer(objectSingletonInstance(beanDesc.beanClass))
}

fun JsonDeserializer<*>.maybeSingletonDeserializer(singleton: Any?) = when (singleton) {
    null -> this
    else -> this.asSingletonDeserializer(singleton)
}

private fun objectSingletonInstance(beanClass: Class<*>): Any? = if (!beanClass.isKotlinClass()) {
    null
} else {
    beanClass.kotlin.objectInstance
}