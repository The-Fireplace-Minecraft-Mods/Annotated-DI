package dev.the_fireplace.annotateddi.impl.loader;

import dev.the_fireplace.annotateddi.api.di.Implementation;
import dev.the_fireplace.annotateddi.impl.domain.loader.LoaderHelper;
import net.minecraftforge.fml.ModContainer;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.loading.FMLEnvironment;
import net.minecraftforge.forgespi.language.IModInfo;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collection;
import java.util.Collections;
import java.util.Locale;
import java.util.Optional;
import java.util.stream.Collectors;

import static dev.the_fireplace.annotateddi.impl.di.ImplementationScanner.DI_CONFIG_FILE_NAME;

@Implementation
public final class ForgeLoaderHelper implements LoaderHelper
{
    @Override
    public Collection<String> getLoadedMods() {
        return ModList.get().getMods().stream().map(IModInfo::getModId).collect(Collectors.toSet());
    }

    @Override
    public boolean isModLoaded(String modId) {
        return ModList.get().isLoaded(modId);
    }

    @Override
    public Collection<String> getDependencies(String modId) {
        Optional<? extends ModContainer> modContainer = ModList.get().getModContainerById(modId);
        if (modContainer.isEmpty()) {
            return Collections.emptySet();
        }
        return modContainer.get().getModInfo().getDependencies().stream()
            .map(IModInfo.ModVersion::getModId)
            .collect(Collectors.toSet());
    }

    @Override
    public boolean isDevelopmentEnvironment() {
        return !FMLEnvironment.production;
    }

    @Override
    public boolean isOnEnvironment(String environment) {
        return switch (environment.toLowerCase(Locale.ROOT)) {
            case "client" -> FMLEnvironment.dist.isClient();
            case "server" -> FMLEnvironment.dist.isDedicatedServer();
            default -> throw new IllegalStateException("Unknown environment: " + environment);
        };
    }

    @Override
    public Optional<Path> findDiConfigPath(String modId) {
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
}
