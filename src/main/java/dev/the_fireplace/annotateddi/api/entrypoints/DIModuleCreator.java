package dev.the_fireplace.annotateddi.api.entrypoints;

import com.google.inject.AbstractModule;

import java.util.Collection;

public interface DIModuleCreator {
    Collection<AbstractModule> getModules();
}
