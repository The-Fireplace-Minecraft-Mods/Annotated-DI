package dev.the_fireplace.annotateddi.impl.entrypoint;

import com.google.inject.Injector;
import dev.the_fireplace.annotateddi.api.DIContainer;
import dev.the_fireplace.annotateddi.api.entrypoints.DIModInitializer;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.loader.api.FabricLoader;

public final class Main implements ModInitializer
{
    @Override
    public void onInitialize() {
        Injector container = DIContainer.get();
        FabricLoader.getInstance().getEntrypointContainers("di-main", DIModInitializer.class)
            .forEach((entrypoint) -> entrypoint.getEntrypoint().onInitialize(container));
    }
}
