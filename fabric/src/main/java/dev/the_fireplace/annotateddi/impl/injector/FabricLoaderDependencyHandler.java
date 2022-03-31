package dev.the_fireplace.annotateddi.impl.injector;

import dev.the_fireplace.annotateddi.api.di.Implementation;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.ModContainer;
import net.fabricmc.loader.api.metadata.ModDependency;

import java.util.Collection;
import java.util.Collections;
import java.util.Optional;
import java.util.stream.Collectors;

@Implementation
public final class FabricLoaderDependencyHandler implements LoaderDependencyHandler
{
    @Override
    public Collection<String> getDependencies(String modId) {
        Optional<ModContainer> modContainer = FabricLoader.getInstance().getModContainer(modId);
        if (modContainer.isEmpty()) {
            return Collections.emptySet();
        }
        return modContainer.get().getMetadata().getDependencies().stream()
            .map(ModDependency::getModId)
            .filter(id -> !isLoaderOrMinecraft(id))
            .collect(Collectors.toSet());
    }

    private static boolean isLoaderOrMinecraft(String modid) {
        return modid.equals("fabric") || modid.equals("fabricloader") || modid.equals("minecraft");
    }
}
