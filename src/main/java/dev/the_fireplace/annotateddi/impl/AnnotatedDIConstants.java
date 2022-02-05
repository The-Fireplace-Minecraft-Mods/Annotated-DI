package dev.the_fireplace.annotateddi.impl;

import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Stage;
import dev.the_fireplace.annotateddi.api.entrypoints.DIModuleCreator;
import dev.the_fireplace.annotateddi.impl.di.AnnotatedDIModule;
import net.fabricmc.loader.api.FabricLoader;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public final class AnnotatedDIConstants
{
    public static final String MODID = "annotateddi";
    private static final Logger LOGGER = LogManager.getLogger(MODID);
    private static Injector injector = null;

    public static Logger getLogger() {
        return LOGGER;
    }

    public static Injector getInjector() {
        if (injector == null) {
            ModuleContainer moduleContainer = new ModuleContainer();
            FabricLoader.getInstance().getEntrypointContainers("di-module", DIModuleCreator.class).forEach((entrypoint) -> {
                moduleContainer.modules = ArrayUtils.addAll(moduleContainer.modules, entrypoint.getEntrypoint().getModules().toArray(new AbstractModule[0]));
            });
            injector = Guice.createInjector(FabricLoader.getInstance().isDevelopmentEnvironment() ? Stage.DEVELOPMENT : Stage.PRODUCTION, moduleContainer.modules);
        }

        return injector;
    }

    private static class ModuleContainer
    {
        private AbstractModule[] modules = new AbstractModule[]{new AnnotatedDIModule()};
    }
}
