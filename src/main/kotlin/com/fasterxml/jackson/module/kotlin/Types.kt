package com.fasterxml.jackson.module.kotlin

import kotlin.reflect.KType
import kotlin.reflect.jvm.jvmErasure

internal fun KType.erasedType(): Class<out Any> = this.jvmErasure.java
