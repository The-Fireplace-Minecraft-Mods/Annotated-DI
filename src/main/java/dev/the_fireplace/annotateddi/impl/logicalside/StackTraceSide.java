package dev.the_fireplace.annotateddi.impl.logicalside;

import com.google.common.collect.Sets;
import dev.the_fireplace.annotateddi.impl.AnnotatedDI;
import dev.the_fireplace.annotateddi.impl.logicalside.thread.ClientThread;
import dev.the_fireplace.annotateddi.impl.logicalside.thread.ServerThread;
import net.fabricmc.api.EnvType;

import javax.annotation.Nullable;
import java.util.Set;

public final class StackTraceSide
{
    //TODO Any more names to add? Netty? Worldgen? Intermediary names from prod?
    private static final Set<String> clientNames = Sets.newHashSet(
        "net.minecraft.client.main.Main",
        "net.minecraft.client.MinecraftClient",
        "net.minecraft.class_310",
        "dxo",
        "net.minecraft.client.network.ClientPlayNetworkHandler",
        "net.minecraft.class_634",
        "elm"
    );
    private static final Set<String> serverNames = Sets.newHashSet(
        "net.minecraft.server.Main",
        "net.minecraft.server.MinecraftServer",
        "net.minecraft.server.network.ServerPlayNetworkHandler",
        "net.minecraft.class_3244",
        "aea"
    );

    @Nullable
    public static EnvType get() {
        Thread currentThread = Thread.currentThread();
        if (currentThread instanceof ServerThread) {
            return EnvType.SERVER;
        }
        if (currentThread instanceof ClientThread) {
            return EnvType.CLIENT;
        }
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
}
