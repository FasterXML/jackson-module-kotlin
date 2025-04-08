package tools.jackson.module.kotlin

import tools.jackson.databind.BeanDescription
import tools.jackson.databind.DeserializationConfig
import tools.jackson.databind.ValueDeserializer
import tools.jackson.databind.deser.ValueDeserializerModifier

// [module-kotlin#225]: keep Kotlin singletons as singletons
object KotlinValueDeserializerModifier : ValueDeserializerModifier() {
    private fun readResolve(): Any = KotlinValueDeserializerModifier

    override fun modifyDeserializer(
            config: DeserializationConfig,
            beanDesc: BeanDescription,
            deserializer: ValueDeserializer<*>
    ): ValueDeserializer<out Any> {
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
