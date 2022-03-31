package dev.the_fireplace.annotateddi.impl.di;

import javax.annotation.Nullable;
import java.util.List;

public record ImplementationData<T>(
    Class<T> implementation,
    List<Class<?>> interfaces,
    String name,
    boolean useAllInterfaces,
    @Nullable String environment
) {
}
