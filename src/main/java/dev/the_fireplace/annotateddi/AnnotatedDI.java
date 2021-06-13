package dev.the_fireplace.annotateddi;

import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import dev.the_fireplace.annotateddi.di.AnnotatedDIModule;
import dev.the_fireplace.annotateddi.entrypoint.DIModuleEntrypoint;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.loader.api.FabricLoader;
import org.apache.commons.lang3.ArrayUtils;

public final class AnnotatedDI implements ModInitializer {
    public static final String MODID = "annotateddi";

    private static Injector injector = null;
    public static Injector getInjector() {
        if (injector == null) {
            var moduleContainer = new Object() {
                AbstractModule[] modules = new AbstractModule[]{new AnnotatedDIModule()};
            };
            FabricLoader.getInstance().getEntrypointContainers("di", DIModuleEntrypoint.class).forEach((entrypoint) -> {
                moduleContainer.modules = ArrayUtils.addAll(moduleContainer.modules, entrypoint.getEntrypoint().getModules().toArray(new AbstractModule[0]));
            });
            injector = Guice.createInjector(moduleContainer.modules);
        }

        return injector;
    }

    @Override
    public void onInitialize() {
        getInjector();
    }
}
