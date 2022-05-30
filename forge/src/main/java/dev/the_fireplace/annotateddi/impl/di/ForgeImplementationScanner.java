package dev.the_fireplace.annotateddi.impl.di;

import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.ModContainer;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.loading.FMLEnvironment;
import net.minecraftforge.fml.loading.moddiscovery.ModFileInfo;
import net.minecraftforge.fml.packs.ResourcePackLoader;
import net.minecraftforge.forgespi.language.IModFileInfo;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Locale;
import java.util.Optional;

public final class ForgeImplementationScanner extends ImplementationScanner
{
    @Override
    public Optional<Path> findModImplementationPath(String modId) {
        Optional<? extends ModContainer> modContainer = ModList.get().getModContainerById(modId);
        if (!modContainer.isPresent()) {
            return Optional.empty();
        }
        IModFileInfo modFileInfo = modContainer.get().getModInfo().getOwningFile();
        if (modFileInfo instanceof ModFileInfo) {
            Path resource = ((ModFileInfo)modFileInfo).getFile().findResource(DI_CONFIG_FILE_NAME);
            if (Files.exists(resource)) {
                return Optional.of(resource);
            }
        }
        return Optional.empty();
    }

    @Override
    protected boolean isOnEnvironment(String environment) {
        switch (environment.toLowerCase(Locale.ROOT)) {
            case "client":
                return FMLEnvironment.dist.isClient();
            case "server":
                return FMLEnvironment.dist.isDedicatedServer();
            default:
                throw new IllegalStateException("Unknown environment: " + environment);
        }
    }

    @Override
    protected boolean isModLoaded(String modId) {
        return ModList.get().isLoaded(modId);
    }
}
