package dev.the_fireplace.annotateddi.api.di;

import java.lang.annotation.ElementType;
import java.lang.annotation.Target;

@Target(ElementType.TYPE)
public @interface Implementation {
    String[] value() default "";
    String name() default "";
}
