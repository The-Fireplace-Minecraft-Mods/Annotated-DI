package dev.the_fireplace.annotateddi.impl.injector;

import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Stage;
import dev.the_fireplace.annotateddi.impl.di.AnnotatedDIModule;
import dev.the_fireplace.annotateddi.impl.di.ImplementationContainer;
import dev.the_fireplace.annotateddi.impl.di.ImplementationScanner;

import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentSkipListSet;

public abstract class InjectorFactory
{
    private final Map<String, ImplementationContainer> modDefaultImplementationMap = new ConcurrentHashMap<>();
    private final Map<String, Collection<AbstractModule>> modCustomModules = new ConcurrentHashMap<>();
    private final Set<String> modsWithoutDefaultImplementations = new ConcurrentSkipListSet<>();

    public String[] resolveInjectableDependencies(String[] dependencies) {
        String[] unresolvedMods = Arrays.stream(dependencies)
            .filter(modId -> !modsWithoutDefaultImplementations.contains(modId))
            .filter(modId -> !modDefaultImplementationMap.containsKey(modId))
            .toArray(String[]::new);
        for (String unresolvedModId : unresolvedMods) {
            Optional<Path> configPath = getImplementationScanner().findModImplementationPath(unresolvedModId);
            if (configPath.isPresent()) {
                Optional<ImplementationContainer> container = getImplementationScanner().readImplementationContainerFromPath(configPath.get());
                if (container.isPresent()) {
                    modDefaultImplementationMap.put(unresolvedModId, container.get());
                } else {
                    modsWithoutDefaultImplementations.add(unresolvedModId);
                }
            } else {
                modsWithoutDefaultImplementations.add(unresolvedModId);
            }
        }

        return Arrays.stream(dependencies)
            .filter(modId -> modDefaultImplementationMap.containsKey(modId) || modCustomModules.containsKey(modId))
            .toArray(String[]::new);
    }

    public Injector create(String[] modIds) {
        modIds = resolveInjectableDependencies(modIds);
        Collection<AbstractModule> abstractModules = new HashSet<>();
        for (String modId : modIds) {
            if (modDefaultImplementationMap.containsKey(modId)) {
                abstractModules.add(new AnnotatedDIModule(modDefaultImplementationMap.get(modId)));
            }
            if (modCustomModules.containsKey(modId)) {
                abstractModules.addAll(modCustomModules.get(modId));
            }
        }
        return Guice.createInjector(isDevelopmentEnvironment() ? Stage.DEVELOPMENT : Stage.PRODUCTION, abstractModules);
    }

    public void addModules(String modId, Collection<AbstractModule> modules) {
        modCustomModules.computeIfAbsent(modId, m -> new HashSet<>()).addAll(modules);
    }

    protected abstract ImplementationScanner getImplementationScanner();

    protected abstract boolean isDevelopmentEnvironment();
}
