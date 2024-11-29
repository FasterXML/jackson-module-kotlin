package tools.jackson.module.kotlin

import com.fasterxml.jackson.annotation.JsonFormat
import com.fasterxml.jackson.annotation.JsonKey
import tools.jackson.core.JsonGenerator
import tools.jackson.databind.BeanDescription
import tools.jackson.databind.JavaType
import tools.jackson.databind.SerializationConfig
import tools.jackson.databind.SerializationContext
import tools.jackson.databind.ValueSerializer
import tools.jackson.databind.ser.Serializers
import tools.jackson.databind.ser.std.StdSerializer
import java.lang.reflect.Method
import java.lang.reflect.Modifier

internal object ValueClassUnboxKeySerializer : StdSerializer<Any>(Any::class.java) {
    override fun serialize(value: Any, gen: JsonGenerator, ctxt: SerializationContext) {
        val method = value::class.java.getMethod("unbox-impl")
        val unboxed = method.invoke(value)

        if (unboxed == null) {
            val javaType = ctxt.typeFactory.constructType(method.genericReturnType)
            ctxt.findNullKeySerializer(javaType, null).serialize(null, gen, ctxt)
            return
        }

        ctxt.findKeySerializer(unboxed::class.java, null).serialize(unboxed, gen, ctxt)
    }
}

// Class must be UnboxableValueClass.
private fun Class<*>.getStaticJsonKeyGetter(): Method? = this.declaredMethods.find { method ->
    Modifier.isStatic(method.modifiers) && method.annotations.any { it is JsonKey && it.value }
}

internal class ValueClassStaticJsonKeySerializer<T>(
    t: Class<T>,
    private val staticJsonKeyGetter: Method
) : StdSerializer<T>(t) {
    private val keyType: Class<*> = staticJsonKeyGetter.returnType
    private val unboxMethod: Method = t.getMethod("unbox-impl")

    override fun serialize(value: T, gen: JsonGenerator, ctxt: SerializationContext) {
        val unboxed = unboxMethod.invoke(value)
        val jsonKey: Any? = staticJsonKeyGetter.invoke(null, unboxed)

        val serializer = jsonKey
            ?.let { ctxt.findKeySerializer(keyType, null) }
            ?: ctxt.findNullKeySerializer(ctxt.constructType(keyType), null)

        serializer.serialize(jsonKey, gen, ctxt)
    }

    companion object {
        fun createOrNull(t: Class<*>): StdSerializer<*>? =
            t.getStaticJsonKeyGetter()?.let { ValueClassStaticJsonKeySerializer(t, it) }
    }
}

internal class KotlinKeySerializers : Serializers.Base() {
    override fun findSerializer(
        config: SerializationConfig,
        type: JavaType,
        beanDesc: BeanDescription,
        formatOverrides: JsonFormat.Value?
    ): ValueSerializer<*>? = when {
        type.rawClass.isUnboxableValueClass() -> ValueClassStaticJsonKeySerializer.createOrNull(type.rawClass)
            ?: ValueClassUnboxKeySerializer
        else -> null
    }
}
