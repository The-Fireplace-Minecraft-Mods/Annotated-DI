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

    /**
     * Controls which environment the implementation is allowed to be used in.
     * If a loader-specific annotation is present, that will be used.
     * Leave blank for this implementation to be used in all environments.
     * <p>
     * Valid values are CLIENT and SERVER.
     */
    String environment() default "";

    /**
     * If this is not empty, this implementation will only be injectable if the given dependencies are present.
     */
    String[] dependencyModIds() default {};
}
