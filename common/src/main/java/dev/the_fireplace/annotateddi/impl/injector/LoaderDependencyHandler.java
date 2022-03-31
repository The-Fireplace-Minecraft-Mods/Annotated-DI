package dev.the_fireplace.annotateddi.impl.injector;

import java.util.Collection;

public interface LoaderDependencyHandler
{
    /**
     * Get a list of modids which might use dependency injection (e.g. exclude Minecraft, Fabric/Forge, etc)
     */
    Collection<String> getDependencies(String modId);
}
