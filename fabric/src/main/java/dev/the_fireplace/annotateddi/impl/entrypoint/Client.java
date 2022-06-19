package dev.the_fireplace.annotateddi.impl.entrypoint;

import dev.the_fireplace.annotateddi.impl.di.FabricInjectorSetup;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.loader.api.FabricLoader;

@Environment(EnvType.CLIENT)
public final class Client implements ClientModInitializer
{
    @Override
    public void onInitializeClient() {
        FabricInjectorSetup.init();
        FabricLoader.getInstance().getEntrypointContainers(
            "di-client",
            ClientModInitializer.class
        ).forEach(entrypoint -> entrypoint.getEntrypoint().onInitializeClient());
    }
}
