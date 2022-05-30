package dev.the_fireplace.annotateddi.impl.di;

import java.util.List;
import java.util.Map;

public final class ImplementationContainer {
    private final String version;
    private final Map<Class, List<ImplementationData>> implementations;

    public ImplementationContainer(
        String version,
        Map<Class, List<ImplementationData>> implementations
    ) {
        this.version = version;
        this.implementations = implementations;
    }

    public String version() {
        return version;
    }

    public Map<Class, List<ImplementationData>> implementations() {
        return implementations;
    }
}
