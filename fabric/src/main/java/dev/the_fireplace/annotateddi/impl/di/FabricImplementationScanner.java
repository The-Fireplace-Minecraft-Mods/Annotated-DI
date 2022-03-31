package dev.the_fireplace.annotateddi.impl.di;

import net.fabricmc.api.EnvType;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.ModContainer;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;

public final class FabricImplementationScanner extends ImplementationScanner
{
    @Override
    public Optional<Path> findModImplementationPath(String modId) {
        Optional<ModContainer> modContainer = FabricLoader.getInstance().getModContainer(modId);
        if (modContainer.isEmpty()) {
            return Optional.empty();
        }
        Optional<Path> path = modContainer.get().findPath(DI_CONFIG_FILE_NAME);
        if (path.isPresent() || !FabricLoader.getInstance().isDevelopmentEnvironment()) {
            return path;
        }
        Optional<Path> fabricModJsonPath = modContainer.get().findPath("fabric.mod.json");
        if (fabricModJsonPath.isEmpty()
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

    @Override
    protected boolean isOnEnvironment(String environment) {
        return FabricLoader.getInstance().getEnvironmentType().equals(EnvType.valueOf(environment));
    }

    @Override
    protected boolean isModLoaded(String modId) {
        return FabricLoader.getInstance().isModLoaded(modId);
    }
}
