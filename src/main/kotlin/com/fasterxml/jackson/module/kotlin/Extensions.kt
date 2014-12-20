package com.fasterxml.jackson.module.kotlin

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.databind.MappingIterator
import java.io.File
import java.net.URL
import java.io.Reader
import java.io.InputStream
import com.fasterxml.jackson.core.TreeNode
import com.fasterxml.jackson.core.JsonProcessingException
import com.fasterxml.jackson.databind.ObjectReader

public fun jacksonObjectMapper(): ObjectMapper = ObjectMapper().registerKotlinModule()
public fun ObjectMapper.registerKotlinModule(): ObjectMapper = this.registerModule(KotlinModule())

public inline fun <reified T> ObjectMapper.readValue(jp: JsonParser): T = readValue(jp, javaClass<T>())
public inline fun <reified T> ObjectMapper.readValues(jp: JsonParser): MappingIterator<T> = readValues(jp, javaClass<T>())

public inline fun <reified T> ObjectMapper.readValue(src: File): T = readValue(src, javaClass<T>())
public inline fun <reified T> ObjectMapper.readValue(src: URL): T = readValue(src, javaClass<T>())
public inline fun <reified T> ObjectMapper.readValue(content: String): T = readValue(content, javaClass<T>())
public inline fun <reified T> ObjectMapper.readValue(src: Reader): T = readValue(src, javaClass<T>())
public inline fun <reified T> ObjectMapper.readValue(src: InputStream): T = readValue(src, javaClass<T>())
public inline fun <reified T> ObjectMapper.readValue(src: ByteArray): T = readValue(src, javaClass<T>())

public inline fun <reified T> ObjectMapper.treeToValue(n: TreeNode): T = treeToValue(n, javaClass<T>())
public inline fun <reified T> ObjectMapper.convertValue(from: Any): T = convertValue(from, javaClass<T>())

public inline fun <reified T> ObjectReader.readValue(jp: JsonParser): T = readValue(jp, javaClass<T>())
public inline fun <reified T> ObjectReader.readValues(jp: JsonParser): Iterator<T> = readValues(jp, javaClass<T>())
public inline fun <reified T> ObjectReader.treeToValue(n: TreeNode): T = treeToValue(n, javaClass<T>())

