package dev.the_fireplace.annotateddi.entrypoint;

import com.google.inject.AbstractModule;

import java.util.Collection;

public interface DIModuleEntrypoint {
    Collection<AbstractModule> getModules();
}
