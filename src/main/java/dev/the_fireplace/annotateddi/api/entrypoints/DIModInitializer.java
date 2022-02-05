package dev.the_fireplace.annotateddi.api.entrypoints;

import com.google.inject.Injector;

public interface DIModInitializer
{
    void onInitialize(Injector diContainer);
}
