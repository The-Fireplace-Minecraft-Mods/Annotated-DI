package dev.the_fireplace.annotateddi.impl.loader;

import dev.the_fireplace.annotateddi.api.di.Implementation;
import dev.the_fireplace.annotateddi.impl.domain.loader.LoaderHelper;
import net.fabricmc.api.EnvType;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.ModContainer;
import net.fabricmc.loader.api.metadata.ModDependency;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collection;
import java.util.Collections;
import java.util.Optional;
import java.util.stream.Collectors;

import static dev.the_fireplace.annotateddi.impl.di.ImplementationScanner.DI_CONFIG_FILE_NAME;

@Implementation
public final class FabricLoaderHelper implements LoaderHelper
{
    @Override
    public Collection<String> getLoadedMods() {
        return FabricLoader.getInstance().getAllMods().stream().map(mod -> mod.getMetadata().getId()).collect(Collectors.toSet());
    }

    @Override
    public boolean isModLoaded(String modId) {
        return FabricLoader.getInstance().isModLoaded(modId);
    }

    @Override
    public Collection<String> getDependencies(String modId) {
        Optional<ModContainer> modContainer = FabricLoader.getInstance().getModContainer(modId);
        if (!modContainer.isPresent()) {
            return Collections.emptySet();
        }
        return modContainer.get().getMetadata().getDependencies().stream()
            .map(ModDependency::getModId)
            .collect(Collectors.toSet());
    }

    @Override
    public boolean isDevelopmentEnvironment() {
        return FabricLoader.getInstance().isDevelopmentEnvironment();
    }

    @Override
    public boolean isOnEnvironment(String environment) {
        return FabricLoader.getInstance().getEnvironmentType().equals(EnvType.valueOf(environment));
    }

    @Override
    public Optional<Path> findDiConfigPath(String modId) {
        Optional<ModContainer> modContainer = FabricLoader.getInstance().getModContainer(modId);
        if (!modContainer.isPresent()) {
            return Optional.empty();
        }
        Optional<Path> path = modContainer.get().findPath(DI_CONFIG_FILE_NAME);
        if (path.isPresent() || !isDevelopmentEnvironment()) {
            return path;
        }
        Optional<Path> fabricModJsonPath = modContainer.get().findPath("fabric.mod.json");
        if (!fabricModJsonPath.isPresent()
            || fabricModJsonPath.get().getParent() == null
            || fabricModJsonPath.get().getParent().getParent() == null
            || fabricModJsonPath.get().getParent().getParent().getParent() == null
            || !Files.isDirectory(fabricModJsonPath.get().getParent())
            || !Files.isDirectory(fabricModJsonPath.get().getParent().getParent())
            || !Files.isDirectory(fabricModJsonPath.get().getParent().getParent().getParent())
        ) {
            return Optional.empty();
        }

        String sourceSetName = fabricModJsonPath.get().getParent().getFileName().toString();

        String[] classesSubfolders = {"java", "kotlin", "scala"};
        for (String classesSubfolder : classesSubfolders) {
            Path builtClassesFolder = fabricModJsonPath.get().getParent().getParent().getParent().resolve("classes").resolve(classesSubfolder).resolve(sourceSetName);
            if (Files.isDirectory(builtClassesFolder)) {
                Path configPath = builtClassesFolder.resolve(DI_CONFIG_FILE_NAME);
                if (Files.exists(configPath)) {
                    return Optional.of(configPath);
                }
            }
        }

        return Optional.empty();
    }
}
