package dev.the_fireplace.annotateddi.impl.injector;

import dev.the_fireplace.annotateddi.api.di.Implementation;
import dev.the_fireplace.annotateddi.impl.di.ForgeImplementationScanner;
import dev.the_fireplace.annotateddi.impl.di.ImplementationScanner;
import net.minecraftforge.fml.loading.FMLEnvironment;

import javax.inject.Singleton;

@Singleton
@Implementation("dev.the_fireplace.annotateddi.impl.injector.InjectorFactory")
public final class ForgeInjectorFactory extends InjectorFactory
{
    private final ImplementationScanner implementationScanner = new ForgeImplementationScanner();

    @Override
    protected ImplementationScanner getImplementationScanner() {
        return implementationScanner;
    }

    @Override
    protected boolean isDevelopmentEnvironment() {
        return !FMLEnvironment.production;
    }
}
