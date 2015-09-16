package com.fasterxml.jackson.module.kotlin

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonIgnore

internal abstract class RangeMixin<T : Comparable<T>> @JsonCreator constructor(override public val start: T, override public val end: T) : Range<T> {
    @JsonIgnore override abstract fun isEmpty(): Boolean
    @JsonIgnore abstract fun getIncrement(): T
}