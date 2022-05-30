package dev.the_fireplace.annotateddi.impl.di;

import javax.annotation.Nullable;
import java.util.List;

public final class ImplementationData<T> {

    private final Class<T> implementation;
    private final List<Class<?>> interfaces;
    private final String name;
    private final boolean useAllInterfaces;
    @Nullable
    private final String environment;

    public ImplementationData(
        Class<T> implementation,
        List<Class<?>> interfaces,
        String name,
        boolean useAllInterfaces,
        @Nullable String environment
    ) {
        this.implementation = implementation;
        this.interfaces = interfaces;
        this.name = name;
        this.useAllInterfaces = useAllInterfaces;
        this.environment = environment;
    }

    public Class<T> implementation() {
        return implementation;
    }

    public List<Class<?>> interfaces() {
        return interfaces;
    }

    public String name() {
        return name;
    }

    public boolean useAllInterfaces() {
        return useAllInterfaces;
    }

    @Nullable
    public String environment() {
        return environment;
    }
}
