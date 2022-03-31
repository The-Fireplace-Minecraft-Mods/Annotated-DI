package dev.the_fireplace.annotateddi.impl.di;

import java.util.List;
import java.util.Map;

public record ImplementationContainer(
    String version,
    Map<Class, List<ImplementationData>> implementations
) {
}
