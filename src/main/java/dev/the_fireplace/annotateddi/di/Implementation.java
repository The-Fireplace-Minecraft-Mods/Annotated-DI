package dev.the_fireplace.annotateddi.di;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
public @interface Implementation {
    Class<?>[] value() default {Object.class};
}
