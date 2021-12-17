package dev.the_fireplace.annotateddi.impl.logicalside;

import dev.the_fireplace.annotateddi.impl.AnnotatedDI;
import dev.the_fireplace.annotateddi.impl.logicalside.thread.ClientThread;
import dev.the_fireplace.annotateddi.impl.logicalside.thread.ServerThread;
import net.fabricmc.api.EnvType;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.MappingResolver;

import javax.annotation.Nullable;
import java.util.Set;

public final class StackTraceSide
{
    @Nullable
    public static EnvType get() {
        Thread currentThread = Thread.currentThread();
        if (currentThread instanceof ServerThread) {
            return EnvType.SERVER;
        }
        if (currentThread instanceof ClientThread) {
            return EnvType.CLIENT;
        }
        Set<String> clientNames = getClientNames();
        Set<String> serverNames = getServerNames();
        StackTraceElement[] stacktrace = currentThread.getStackTrace();
        for (StackTraceElement element : stacktrace) {
            String className = element.getClassName();
            if (clientNames.contains(className)) {
                return EnvType.CLIENT;
            }
            if (serverNames.contains(className)) {
                return EnvType.SERVER;
            }
        }

        AnnotatedDI.getLogger().warn("Thread side not found.", new Throwable("Stack Trace"));
        return null;
    }

    private static Set<String> getClientNames() {
        MappingResolver mappingResolver = FabricLoader.getInstance().getMappingResolver();
        return Set.of(
            mappingResolver.mapClassName("named", "net.minecraft.client.main.Main"),
            mappingResolver.mapClassName("named", "net.minecraft.client.MinecraftClient"),
            mappingResolver.mapClassName("named", "net.minecraft.client.network.ClientPlayNetworkHandler")
        );
    }

    private static Set<String> getServerNames() {
        MappingResolver mappingResolver = FabricLoader.getInstance().getMappingResolver();
        return Set.of(
            mappingResolver.mapClassName("named", "net.minecraft.server.Main"),
            mappingResolver.mapClassName("named", "net.minecraft.server.MinecraftServer"),
            mappingResolver.mapClassName("named", "net.minecraft.server.integrated.IntegratedServer"),
            mappingResolver.mapClassName("named", "net.minecraft.server.network.ServerPlayNetworkHandler"),
            mappingResolver.mapClassName("named", "net.minecraft.server.world.ThreadedAnvilChunkStorage"),
            mappingResolver.mapClassName("named", "net.minecraft.server.world.ChunkTaskPrioritySystem")
        );
    }
}
