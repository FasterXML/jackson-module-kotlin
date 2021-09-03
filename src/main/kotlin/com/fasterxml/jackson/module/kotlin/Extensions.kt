package com.fasterxml.jackson.module.kotlin

import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.core.TreeNode
import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.*
import com.fasterxml.jackson.databind.json.JsonMapper
import com.fasterxml.jackson.databind.module.SimpleModule
import com.fasterxml.jackson.databind.node.ArrayNode
import com.fasterxml.jackson.databind.node.ObjectNode
import java.io.File
import java.io.InputStream
import java.io.Reader
import java.math.BigDecimal
import java.math.BigInteger
import java.net.URL
import java.util.*
import kotlin.reflect.KClass

fun kotlinModule(initializer: KotlinModule.Builder.() -> Unit = {}): KotlinModule {
    val builder = KotlinModule.Builder()
    builder.initializer()
    return builder.build()
}

fun jsonMapper(initializer: JsonMapper.Builder.() -> Unit = {}): JsonMapper {
    val builder = JsonMapper.builder()
    builder.initializer()
    return builder.build()
}

fun jacksonObjectMapper(): ObjectMapper = jsonMapper { addModule(kotlinModule()) }
fun jacksonMapperBuilder(): JsonMapper.Builder = JsonMapper.builder().addModule(kotlinModule())

fun ObjectMapper.registerKotlinModule(): ObjectMapper = this.registerModule(kotlinModule())

inline fun <reified T> jacksonTypeRef(): TypeReference<T> = object: TypeReference<T>() {}

inline fun <reified T> ObjectMapper.readValue(jp: JsonParser): T = readValue(jp, jacksonTypeRef<T>())
inline fun <reified T> ObjectMapper.readValues(jp: JsonParser): MappingIterator<T> = readValues(jp, jacksonTypeRef<T>())

inline fun <reified T> ObjectMapper.readValue(src: File): T = readValue(src, jacksonTypeRef<T>())
inline fun <reified T> ObjectMapper.readValue(src: URL): T = readValue(src, jacksonTypeRef<T>())
inline fun <reified T> ObjectMapper.readValue(content: String): T = readValue(content, jacksonTypeRef<T>())
inline fun <reified T> ObjectMapper.readValue(src: Reader): T = readValue(src, jacksonTypeRef<T>())
inline fun <reified T> ObjectMapper.readValue(src: InputStream): T = readValue(src, jacksonTypeRef<T>())
inline fun <reified T> ObjectMapper.readValue(src: ByteArray): T = readValue(src, jacksonTypeRef<T>())

inline fun <reified T> ObjectMapper.treeToValue(n: TreeNode): T = readValue(this.treeAsTokens(n), jacksonTypeRef<T>())
inline fun <reified T> ObjectMapper.convertValue(from: Any): T = convertValue(from, jacksonTypeRef<T>())

inline fun <reified T> ObjectReader.readValueTyped(jp: JsonParser): T = readValue(jp, jacksonTypeRef<T>())
inline fun <reified T> ObjectReader.readValuesTyped(jp: JsonParser): Iterator<T> = readValues(jp, jacksonTypeRef<T>())
inline fun <reified T> ObjectReader.treeToValue(n: TreeNode): T? = readValue(this.treeAsTokens(n), jacksonTypeRef<T>())

operator fun ArrayNode.plus(element: Boolean) = Unit.apply { add(element) }
operator fun ArrayNode.plus(element: Short) = Unit.apply { add(element) }
operator fun ArrayNode.plus(element: Int) = Unit.apply { add(element) }
operator fun ArrayNode.plus(element: Long) = Unit.apply { add(element) }
operator fun ArrayNode.plus(element: Float) = Unit.apply { add(element) }
operator fun ArrayNode.plus(element: Double) = Unit.apply { add(element) }
operator fun ArrayNode.plus(element: BigDecimal) = Unit.apply { add(element) }
operator fun ArrayNode.plus(element: BigInteger) = Unit.apply { add(element) }
operator fun ArrayNode.plus(element: String) = Unit.apply { add(element) }
operator fun ArrayNode.plus(element: ByteArray) = Unit.apply { add(element) }
operator fun ArrayNode.plus(element: JsonNode) = Unit.apply { add(element) }
operator fun ArrayNode.plus(elements: ArrayNode) = Unit.apply { addAll(elements) }
operator fun ArrayNode.plusAssign(element: Boolean) = Unit.apply { add(element) }
operator fun ArrayNode.plusAssign(element: Short) = Unit.apply { add(element) }
operator fun ArrayNode.plusAssign(element: Int) = Unit.apply { add(element) }
operator fun ArrayNode.plusAssign(element: Long) = Unit.apply { add(element) }
operator fun ArrayNode.plusAssign(element: Float) = Unit.apply { add(element) }
operator fun ArrayNode.plusAssign(element: Double) = Unit.apply { add(element) }
operator fun ArrayNode.plusAssign(element: BigDecimal) = Unit.apply { add(element) }
operator fun ArrayNode.plusAssign(element: BigInteger) = Unit.apply { add(element) }
operator fun ArrayNode.plusAssign(element: String) = Unit.apply { add(element) }
operator fun ArrayNode.plusAssign(element: ByteArray) = Unit.apply { add(element) }
operator fun ArrayNode.plusAssign(element: JsonNode) = Unit.apply { add(element) }
operator fun ArrayNode.plusAssign(elements: ArrayNode) = Unit.apply { addAll(elements) }
operator fun ArrayNode.minus(index: Int) = Unit.apply { remove(index) }
operator fun ArrayNode.minusAssign(index: Int) = Unit.apply { remove(index) }

operator fun ObjectNode.minus(field: String) = Unit.apply { remove(field) }
operator fun ObjectNode.minus(fields: Collection<String>) = Unit.apply { remove(fields) }
operator fun ObjectNode.minusAssign(field: String) = Unit.apply { remove(field) }
operator fun ObjectNode.minusAssign(fields: Collection<String>) = Unit.apply { remove(fields) }

operator fun JsonNode.contains(field: String) = has(field)
operator fun JsonNode.contains(index: Int) = has(index)

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

internal fun Int.toBitSet(): BitSet {
    var i = this
    var index = 0
    val bits = BitSet(32)
    while (i != 0) {
        if (i % 2 != 0) {
            bits.set(index)
        }
        ++index
        i = i shr 1
    }
    return bits
}
