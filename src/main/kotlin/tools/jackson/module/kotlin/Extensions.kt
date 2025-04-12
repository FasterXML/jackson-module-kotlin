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

// region: Do not remove the default argument for functions that take a builder as an argument for compatibility.
//   The default argument can be removed in 2.21 or later. See #775 for the history.
fun jacksonObjectMapper(): ObjectMapper = jsonMapper { addModule(kotlinModule()) }
fun jacksonObjectMapper(initializer: KotlinModule.Builder.() -> Unit = {}): ObjectMapper =
    jsonMapper { addModule(kotlinModule(initializer)) }

fun jacksonMapperBuilder(): JsonMapper.Builder = JsonMapper.builder().addModule(kotlinModule())
fun jacksonMapperBuilder(initializer: KotlinModule.Builder.() -> Unit = {}): JsonMapper.Builder =
    JsonMapper.builder().addModule(kotlinModule(initializer))

// 22-Jul-2019, tatu: Can not be implemented same way as in 2.x, addition via mapper.builder():
// fun ObjectMapper.registerKotlinModule(): ObjectMapper = this.registerModule(kotlinModule())
// fun ObjectMapper.registerKotlinModule(initializer: KotlinModule.Builder.() -> Unit = {}): ObjectMapper =
//     this.registerModule(kotlinModule(initializer))
// endregion

inline fun <reified T> jacksonTypeRef(): TypeReference<T> = object : TypeReference<T>() {}

/**
 * It is public due to Kotlin restrictions, but should not be used externally.
 */
inline fun <reified T> Any?.checkTypeMismatch(): T {
    // Basically, this check assumes that T is non-null and the value is null.
    // Since this can be caused by both input or ObjectMapper implementation errors,
    // a more abstract DatabindException is thrown.
    if (this !is T) {
        val nullability = if (null is T) "?" else "(non-null)"

        throw DatabindException.from(
            null as JsonParser?,
            "Deserialized value did not match the specified type; " +
                    "specified ${T::class.qualifiedName}${nullability} but was ${this?.let { it::class.qualifiedName }}"
        )
    }
    return this
}

/**
 * Shorthand for [ObjectMapper.readValue].
 * @throws DatabindException Especially if [T] is non-null and the value read is null.
 *   Other cases where the read value is of a different type than [T]
 *   due to an incorrect customization to [ObjectMapper].
 */
inline fun <reified T> ObjectMapper.readValue(jp: JsonParser): T = readValue(jp, jacksonTypeRef<T>())
    .checkTypeMismatch()
/**
 * Shorthand for [ObjectMapper.readValues].
 * @throws DatabindException Especially if [T] is non-null and the value read is null.
 *   Other cases where the read value is of a different type than [T]
 *   due to an incorrect customization to [ObjectMapper].
 */
inline fun <reified T> ObjectMapper.readValues(jp: JsonParser): MappingIterator<T> {
    val values = readValues(jp, jacksonTypeRef<T>())

    return object : MappingIterator<T>(values) {
        override fun nextValue(): T = super.nextValue().checkTypeMismatch()
    }
}

/**
 * Shorthand for [ObjectMapper.readValue].
 * @throws DatabindException Especially if [T] is non-null and the value read is null.
 *   Other cases where the read value is of a different type than [T]
 *   due to an incorrect customization to [ObjectMapper].
 */
inline fun <reified T> ObjectMapper.readValue(src: File): T = readValue(src, jacksonTypeRef<T>()).checkTypeMismatch()
/**
 * Shorthand for [ObjectMapper.readValue].
 * @throws DatabindException Especially if [T] is non-null and the value read is null.
 *   Other cases where the read value is of a different type than [T]
 *   due to an incorrect customization to [ObjectMapper].
 */
inline fun <reified T> ObjectMapper.readValue(src: URL): T = readValue(src, jacksonTypeRef<T>()).checkTypeMismatch()
/**
 * Shorthand for [ObjectMapper.readValue].
 * @throws DatabindException Especially if [T] is non-null and the value read is null.
 *   Other cases where the read value is of a different type than [T]
 *   due to an incorrect customization to [ObjectMapper].
 */
