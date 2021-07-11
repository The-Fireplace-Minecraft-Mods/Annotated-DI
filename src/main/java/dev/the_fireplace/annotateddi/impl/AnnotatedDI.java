package dev.the_fireplace.annotateddi.impl;

import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Stage;
import dev.the_fireplace.annotateddi.api.DIContainer;
import dev.the_fireplace.annotateddi.api.entrypoints.DIModInitializer;
import dev.the_fireplace.annotateddi.api.entrypoints.DIModuleCreator;
import dev.the_fireplace.annotateddi.impl.di.AnnotatedDIModule;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.loader.api.FabricLoader;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public final class AnnotatedDI implements ModInitializer {
    public static final String MODID = "annotateddi";
    private static final Logger LOGGER = LogManager.getLogger(MODID);
    public static Logger getLogger() {
        return LOGGER;
    }

    private static Injector injector = null;
    public static Injector getInjector() {
        if (injector == null) {
            var moduleContainer = new Object() {
                AbstractModule[] modules = new AbstractModule[]{new AnnotatedDIModule()};
            };
            FabricLoader.getInstance().getEntrypointContainers("di-module", DIModuleCreator.class).forEach((entrypoint) -> {
                moduleContainer.modules = ArrayUtils.addAll(moduleContainer.modules, entrypoint.getEntrypoint().getModules().toArray(new AbstractModule[0]));
            });
            injector = Guice.createInjector(FabricLoader.getInstance().isDevelopmentEnvironment() ? Stage.DEVELOPMENT : Stage.PRODUCTION, moduleContainer.modules);
        }

        return injector;
    }

    @Override
    public void onInitialize() {
        Injector container = DIContainer.get();
        FabricLoader.getInstance().getEntrypointContainers("di-main", DIModInitializer.class)
            .forEach((entrypoint) -> entrypoint.getEntrypoint().onInitialize(container));
    }
}
