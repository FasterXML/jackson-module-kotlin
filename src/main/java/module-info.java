// Kotlin module-info for Main artifact
module tools.jackson.module.kotlin
{
    requires kotlin.reflect;
    requires transitive kotlin.stdlib;
    requires org.jetbrains.annotations;

    requires com.fasterxml.jackson.annotation;
    requires tools.jackson.core;
    requires transitive tools.jackson.databind;

    exports tools.jackson.module.kotlin;

    // No 0-arg constructor, cannot register as a Service via SPI
    //provides tools.jackson.databind.JacksonModule with
    //    tools.jackson.module.kotlin.KotlinModule;
}
