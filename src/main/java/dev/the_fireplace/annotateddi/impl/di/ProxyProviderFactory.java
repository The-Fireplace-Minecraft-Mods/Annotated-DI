package dev.the_fireplace.annotateddi.impl.di;

import dev.the_fireplace.annotateddi.api.DIContainer;
import dev.the_fireplace.annotateddi.impl.logicalside.StackTraceSide;
import net.fabricmc.api.EnvType;

import javax.inject.Provider;
import java.util.List;

public class ProxyProviderFactory
{
    public static <T> Provider<T> getProvider(Class<? extends T> providesClass, List<AnnotatedDIModule.ImplementationData> implementations) {
        return new ProxyProvider<>(implementations);
    }

    private static class ProxyProvider<T> implements Provider<T>
    {
        private AnnotatedDIModule.ImplementationData fallbackImplementation = null;
        private AnnotatedDIModule.ImplementationData clientImplementation = null;
        private AnnotatedDIModule.ImplementationData dedicatedServerImplementation = null;

        private ProxyProvider(List<AnnotatedDIModule.ImplementationData> implementations) {
            for (AnnotatedDIModule.ImplementationData implementationData : implementations) {
                if (!implementationData.name().isEmpty()) {
                    continue;
                }
                EnvType environment = implementationData.environment();
                if (environment == null) {
                    fallbackImplementation = implementationData;
                } else if (environment == EnvType.CLIENT) {
                    clientImplementation = implementationData;
                } else if (environment == EnvType.SERVER) {
                    dedicatedServerImplementation = implementationData;
                }
            }
        }

        @Override
        public T get() {
            EnvType logicalSide = StackTraceSide.get();
            AnnotatedDIModule.ImplementationData implementation = fallbackImplementation;
            if (logicalSide == EnvType.CLIENT && clientImplementation != null) {
                implementation = clientImplementation;
            }
            if (logicalSide == EnvType.SERVER && dedicatedServerImplementation != null) {
                implementation = dedicatedServerImplementation;
            }
            //noinspection unchecked
            return (T) DIContainer.get().getInstance(implementation.implementation());
        }
    }
}
