package com.fasterxml.jackson.module.kotlin

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.annotation.JsonProperty

internal abstract class ClosedRangeMixin<T> @JsonCreator constructor(public val start: T, @get:JsonProperty("end") public val endInclusive: T)  {
    @JsonIgnore abstract public fun getEnd(): T
    @JsonIgnore abstract public fun getFirst(): T
    @JsonIgnore abstract public fun getLast(): T
    @JsonIgnore abstract public fun getIncrement(): T
    @JsonIgnore abstract public fun isEmpty(): Boolean
    @JsonIgnore abstract public fun getStep(): T
}
