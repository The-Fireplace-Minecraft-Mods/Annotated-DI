package dev.the_fireplace.annotateddi.impl;

import com.google.inject.Injector;
import dev.the_fireplace.annotateddi.api.DIContainer;
import dev.the_fireplace.annotateddi.api.entrypoints.DedicatedServerDIModInitializer;
import net.fabricmc.api.DedicatedServerModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.loader.api.FabricLoader;

@Environment(EnvType.SERVER)
public final class AnnotatedDIServer implements DedicatedServerModInitializer {
    @Override
    public void onInitializeServer() {
        Injector container = DIContainer.get();
        FabricLoader.getInstance().getEntrypointContainers("di-server", DedicatedServerDIModInitializer.class)
            .forEach((entrypoint) -> entrypoint.getEntrypoint().onInitializeServer(container));
    }
}
