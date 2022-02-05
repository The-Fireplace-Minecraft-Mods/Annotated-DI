package dev.the_fireplace.annotateddi.impl.entrypoint;

import com.google.inject.Injector;
import dev.the_fireplace.annotateddi.api.DIContainer;
import dev.the_fireplace.annotateddi.api.entrypoints.ClientDIModInitializer;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.loader.api.FabricLoader;

@Environment(EnvType.CLIENT)
public final class Client implements ClientModInitializer
{
    @Override
    public void onInitializeClient() {
        Injector container = DIContainer.get();
        FabricLoader.getInstance().getEntrypointContainers("di-client", ClientDIModInitializer.class)
            .forEach((entrypoint) -> entrypoint.getEntrypoint().onInitializeClient(container));
    }
}
