package com.fasterxml.jackson.module.kotlin

import com.fasterxml.jackson.databind.ObjectMapper

public fun ObjectMapper.registerKotlinModule(): ObjectMapper = this.registerModule(KotlinModule())!!
public fun jacksonObjectMapper(): ObjectMapper = ObjectMapper().registerKotlinModule()