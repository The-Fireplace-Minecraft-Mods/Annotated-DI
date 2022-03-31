package dev.the_fireplace.annotateddi.api;

import com.google.inject.Injector;
import dev.the_fireplace.annotateddi.impl.injector.InjectorManager;

public interface Injectors
{
    Injectors INSTANCE = new InjectorManager();

    /**
     * Gets an injector containing the given mod and its dependencies' injection configurations.
     */
    Injector getAutoInjector(String modid);

    /**
     * Gets an injector containing the given mods' injection configurations.
     */
    Injector getCustomInjector(String modid, String... otherModids);
}