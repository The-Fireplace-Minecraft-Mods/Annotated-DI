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
     * When an interface is defined with a specific {@link net.fabricmc.api.Environment} and another implementation exists for the common side,
     * use the logical side as the proxy. This is mostly relevant in Single Player where the client and integrated server are together.
     * With this on, we would get CLIENT -> ClientImplementation and COMMON -> CommonImplementation.
     * With this off, we would get CLIENT -> ClientImplementation and COMMON -> ClientImplementation because we are physically on the client still.
     */
    boolean useLogicalProxy() default true;
}
