package com.fasterxml.jackson.module.kotlin

import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.core.TreeNode
import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.*
import com.fasterxml.jackson.databind.json.JsonMapper
import com.fasterxml.jackson.databind.module.SimpleModule
import java.io.File
import java.io.InputStream
import java.io.Reader
import java.net.URL
import kotlin.reflect.KClass

fun jacksonObjectMapper(): ObjectMapper = JsonMapper.builder().addModule(KotlinModule()).build()
fun jacksonMapperBuilder(): JsonMapper.Builder = JsonMapper.builder().addModule(KotlinModule())

// 22-Jul-2019, tatu: Can not be implemented same way as in 2.x, addition via mapper.builder():
//fun ObjectMapper.registerKotlinModule(): ObjectMapper = this.registerModule(KotlinModule())

inline fun <reified T> jacksonTypeRef(): TypeReference<T> = object : TypeReference<T>() {}

inline fun <reified T> ObjectMapper.readValue(jp: JsonParser): T = readValue(jp, jacksonTypeRef<T>()).throwIfNullableTypeIsNull()
inline fun <reified T> ObjectMapper.readValues(jp: JsonParser): MappingIterator<T> = readValues(jp, jacksonTypeRef<T>()).throwIfNullableTypeIsNull()

inline fun <reified T> ObjectMapper.readValue(src: File): T = readValue(src, jacksonTypeRef<T>()).throwIfNullableTypeIsNull()
inline fun <reified T> ObjectMapper.readValue(src: URL): T = readValue(src, jacksonTypeRef<T>()).throwIfNullableTypeIsNull()
inline fun <reified T> ObjectMapper.readValue(content: String): T = readValue(content, jacksonTypeRef<T>()).throwIfNullableTypeIsNull()
inline fun <reified T> ObjectMapper.readValue(src: Reader): T = readValue(src, jacksonTypeRef<T>()).throwIfNullableTypeIsNull()
inline fun <reified T> ObjectMapper.readValue(src: InputStream): T = readValue(src, jacksonTypeRef<T>()).throwIfNullableTypeIsNull()
inline fun <reified T> ObjectMapper.readValue(src: ByteArray): T = readValue(src, jacksonTypeRef<T>()).throwIfNullableTypeIsNull()

inline fun <reified T> ObjectMapper.treeToValue(n: TreeNode): T? = treeToValue(n, T::class.java).throwIfNullableTypeIsNull()
inline fun <reified T> ObjectMapper.convertValue(from: Any): T = convertValue(from, jacksonTypeRef<T>()).throwIfNullableTypeIsNull()

inline fun <reified T> ObjectReader.readValueTyped(jp: JsonParser): T = readValue(jp, jacksonTypeRef<T>()).throwIfNullableTypeIsNull()
inline fun <reified T> ObjectReader.readValuesTyped(jp: JsonParser): Iterator<T> = readValues(jp, jacksonTypeRef<T>()).throwIfNullableTypeIsNull()
inline fun <reified T> ObjectReader.treeToValue(n: TreeNode): T? = treeToValue(n, T::class.java).throwIfNullableTypeIsNull()

internal fun JsonMappingException.wrapWithPath(refFrom: Any?, refFieldName: String) = JsonMappingException.wrapWithPath(this, refFrom, refFieldName)
internal fun JsonMappingException.wrapWithPath(refFrom: Any?, index: Int) = JsonMappingException.wrapWithPath(this, refFrom, index)

inline fun <reified T : Any> SimpleModule.addSerializer(kClass: KClass<T>, serializer: JsonSerializer<T>) = this.apply {
    addSerializer(kClass.java, serializer)
    addSerializer(kClass.javaObjectType, serializer)
}

inline fun <reified T : Any> SimpleModule.addDeserializer(kClass: KClass<T>, deserializer: JsonDeserializer<T>) = this.apply {
    addDeserializer(kClass.java, deserializer)
    addDeserializer(kClass.javaObjectType, deserializer)
}

inline fun <reified T> T.throwIfNullableTypeIsNull(): T {
    if (null !is T && this == null) {
        throw Exception()
    }
    return this
}