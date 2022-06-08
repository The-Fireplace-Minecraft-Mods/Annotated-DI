package dev.the_fireplace.annotateddi.impl.domain.loader;

import com.google.inject.AbstractModule;

import java.util.Collection;

public interface ModInjectableData
{
    Collection<AbstractModule> getModules(Collection<String> dependencyTreeNode);

    void addModules(String modId, Collection<AbstractModule> modules);

    boolean isInjectable(Collection<String> node);
}
