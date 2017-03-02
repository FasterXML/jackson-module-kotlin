package com.fasterxml.jackson.module.kotlin

import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.core.TreeNode
import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.JsonMappingException
import com.fasterxml.jackson.databind.MappingIterator
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.ObjectReader
import java.io.File
import java.io.InputStream
import java.io.Reader
import java.net.URL

fun jacksonObjectMapper(): ObjectMapper = ObjectMapper().registerKotlinModule()
fun ObjectMapper.registerKotlinModule(): ObjectMapper = this.registerModule(KotlinModule())

inline fun <reified T: Any> jacksonTypeRef(): TypeReference<T> = object: TypeReference<T>() {}

inline fun <reified T: Any> ObjectMapper.readValue(jp: JsonParser): T = readValue(jp, jacksonTypeRef<T>())
inline fun <reified T: Any> ObjectMapper.readValues(jp: JsonParser): MappingIterator<T> = readValues(jp, jacksonTypeRef<T>())

inline fun <reified T: Any> ObjectMapper.readValue(src: File): T = readValue(src, jacksonTypeRef<T>())
inline fun <reified T: Any> ObjectMapper.readValue(src: URL): T = readValue(src, jacksonTypeRef<T>())
inline fun <reified T: Any> ObjectMapper.readValue(content: String): T = readValue(content, jacksonTypeRef<T>())
inline fun <reified T: Any> ObjectMapper.readValue(src: Reader): T = readValue(src, jacksonTypeRef<T>())
inline fun <reified T: Any> ObjectMapper.readValue(src: InputStream): T = readValue(src, jacksonTypeRef<T>())
inline fun <reified T: Any> ObjectMapper.readValue(src: ByteArray): T = readValue(src, jacksonTypeRef<T>())

inline fun <reified T: Any> ObjectMapper.treeToValue(n: TreeNode): T = treeToValue(n, T::class.java)
inline fun <reified T: Any> ObjectMapper.convertValue(from: Any): T = convertValue(from, jacksonTypeRef<T>())

// TODO: are these shadowed functions impossible to then call?  variation named functions are below but is uglier.
@Suppress("EXTENSION_SHADOWED_BY_MEMBER")
inline fun <reified T: Any> ObjectReader.readValue(jp: JsonParser): T = readValue(jp, jacksonTypeRef<T>())
@Suppress("EXTENSION_SHADOWED_BY_MEMBER")
inline fun <reified T: Any> ObjectReader.readValues(jp: JsonParser): Iterator<T> = readValues(jp, jacksonTypeRef<T>())

inline fun <reified T: Any> ObjectReader.readValueTyped(jp: JsonParser): T = readValue(jp, jacksonTypeRef<T>())
inline fun <reified T: Any> ObjectReader.readValuesTyped(jp: JsonParser): Iterator<T> = readValues(jp, jacksonTypeRef<T>())
inline fun <reified T: Any> ObjectReader.treeToValue(n: TreeNode): T = treeToValue(n, T::class.java)


internal fun JsonMappingException.wrapWithPath(refFrom: Any?, refFieldName: String) = JsonMappingException.wrapWithPath(this, refFrom, refFieldName)
internal fun JsonMappingException.wrapWithPath(refFrom: Any?, index: Int) = JsonMappingException.wrapWithPath(this, refFrom, index)