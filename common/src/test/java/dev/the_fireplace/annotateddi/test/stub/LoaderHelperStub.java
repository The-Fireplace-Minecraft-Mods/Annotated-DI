package dev.the_fireplace.annotateddi.test.stub;

import dev.the_fireplace.annotateddi.impl.domain.loader.LoaderHelper;

import java.nio.file.Path;
import java.util.*;

public final class LoaderHelperStub implements LoaderHelper
{
    private final Map<String, Collection<String>> modDependencies = new HashMap<>();

    @Override
    public Collection<String> getLoadedMods() {
        return modDependencies.keySet();
    }

    @Override
    public boolean isModLoaded(String modId) {
        return modDependencies.containsKey(modId);
    }

    @Override
    public Collection<String> getDependencies(String modId) {
        return modDependencies.getOrDefault(modId, Collections.emptySet());
    }

    @Override
    public boolean isDevelopmentEnvironment() {
        return true;
    }

    @Override
    public boolean isOnEnvironment(String environment) {
        return true;
    }

    @Override
    public Optional<Path> findDiConfigPath(String modId) {
        return Optional.empty();
    }

    public void addMod(String modId, Collection<String> dependencies) {
        modDependencies.put(modId, dependencies);
    }
}
