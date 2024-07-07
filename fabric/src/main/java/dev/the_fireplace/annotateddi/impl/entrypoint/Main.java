package dev.the_fireplace.annotateddi.impl.entrypoint;

import dev.the_fireplace.annotateddi.impl.di.FabricInjectorSetup;
import dev.the_fireplace.annotateddi.impl.injector.AnnotatedDIInjectorLoader;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.loader.api.FabricLoader;

public final class Main implements ModInitializer
{
    @Override
    public void onInitialize() {
        AnnotatedDIInjectorLoader.setDevelopmentEnvironment(FabricLoader.getInstance().isDevelopmentEnvironment());
        FabricInjectorSetup.init();
        FabricLoader.getInstance().getEntrypointContainers(
            "di-main",
            ModInitializer.class
        ).forEach(entrypoint -> entrypoint.getEntrypoint().onInitialize());
    }
}
