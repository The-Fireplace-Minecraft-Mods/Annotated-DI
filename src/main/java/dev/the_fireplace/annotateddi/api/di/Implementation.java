package dev.the_fireplace.annotateddi.api.di;

import java.lang.annotation.ElementType;
import java.lang.annotation.Target;

@Target(ElementType.TYPE)
public @interface Implementation {
    Class<?>[] value() default Object.class;
    String name() default "";
}
