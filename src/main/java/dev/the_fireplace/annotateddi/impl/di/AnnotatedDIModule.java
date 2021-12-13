package dev.the_fireplace.annotateddi.impl.di;

import com.google.inject.AbstractModule;
import com.google.inject.name.Names;
import dev.the_fireplace.annotateddi.api.injectable.LogicalSideChecker;
import dev.the_fireplace.annotateddi.api.injectable.LogicalSidedThreadFactory;
import dev.the_fireplace.annotateddi.impl.logicalside.ClientLogicalSideChecker;
import dev.the_fireplace.annotateddi.impl.logicalside.DedicatedLogicalSide;
import dev.the_fireplace.annotateddi.impl.logicalside.LogicalSidedThreadFactoryImpl;
import dev.the_fireplace.annotateddi.impl.logicalside.StackTraceSide;
import net.fabricmc.api.EnvType;

import javax.annotation.Nullable;
import java.util.*;

@SuppressWarnings({"rawtypes", "unchecked"})
public final class AnnotatedDIModule extends AbstractModule {

    @Override
    protected void configure() {
        bindLogicalSideChecker();
        bind(LogicalSidedThreadFactory.class).to(LogicalSidedThreadFactoryImpl.class);

        Set<ImplementationContainer> implementations = new ImplementationScanner().findImplementations();
        bindImplementations(implementations);
    }

    private void bindLogicalSideChecker() {
        Class<? extends LogicalSideChecker> logicalSideChecker;
        EnvType currentSide = StackTraceSide.get();
        if (Objects.equals(currentSide, EnvType.SERVER)) {
            logicalSideChecker = DedicatedLogicalSide.class;
        } else if (Objects.equals(currentSide, EnvType.CLIENT)) {
            logicalSideChecker = ClientLogicalSideChecker.class;
        } else {
            throw new IllegalStateException("Cannot bind logical side checker from unknown side!");
        }
        bind(LogicalSideChecker.class).to(logicalSideChecker);
    }

    private void bindImplementations(Iterable<ImplementationContainer> modImplementations) {
        for (ImplementationContainer modImplementationData : modImplementations) {
            for (Map.Entry<Class, List<ImplementationData>> classImplementations : modImplementationData.implementations.entrySet()) {
                if (classImplementations.getValue().size() == 1) {
                    ImplementationData implementationData = classImplementations.getValue().get(0);
                    bindImplementationData(implementationData);
                } else if (canUseProxy(classImplementations.getValue())) {
                    bindProxy(classImplementations.getKey(), classImplementations.getValue());
                } else {
                    for (ImplementationData implementationData : classImplementations.getValue()) {
                        bindImplementationData(implementationData);
                    }
                }
            }
        }
    }

    private boolean canUseProxy(List<ImplementationData> implementationDatas) {
        boolean hasSide = false;
        boolean hasStandard = false;
        for (ImplementationData data : implementationDatas) {
            if (!data.name().isEmpty()) {
                continue;
            }
            if (data.environment != null) {
                hasSide = true;
            } else {
                hasStandard = true;
            }
            if (hasSide && hasStandard) {
                return true;
            }
        }

        return false;
    }

    private void bindProxy(Class implementationClass, List<ImplementationData> implementationDatas) {
        bind(implementationClass).toProvider(ProxyProviderFactory.getProvider(implementationClass, implementationDatas));
        for (ImplementationData implementationData : implementationDatas) {
            if (!implementationData.name().isEmpty()) {
                bindImplementationData(implementationData);
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
        if (name.isBlank()) {
            bind(injectableInterface).to(implementation);
        } else {
            bind(injectableInterface).annotatedWith(Names.named(name)).to(implementation);
        }
    }

    public record ImplementationContainer(String version, Map<Class, List<ImplementationData>> implementations)
    {
    }

    public record ImplementationData(
        Class implementation,
        List<Class> interfaces,
        String name,
        boolean useAllInterfaces,
        @Nullable EnvType environment
    )
    {
    }
}
