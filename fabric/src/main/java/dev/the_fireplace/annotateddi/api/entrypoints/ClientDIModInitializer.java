package dev.the_fireplace.annotateddi.api.entrypoints;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Environment(EnvType.CLIENT)
public interface ClientDIModInitializer
{
    void onInitializeClient();
}
