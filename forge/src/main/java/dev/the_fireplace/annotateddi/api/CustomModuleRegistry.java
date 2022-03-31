package dev.the_fireplace.annotateddi.api;

import com.google.inject.AbstractModule;
import dev.the_fireplace.annotateddi.impl.di.CustomModuleRegistryImpl;

import java.util.Collection;

public interface CustomModuleRegistry
{
    CustomModuleRegistry INSTANCE = new CustomModuleRegistryImpl();

    void register(Collection<AbstractModule> customModules);
}
