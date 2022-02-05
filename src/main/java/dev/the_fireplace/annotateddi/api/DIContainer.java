package dev.the_fireplace.annotateddi.api;

import com.google.inject.Injector;
import dev.the_fireplace.annotateddi.impl.AnnotatedDIConstants;

public final class DIContainer
{
    public static Injector get() {
        return AnnotatedDIConstants.getInjector();
    }
}
