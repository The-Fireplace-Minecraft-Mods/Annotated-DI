package dev.the_fireplace.annotateddi.impl.injector;

import com.google.inject.Injector;
import dev.the_fireplace.annotateddi.api.Injectors;
import dev.the_fireplace.annotateddi.impl.AnnotatedDIConstants;
import dev.the_fireplace.annotateddi.impl.domain.injector.InjectorFactory;
import dev.the_fireplace.annotateddi.impl.domain.loader.InjectorNodeFinder;

import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public final class InjectorManager implements Injectors
{
    private final Map<String, Injector> injectorCache = new HashMap<>();

    @Override
    public synchronized Injector getAutoInjector(String modId) {
        if (injectorCache.containsKey(modId)) {
            return injectorCache.get(modId);
        }
        if (modId.equals(AnnotatedDIConstants.MODID)) {
            createRootInjector();
            return getRootInjector();
        }

        return buildModInjector(modId);
    }

    public Injector buildModInjector(String modId) {
        Collection<String> node = getRootInjector().getInstance(InjectorNodeFinder.class).getNode(modId);
        Injector injector = getRootInjector().getInstance(InjectorFactory.class).create(node);
        injectorCache.put(modId, injector);

        return injector;
    }

    private void createRootInjector() {
        Injector injector = AnnotatedDIInjectorLoader.loadAnnotatedDIInjector();
        injectorCache.put(AnnotatedDIConstants.MODID, injector);
    }

    private Injector getRootInjector() {
        return getAutoInjector(AnnotatedDIConstants.MODID);
    }
}
