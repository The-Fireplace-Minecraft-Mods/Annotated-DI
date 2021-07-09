package dev.the_fireplace.annotateddi.api.di;

public @interface Implementation {
    Class<?>[] value() default Object.class;
    String name() default "";
}
