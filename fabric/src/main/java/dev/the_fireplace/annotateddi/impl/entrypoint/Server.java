package dev.the_fireplace.annotateddi.impl.entrypoint;

import dev.the_fireplace.annotateddi.impl.di.FabricInjectorSetup;
import net.fabricmc.api.DedicatedServerModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.loader.api.FabricLoader;

@Environment(EnvType.SERVER)
public final class Server implements DedicatedServerModInitializer
{
    @Override
    public void onInitializeServer() {
        FabricInjectorSetup.init();
        FabricLoader.getInstance().getEntrypointContainers(
            "di-server",
            DedicatedServerModInitializer.class
        ).forEach(entrypoint -> entrypoint.getEntrypoint().onInitializeServer());
    }
}
