package com.fasterxml.jackson.module.kotlin

import com.fasterxml.jackson.databind.ObjectMapper

public fun jacksonObjectMapper(): ObjectMapper = ObjectMapper().registerKotlinModule()
public fun ObjectMapper.registerKotlinModule(): ObjectMapper = this.registerModule(KotlinModule())
