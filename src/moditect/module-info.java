// Manually created 02-Nov-2020 for
//   https://github.tools/jackson-module-kotlin/issues/385
module tools.jackson.kotlin {
    requires java.desktop;

    requires kotlin.reflect;
    requires kotlin.stdlib;

    requires tools.jackson.annotation;
    requires tools.jackson.databind;

    exports tools.jackson.module.kotlin;

    provides tools.jackson.databind.Module with
        tools.jackson.module.kotlin.KotlinModule;
}
