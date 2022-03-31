package dev.the_fireplace.annotateddi.impl.di;

import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.ModContainer;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.loading.FMLEnvironment;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Locale;
import java.util.Optional;

public final class ForgeImplementationScanner extends ImplementationScanner
{
    @Override
    public Optional<Path> findModImplementationPath(String modId) {
        Optional<? extends ModContainer> modContainer = ModList.get().getModContainerById(modId);
        if (modContainer.isEmpty()) {
            return Optional.empty();
        }
        Path resource = modContainer.get().getModInfo().getOwningFile().getFile().findResource(DI_CONFIG_FILE_NAME);
        if (Files.exists(resource)) {
            return Optional.of(resource);
        }
        return Optional.empty();
    }

    @Override
    protected boolean isOnEnvironment(String environment) {
        return switch (environment.toLowerCase(Locale.ROOT)) {
            case "client" -> FMLEnvironment.dist.isClient();
            case "server" -> FMLEnvironment.dist.isDedicatedServer();
            default -> throw new IllegalStateException("Unknown environment: " + environment);
        };
    }

    @Override
    protected boolean isModLoaded(String modId) {
        return ModList.get().isLoaded(modId);
    }
}
