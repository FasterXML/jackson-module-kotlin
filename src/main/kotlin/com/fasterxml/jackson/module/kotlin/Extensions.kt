package com.fasterxml.jackson.module.kotlin

import com.fasterxml.jackson.core.JsonFactory
import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.core.TreeNode
import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.MappingIterator
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.ObjectReader
import com.fasterxml.jackson.databind.deser.DefaultDeserializationContext
import com.fasterxml.jackson.databind.ser.DefaultSerializerProvider
import java.io.File
import java.io.InputStream
import java.io.Reader
import java.net.URL

fun jacksonObjectMapper(): ObjectMapper = ObjectMapper().registerKotlinModule()
fun jacksonObjectMapper(jf: JsonFactory? = null, sp : DefaultSerializerProvider? = null, dc: DefaultDeserializationContext? = null) = ObjectMapper(jf, sp, dc).registerKotlinModule()
fun ObjectMapper.registerKotlinModule(): ObjectMapper = registerModule(KotlinModule())

inline fun <reified T: Any> ObjectMapper.readValue(jp: JsonParser): T = readValue(jp, object: TypeReference<T>() {})
inline fun <reified T: Any> ObjectMapper.readValues(jp: JsonParser): MappingIterator<T> = readValues(jp, object: TypeReference<T>() {})

inline fun <reified T: Any> ObjectMapper.readValue(src: File): T = readValue(src, object: TypeReference<T>() {})
inline fun <reified T: Any> ObjectMapper.readValue(src: URL): T = readValue(src, object: TypeReference<T>() {})
inline fun <reified T: Any> ObjectMapper.readValue(content: String): T = readValue(content, object: TypeReference<T>() {})
inline fun <reified T: Any> ObjectMapper.readValue(src: Reader): T = readValue(src, object: TypeReference<T>() {})
inline fun <reified T: Any> ObjectMapper.readValue(src: InputStream): T = readValue(src, object: TypeReference<T>() {})
inline fun <reified T: Any> ObjectMapper.readValue(src: ByteArray): T = readValue(src, object: TypeReference<T>() {})

inline fun <reified T: Any> ObjectMapper.treeToValue(n: TreeNode): T = treeToValue(n, T::class.java)
inline fun <reified T: Any> ObjectMapper.convertValue(from: Any): T = convertValue(from, object: TypeReference<T>() {})

inline fun <reified T: Any> ObjectReader.readValue(jp: JsonParser): T = readValue(jp, object: TypeReference<T>() {})
inline fun <reified T: Any> ObjectReader.readValues(jp: JsonParser): Iterator<T> = readValues(jp, object: TypeReference<T>() {})
inline fun <reified T: Any> ObjectReader.treeToValue(n: TreeNode): T = treeToValue(n, T::class.java)
