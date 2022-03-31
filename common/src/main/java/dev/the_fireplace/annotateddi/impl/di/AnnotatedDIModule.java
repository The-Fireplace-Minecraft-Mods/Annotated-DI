package dev.the_fireplace.annotateddi.impl.di;

import com.google.inject.AbstractModule;
import com.google.inject.name.Names;

import java.util.*;

@SuppressWarnings({"rawtypes", "unchecked"})
public final class AnnotatedDIModule extends AbstractModule
{
    private final ImplementationContainer implementationContainer;

    public AnnotatedDIModule(ImplementationContainer implementationContainer) {
        this.implementationContainer = implementationContainer;
    }

    @Override
    protected void configure() {
        bindImplementation();
    }

    private void bindImplementation() {
        for (Map.Entry<Class, List<ImplementationData>> classImplementations : implementationContainer.implementations().entrySet()) {
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

    private void bindImplementationData(ImplementationData implementationData) {
        bindImplementationToInterface(
            implementationData.implementation(),
            implementationData.interfaces(),
            implementationData.name(),
            implementationData.useAllInterfaces()
        );
    }

    private void bindImplementationToInterface(Class implementation, List<Class> injectableInterfaces, String name, boolean useAllInterfaces) {
        boolean hasExplicitBindings = injectableInterfaces.size() != 1 || !injectableInterfaces.get(0).equals(Object.class);
        if (hasExplicitBindings) {
            bindToInterfaces(implementation, injectableInterfaces, name);
        } else {
            Class[] interfaces = implementation.getInterfaces();
            if (interfaces.length == 1) {
                bindWithOptionalName(interfaces[0], implementation, name);
            } else if (interfaces.length > 1) {
                if (useAllInterfaces) {
                    bindToInterfaces(implementation, List.of(interfaces), name);
                } else {
                    throw new ImplementationException(String.format("Multiple interfaces found for @Implementation annotated class %s, please set the value(s) to pick the correct one(s).", implementation.getCanonicalName()));
                }
            } else {
                throw new ImplementationException(String.format("No interfaces found for @Implementation annotated class %s, please set the value(s) to pick the correct one(s).", implementation.getCanonicalName()));
            }
        }
    }

    private void bindToInterfaces(Class implementation, List<Class> injectableInterfaces, String name) {
        for (Class injectableInterface : injectableInterfaces) {
            bindWithOptionalName(injectableInterface, implementation, name);
        }
    }

    private void bindWithOptionalName(Class injectableInterface, Class implementation, String name) {
        if (name.isBlank()) {
            bind(injectableInterface).to(implementation);
        } else {
            bind(injectableInterface).annotatedWith(Names.named(name)).to(implementation);
        }
    }
}
