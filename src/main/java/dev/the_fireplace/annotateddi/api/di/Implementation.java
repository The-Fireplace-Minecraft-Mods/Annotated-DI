package dev.the_fireplace.annotateddi.api.di;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
public @interface Implementation {
    Class<?>[] value() default {Object.class};
    String name() default "";
}
