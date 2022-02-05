package dev.the_fireplace.annotateddi.impl.di;

import com.google.inject.AbstractModule;
import com.google.inject.name.Names;
import net.fabricmc.api.EnvType;

import javax.annotation.Nullable;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;

@SuppressWarnings({"rawtypes", "unchecked"})
public final class AnnotatedDIModule extends AbstractModule
{
    @Override
    protected void configure() {
        Set<ImplementationContainer> implementations = new ImplementationScanner().findImplementations();
        bindImplementations(implementations);
    }

    private void bindImplementations(Iterable<ImplementationContainer> modImplementations) {
        for (ImplementationContainer modImplementationData : modImplementations) {
            for (Map.Entry<Class, List<ImplementationData>> classImplementations : modImplementationData.implementations.entrySet()) {
                if (classImplementations.getValue().size() == 1) {
                    ImplementationData implementationData = classImplementations.getValue().get(0);
                    bindImplementationData(implementationData);
                } else {
                    for (ImplementationData implementationData : classImplementations.getValue()) {
                        bindImplementationData(implementationData);
                    }
                }
            }
        }
    }

    private void bindImplementationData(ImplementationData implementationData) {
        bindImplementationToInterface(
            implementationData.implementation,
            implementationData.interfaces.toArray(new Class[0]),
            implementationData.name,
            implementationData.useAllInterfaces
        );
    }

    private void bindImplementationToInterface(Class implementation, Class[] injectableInterfaces, String name, boolean useAllInterfaces) {
        boolean hasExplicitBindings = !Arrays.equals(injectableInterfaces, new Class[]{Object.class});
        if (hasExplicitBindings) {
            bindToInterfaces(implementation, injectableInterfaces, name);
        } else {
            Class[] interfaces = implementation.getInterfaces();
            if (interfaces.length == 1) {
                bindWithOptionalName(interfaces[0], implementation, name);
            } else if (interfaces.length > 1) {
                if (useAllInterfaces) {
                    bindToInterfaces(implementation, interfaces, name);
                } else {
                    throw new ImplementationException(String.format("Multiple interfaces found for @Implementation annotated class %s, please set the value(s) to pick the correct one(s).", implementation.getCanonicalName()));
                }
            } else {
                throw new ImplementationException(String.format("No interfaces found for @Implementation annotated class %s, please set the value(s) to pick the correct one(s).", implementation.getCanonicalName()));
            }
        }
    }

    private void bindToInterfaces(Class implementation, Class[] injectableInterfaces, String name) {
        for (Class injectableInterface : injectableInterfaces) {
            bindWithOptionalName(injectableInterface, implementation, name);
        }
    }

    private void bindWithOptionalName(Class injectableInterface, Class implementation, String name) {
        if (name.trim().isEmpty()) {
            bind(injectableInterface).to(implementation);
        } else {
            bind(injectableInterface).annotatedWith(Names.named(name)).to(implementation);
        }
    }

    public static class ImplementationContainer {
        public final String version;
        public final Map<Class, List<ImplementationData>> implementations;

        public ImplementationContainer(String version, Map<Class, List<ImplementationData>> implementations) {
            this.version = version;
            this.implementations = implementations;
        }
    }

    public static class ImplementationData
    {
        public final Class implementation;
        public final List<Class> interfaces;
        public final String name;
        public final boolean useAllInterfaces;
        @Nullable
        public final EnvType environment;

        public ImplementationData(
            Class implementation,
            List<Class> interfaces,
            String name,
            boolean useAllInterfaces,
            @Nullable EnvType environment
        ) {
            this.implementation = implementation;
            this.interfaces = interfaces;
            this.name = name;
            this.useAllInterfaces = useAllInterfaces;
            this.environment = environment;
        }
    }
}
