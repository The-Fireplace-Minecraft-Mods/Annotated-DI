package dev.the_fireplace.annotateddi.impl.entrypoint;

import dev.the_fireplace.annotateddi.api.entrypoints.DIModInitializer;
import dev.the_fireplace.annotateddi.impl.di.FabricInjectorSetup;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.loader.api.FabricLoader;

public final class Main implements ModInitializer
{
    @Override
    public void onInitialize() {
        FabricInjectorSetup.init();
        FabricLoader.getInstance().getEntrypointContainers(
            "di-main",
            DIModInitializer.class
        ).forEach(entrypoint -> entrypoint.getEntrypoint().onInitialize());
    }
}
