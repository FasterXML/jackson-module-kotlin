package com.fasterxml.jackson.module.kotlin

import com.fasterxml.jackson.databind.introspect.Annotated
import kotlinx.metadata.ClassName
import kotlinx.metadata.KmClass
import kotlinx.metadata.jvm.KotlinClassHeader
import kotlinx.metadata.jvm.KotlinClassMetadata

internal fun Metadata.toKotlinClassHeader() = KotlinClassHeader(
    this.kind,
    this.metadataVersion,
    this.data1,
    this.data2,
    this.extraString,
    this.packageName,
    this.extraInt
)

internal fun Metadata.toKmClassOrNull(): KmClass? =
    (KotlinClassMetadata.read(this.toKotlinClassHeader()) as? KotlinClassMetadata.Class)?.toKmClass()
internal fun Class<*>.toKmClassOrNull(): KmClass? = this.getAnnotation(Metadata::class.java)?.toKmClassOrNull()
internal fun Annotated.toKmClassOrNull(): KmClass? = this.getAnnotation(Metadata::class.java)?.toKmClassOrNull()

internal fun ClassName.toJavaClass(): Class<*> {
    return Class.forName(replace(".", "$").replace("/", "."))
}
