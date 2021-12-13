package dev.the_fireplace.annotateddi.impl.logicalside;

import dev.the_fireplace.annotateddi.api.injectable.LogicalSideChecker;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.server.MinecraftServer;

import javax.annotation.Nullable;

@Environment(EnvType.CLIENT)
public final class ClientLogicalSideChecker implements LogicalSideChecker
{
    private final MinecraftClient client;
    @Nullable
    private final MinecraftServer server;

    public ClientLogicalSideChecker() {
        this.client = MinecraftClient.getInstance();
        this.server = client.getServer();
    }

    @Override
    public @Nullable
    EnvType getLogicalSide() {
        if (server == null || client.isOnThread()) {
            return EnvType.CLIENT;
        }
        if (server.isOnThread()) {
            return EnvType.SERVER;
        }
        return StackTraceSide.get();
    }
}
