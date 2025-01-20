// Kotlin module-info for Main artifact
module com.fasterxml.jackson.kotlin
{
    requires java.desktop;

    requires kotlin.reflect;
    requires kotlin.stdlib;

    requires com.fasterxml.jackson.annotation;
    requires tools.jackson.databind;

    exports tools.jackson.module.kotlin;

    // No 0-arg constructor, cannot register as a Service via SPI
    //provides tools.jackson.databind.JacksonModule with
    //    tools.jackson.module.kotlin.KotlinModule;
}
