package dev.the_fireplace.annotateddi;

import com.google.inject.Guice;
import com.google.inject.Injector;
import dev.the_fireplace.annotateddi.di.AnnotatedDIModule;
import net.fabricmc.api.ModInitializer;

public final class AnnotatedDI implements ModInitializer {
    public static final String MODID = "annotateddi";

    private static Injector injector = null;
    public static Injector getInjector() {
        if (injector == null) {
            injector = Guice.createInjector(new AnnotatedDIModule());
        }

        return injector;
    }

    @Override
    public void onInitialize() {
        getInjector();
    }
}
