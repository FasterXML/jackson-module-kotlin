package com.fasterxml.jackson.module.kotlin

import com.fasterxml.jackson.databind.Module

internal object KotlinModuleVersionAdapter {
    fun configureModuleSetupContext(context: Module.SetupContext) {
        context.addDeserializers(KotlinDeserializers_1_5())
        context.addKeyDeserializers(KotlinKeyDeserializers)
    }
}
