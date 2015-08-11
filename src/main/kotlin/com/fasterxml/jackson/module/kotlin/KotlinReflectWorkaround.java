package com.fasterxml.jackson.module.kotlin;


import kotlin.reflect.jvm.internal.KClassImpl;
import kotlin.reflect.jvm.internal.impl.descriptors.ClassDescriptor;

// see https://youtrack.jetbrains.com/issue/KT-7807
public class KotlinReflectWorkaround {
    static ClassDescriptor getClassDescriptor(KClassImpl<?> klass) {
        return klass.getDescriptor();
    }

    static ClassDescriptor getCompanionClassDescriptor(KClassImpl<?> klass) {
        ClassDescriptor mine = klass.getDescriptor();
        if (mine != null) {
            return mine.getCompanionObjectDescriptor();
        } else {
            return null;
        }

    }
}
