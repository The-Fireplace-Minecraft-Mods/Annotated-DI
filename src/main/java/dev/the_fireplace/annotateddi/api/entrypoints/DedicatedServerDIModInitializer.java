package dev.the_fireplace.annotateddi.api.entrypoints;

import com.google.inject.Injector;

public interface DedicatedServerDIModInitializer {
    void onInitializeServer(Injector diContainer);
}
