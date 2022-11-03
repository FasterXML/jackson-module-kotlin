package tools.jackson.module.kotlin

import tools.jackson.core.JsonParser
import tools.jackson.core.TreeNode
import tools.jackson.core.type.TypeReference
import tools.jackson.databind.DatabindException
import tools.jackson.databind.JsonNode
import tools.jackson.databind.MappingIterator
import tools.jackson.databind.ObjectMapper
import tools.jackson.databind.ObjectReader
import tools.jackson.databind.ValueDeserializer
import tools.jackson.databind.ValueSerializer
import tools.jackson.databind.json.JsonMapper
import tools.jackson.databind.module.SimpleModule
import tools.jackson.databind.node.ArrayNode
import tools.jackson.databind.node.ObjectNode
import java.io.File
import java.io.InputStream
import java.io.Reader
import java.math.BigDecimal
import java.math.BigInteger
import java.net.URL
import java.util.*
import kotlin.reflect.KClass

fun kotlinModule(initializer: tools.jackson.module.kotlin.KotlinModule.Builder.() -> Unit = {}): _root_ide_package_.tools.jackson.module.kotlin.KotlinModule {
    val builder = _root_ide_package_.tools.jackson.module.kotlin.KotlinModule.Builder()
    builder.initializer()
    return builder.build()
}

fun jsonMapper(initializer: JsonMapper.Builder.() -> Unit = {}): JsonMapper {
    val builder = JsonMapper.builder()
    builder.initializer()
    return builder.build()
}

fun jacksonObjectMapper(): ObjectMapper =
    _root_ide_package_.tools.jackson.module.kotlin.jsonMapper { addModule(kotlinModule()) }
fun jacksonMapperBuilder(): JsonMapper.Builder = JsonMapper.builder().addModule(_root_ide_package_.tools.jackson.module.kotlin.kotlinModule())

// 22-Jul-2019, tatu: Can not be implemented same way as in 2.x, addition via mapper.builder():
//fun ObjectMapper.registerKotlinModule(): ObjectMapper = this.registerModule(KotlinModule())

inline fun <reified T> jacksonTypeRef(): TypeReference<T> = object : TypeReference<T>() {}

inline fun <reified T> ObjectMapper.readValue(p: JsonParser): T = readValue(p, jacksonTypeRef<T>())
inline fun <reified T> ObjectMapper.readValues(p: JsonParser): MappingIterator<T> = readValues(p, jacksonTypeRef<T>())

inline fun <reified T> ObjectMapper.readValue(src: File): T = readValue(src, jacksonTypeRef<T>())
inline fun <reified T> ObjectMapper.readValue(src: URL): T = readValue(src, jacksonTypeRef<T>())
inline fun <reified T> ObjectMapper.readValue(content: String): T = readValue(content, jacksonTypeRef<T>())
inline fun <reified T> ObjectMapper.readValue(src: Reader): T = readValue(src, jacksonTypeRef<T>())
inline fun <reified T> ObjectMapper.readValue(src: InputStream): T = readValue(src, jacksonTypeRef<T>())
inline fun <reified T> ObjectMapper.readValue(src: ByteArray): T = readValue(src, jacksonTypeRef<T>())

inline fun <reified T> ObjectMapper.treeToValue(n: TreeNode): T = readValue(this.treeAsTokens(n), jacksonTypeRef<T>())
inline fun <reified T> ObjectMapper.convertValue(from: Any): T = convertValue(from, jacksonTypeRef<T>())

inline fun <reified T> ObjectReader.readValueTyped(p: JsonParser): T = forType(jacksonTypeRef<T>()).readValue(p)
inline fun <reified T> ObjectReader.readValuesTyped(p: JsonParser): Iterator<T> = readValues(p, jacksonTypeRef<T>())
inline fun <reified T> ObjectReader.treeToValue(n: TreeNode): T? = forType(jacksonTypeRef<T>()).readValue(this.treeAsTokens(n))

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

internal fun DatabindException.wrapWithPath(refFrom: Any?, refFieldName: String) = DatabindException.wrapWithPath(this, refFrom, refFieldName)
internal fun DatabindException.wrapWithPath(refFrom: Any?, index: Int) = DatabindException.wrapWithPath(this, refFrom, index)

inline fun <reified T : Any> SimpleModule.addSerializer(kClass: KClass<T>, serializer: ValueSerializer<T>) = this.apply {
    addSerializer(kClass.java, serializer)
    addSerializer(kClass.javaObjectType, serializer)
}

inline fun <reified T : Any> SimpleModule.addDeserializer(kClass: KClass<T>, deserializer: ValueDeserializer<T>) = this.apply {
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

// In the future, value classes without @JvmInline will be available, and unboxing may not be able to handle it.
// https://github.com/FasterXML/jackson-module-kotlin/issues/464
// The JvmInline annotation can be added to Java classes,
// so the isKotlinClass decision is necessary (the order is preferable in terms of possible frequency).
internal fun Class<*>.isUnboxableValueClass() = annotations.any { it is JvmInline } && this.isKotlinClass()
