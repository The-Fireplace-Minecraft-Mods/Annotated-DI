package dev.the_fireplace.annotateddi.impl.di;

import dev.the_fireplace.annotateddi.api.di.Implementation;
import dev.the_fireplace.annotateddi.impl.injector.InjectorFactory;
import net.fabricmc.loader.api.FabricLoader;

import javax.inject.Singleton;

@Singleton
@Implementation("dev.the_fireplace.annotateddi.impl.injector.InjectorFactory")
public final class FabricInjectorFactory extends InjectorFactory
{
    private final ImplementationScanner implementationScanner = new FabricImplementationScanner();

    @Override
    protected ImplementationScanner getImplementationScanner() {
        return implementationScanner;
    }

    @Override
    protected boolean isDevelopmentEnvironment() {
        return FabricLoader.getInstance().isDevelopmentEnvironment();
    }
}