inline fun <reified T> ObjectMapper.readValue(content: String): T = readValue(content, jacksonTypeRef<T>())
    .checkTypeMismatch()
/**
 * Shorthand for [ObjectMapper.readValue].
 * @throws DatabindException Especially if [T] is non-null and the value read is null.
 *   Other cases where the read value is of a different type than [T]
 *   due to an incorrect customization to [ObjectMapper].
 */
inline fun <reified T> ObjectMapper.readValue(src: Reader): T = readValue(src, jacksonTypeRef<T>()).checkTypeMismatch()
/**
 * Shorthand for [ObjectMapper.readValue].
 * @throws DatabindException Especially if [T] is non-null and the value read is null.
 *   Other cases where the read value is of a different type than [T]
 *   due to an incorrect customization to [ObjectMapper].
 */
inline fun <reified T> ObjectMapper.readValue(src: InputStream): T = readValue(src, jacksonTypeRef<T>())
    .checkTypeMismatch()
/**
 * Shorthand for [ObjectMapper.readValue].
 * @throws DatabindException Especially if [T] is non-null and the value read is null.
 *   Other cases where the read value is of a different type than [T]
 *   due to an incorrect customization to [ObjectMapper].
 */
inline fun <reified T> ObjectMapper.readValue(src: ByteArray): T = readValue(src, jacksonTypeRef<T>())
    .checkTypeMismatch()

/**
 * Shorthand for [ObjectMapper.readValue].
 * @throws DatabindException Especially if [T] is non-null and the value read is null.
 *   Other cases where the read value is of a different type than [T]
 *   due to an incorrect customization to [ObjectMapper].
 */
inline fun <reified T> ObjectMapper.treeToValue(n: TreeNode): T = readValue(this.treeAsTokens(n), jacksonTypeRef<T>())
    .checkTypeMismatch()
/**
 * Shorthand for [ObjectMapper.convertValue].
 * @throws DatabindException Especially if [T] is non-null and the value read is null.
 *   Other cases where the read value is of a different type than [T]
 *   due to an incorrect customization to [ObjectMapper].
 */
inline fun <reified T> ObjectMapper.convertValue(from: Any?): T = convertValue(from, jacksonTypeRef<T>())
    .checkTypeMismatch()

/**
 * Shorthand for [ObjectReader.forType].
 * @throws DatabindException Especially if [T] is non-null and the value read is null.
 *   Other cases where the read value is of a different type than [T]
 *   due to an incorrect customization to [ObjectMapper].
 */
inline fun <reified T> ObjectReader.readValueTyped(jp: JsonParser): T = forType(jacksonTypeRef<T>()).readValue<T>(jp)
    .checkTypeMismatch()
/**
 * Shorthand for [ObjectReader.readValues].
 * @throws DatabindException Especially if [T] is non-null and the value read is null.
 *   Other cases where the read value is of a different type than [T]
 *   due to an incorrect customization to [ObjectMapper].
 */
inline fun <reified T> ObjectReader.readValuesTyped(jp: JsonParser): Iterator<T> {
    val values = readValues(jp, jacksonTypeRef<T>())

    return object : Iterator<T> by values {
        override fun next(): T = values.next().checkTypeMismatch<T>()
    }
}
inline fun <reified T> ObjectReader.treeToValue(jp: TreeNode): T? = forType(jacksonTypeRef<T>()).readValue(this.treeAsTokens(jp))

inline fun <reified T, reified U> JsonMapper.Builder.addMixIn(): JsonMapper.Builder = this.addMixIn(T::class.java, U::class.java)

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

inline fun <reified T : Any> SimpleModule.addSerializer(kClass: KClass<T>, serializer: ValueSerializer<T>) = this.apply {
    kClass.javaPrimitiveType?.let { addSerializer(it, serializer) }
    addSerializer(kClass.javaObjectType, serializer)
}

inline fun <reified T : Any> SimpleModule.addDeserializer(kClass: KClass<T>, deserializer: ValueDeserializer<T>) = this.apply {
    kClass.javaPrimitiveType?.let { addDeserializer(it, deserializer) }
    addDeserializer(kClass.javaObjectType, deserializer)
}
