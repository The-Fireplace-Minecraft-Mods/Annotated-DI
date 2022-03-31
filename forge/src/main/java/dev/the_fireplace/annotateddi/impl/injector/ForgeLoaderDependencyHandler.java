package dev.the_fireplace.annotateddi.impl.injector;

import dev.the_fireplace.annotateddi.api.di.Implementation;
import net.minecraftforge.fml.ModContainer;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.forgespi.language.IModInfo;

import java.util.Collection;
import java.util.Collections;
import java.util.Optional;
import java.util.stream.Collectors;

@Implementation
public final class ForgeLoaderDependencyHandler implements LoaderDependencyHandler
{
    @Override
    public Collection<String> getDependencies(String modId) {
        Optional<? extends ModContainer> modContainer = ModList.get().getModContainerById(modId);
        if (modContainer.isEmpty()) {
            return Collections.emptySet();
        }
        return modContainer.get().getModInfo().getDependencies().stream()
            .map(IModInfo.ModVersion::getModId)
            .filter(id -> !isLoaderOrMinecraft(id))
            .collect(Collectors.toSet());
    }

    private boolean isLoaderOrMinecraft(String modid) {
        return modid.equals("forge") || modid.equals("minecraft");
    }
}
