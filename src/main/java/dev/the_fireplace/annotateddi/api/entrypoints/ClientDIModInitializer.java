package dev.the_fireplace.annotateddi.api.entrypoints;

import com.google.inject.Injector;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Environment(EnvType.CLIENT)
public interface ClientDIModInitializer {
    void onInitializeClient(Injector diContainer);
}
