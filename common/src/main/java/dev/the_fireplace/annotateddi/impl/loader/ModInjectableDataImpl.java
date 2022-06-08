package dev.the_fireplace.annotateddi.impl.loader;

import com.google.inject.AbstractModule;
import dev.the_fireplace.annotateddi.api.di.Implementation;
import dev.the_fireplace.annotateddi.impl.di.AnnotatedDIConfigModule;
import dev.the_fireplace.annotateddi.impl.di.ImplementationContainer;
import dev.the_fireplace.annotateddi.impl.di.ImplementationScanner;
import dev.the_fireplace.annotateddi.impl.domain.loader.LoaderHelper;
import dev.the_fireplace.annotateddi.impl.domain.loader.ModInjectableData;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentSkipListSet;

@Singleton
@Implementation
public final class ModInjectableDataImpl implements ModInjectableData
{
    private final Map<String, ImplementationContainer> modDefaultImplementationMap = new ConcurrentHashMap<>();
    private final Map<String, Collection<AbstractModule>> modCustomModules = new ConcurrentHashMap<>();
    private final Set<String> modsWithoutDefaultImplementations = new ConcurrentSkipListSet<>();

    private final LoaderHelper loaderHelper;
    private final ImplementationScanner implementationScanner;

    @Inject
    public ModInjectableDataImpl(LoaderHelper loaderHelper, ImplementationScanner implementationScanner) {
        this.loaderHelper = loaderHelper;
        this.implementationScanner = implementationScanner;
    }

    @Override
    public boolean isInjectable(Collection<String> node) {
        for (String modId : node) {
            resolveInjectableDataIfNeeded(modId);
            if (modDefaultImplementationMap.containsKey(modId) || modCustomModules.containsKey(modId)) {
                return true;
            }
        }
        return false;
    }

    private void resolveInjectableDataIfNeeded(String modId) {
        if (isUnresolved(modId)) {
            resolveInjectableData(modId);
        }
    }

    private boolean isUnresolved(String modId) {
        return !modsWithoutDefaultImplementations.contains(modId) && !modDefaultImplementationMap.containsKey(modId);
    }

    private void resolveInjectableData(String modId) {
        Optional<Path> configPath = loaderHelper.findDiConfigPath(modId);
        if (configPath.isPresent()) {
            Optional<ImplementationContainer> container = implementationScanner.readImplementationContainerFromPath(configPath.get());
            if (container.isPresent()) {
                modDefaultImplementationMap.put(modId, container.get());
            } else {
                modsWithoutDefaultImplementations.add(modId);
            }
        } else {
            modsWithoutDefaultImplementations.add(modId);
        }
    }

    @Override
    public Collection<AbstractModule> getModules(Collection<String> dependencyTreeNode) {
        Collection<AbstractModule> abstractModules = new HashSet<>();
        for (String modId : dependencyTreeNode) {
            if (modDefaultImplementationMap.containsKey(modId)) {
                abstractModules.add(new AnnotatedDIConfigModule(modDefaultImplementationMap.get(modId)));
            }
            if (modCustomModules.containsKey(modId)) {
                abstractModules.addAll(modCustomModules.get(modId));
            }
        }

        return abstractModules;
    }

    @Override
    public void addModules(String modId, Collection<AbstractModule> modules) {
        //TODO fail if injector has already been created - modules are being added too late to take effect
        modCustomModules.computeIfAbsent(modId, m -> new HashSet<>()).addAll(modules);
    }
}
