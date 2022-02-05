package dev.the_fireplace.annotateddi.api.di;

import java.lang.annotation.ElementType;
import java.lang.annotation.Target;

@Target(ElementType.TYPE)
public @interface Implementation
{
    String[] value() default "";

    String name() default "";

    /**
     * If set to true, this will act as the implementation for all interfaces declared, as opposed to the default behavior of having to explicitly declare which to implement when there are multiple.
     */
    boolean allInterfaces() default false;
}
