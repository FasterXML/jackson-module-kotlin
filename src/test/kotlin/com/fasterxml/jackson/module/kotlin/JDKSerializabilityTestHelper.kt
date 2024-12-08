package com.fasterxml.jackson.module.kotlin

import org.junit.jupiter.api.Assertions.fail
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.ObjectInputStream
import java.io.ObjectOutputStream

fun jdkSerialize(o: Any): ByteArray {
    val bytes = ByteArrayOutputStream(1000)
    val obOut = ObjectOutputStream(bytes)
    obOut.writeObject(o)
    obOut.close()
    return bytes.toByteArray()
}

fun <T> jdkDeserialize(raw: ByteArray): T {
    val objIn = ObjectInputStream(ByteArrayInputStream(raw))
    return try {
        @Suppress("UNCHECKED_CAST")
        objIn.readObject() as T
    } catch (e: ClassNotFoundException) {
        fail("Missing class: " + e.message)
    } finally {
        objIn.close()
    }
}
