package dev.the_fireplace.annotateddi.impl.injector;

import com.google.inject.Injector;
import dev.the_fireplace.annotateddi.api.Injectors;
import dev.the_fireplace.annotateddi.impl.AnnotatedDIConstants;
import org.apache.commons.lang3.ArrayUtils;

import java.util.Arrays;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public final class InjectorManager implements Injectors
{
    private final Map<String[], Injector> injectorCache = new ConcurrentHashMap<>();

    @Override
    public Injector getAutoInjector(String modId) {
        if (modId.equals(AnnotatedDIConstants.MODID)) {
            return getAnnotatedDIInternalInjector();
        }
        LoaderDependencyHandler loaderDependencyHandler = getAnnotatedDIInternalInjector().getInstance(LoaderDependencyHandler.class);
        String[] dependencies = loaderDependencyHandler.getDependencies(modId).toArray(new String[0]);
        return getCustomInjector(modId, dependencies);
    }

    @Override
    public Injector getCustomInjector(String modid, String... otherModids) {
        String[] key = createInjectorKey(modid, otherModids);
        if (injectorCache.containsKey(key)) {
            return injectorCache.get(key);
        }
        Injector injector = getAnnotatedDIInternalInjector().getInstance(InjectorFactory.class).create(key);
        injectorCache.put(key, injector);

        return injector;
    }

    // Custom internal injector so the other methods can dependency inject their loader-specific stuff instead of having to do some even worse hackery to get around Forge's multithreaded loading.
    private Injector getAnnotatedDIInternalInjector() {
        String[] annotatedDIKey = {AnnotatedDIConstants.MODID};
        if (injectorCache.containsKey(annotatedDIKey)) {
            return injectorCache.get(annotatedDIKey);
        }
        Injector injector = AnnotatedDIInjectorLoader.loadAnnotatedDIInjector();
        injectorCache.put(annotatedDIKey, injector);
        return injector;
    }

    private String[] createInjectorKey(String modid, String... otherModids) {
        String[] combinedArray = getAnnotatedDIInternalInjector().getInstance(InjectorFactory.class).resolveInjectableDependencies(ArrayUtils.add(otherModids, modid));
        Arrays.sort(combinedArray);
        return combinedArray;
    }
}
