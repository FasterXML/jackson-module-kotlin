package com.fasterxml.jackson.module.kotlin

import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.core.TreeNode
import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.MappingIterator
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.ObjectReader
import java.io.File
import java.io.InputStream
import java.io.Reader
import java.net.URL

public fun jacksonObjectMapper(): ObjectMapper = ObjectMapper().registerKotlinModule()
public fun ObjectMapper.registerKotlinModule(): ObjectMapper = this.registerModule(KotlinModule())

public inline fun <reified T: Any> ObjectMapper.readValue(jp: JsonParser): T = readValue(jp, object: TypeReference<T>() {})
public inline fun <reified T: Any> ObjectMapper.readValues(jp: JsonParser): MappingIterator<T> = readValues(jp, object: TypeReference<T>() {})

public inline fun <reified T: Any> ObjectMapper.readValue(src: File): T = readValue(src, object: TypeReference<T>() {})
public inline fun <reified T: Any> ObjectMapper.readValue(src: URL): T = readValue(src, object: TypeReference<T>() {})
public inline fun <reified T: Any> ObjectMapper.readValue(content: String): T = readValue(content, object: TypeReference<T>() {})
public inline fun <reified T: Any> ObjectMapper.readValue(src: Reader): T = readValue(src, object: TypeReference<T>() {})
public inline fun <reified T: Any> ObjectMapper.readValue(src: InputStream): T = readValue(src, object: TypeReference<T>() {})
public inline fun <reified T: Any> ObjectMapper.readValue(src: ByteArray): T = readValue(src, object: TypeReference<T>() {})

public inline fun <reified T: Any> ObjectMapper.treeToValue(n: TreeNode): T = treeToValue(n, T::class.java)
public inline fun <reified T: Any> ObjectMapper.convertValue(from: Any): T = convertValue(from, object: TypeReference<T>() {})

public inline fun <reified T: Any> ObjectReader.readValue(jp: JsonParser): T = readValue(jp, object: TypeReference<T>() {})
public inline fun <reified T: Any> ObjectReader.readValues(jp: JsonParser): Iterator<T> = readValues(jp, object: TypeReference<T>() {})
public inline fun <reified T: Any> ObjectReader.treeToValue(n: TreeNode): T = treeToValue(n, T::class.java)
