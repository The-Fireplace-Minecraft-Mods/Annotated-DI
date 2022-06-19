package dev.the_fireplace.annotateddi.impl.di;

import dev.the_fireplace.annotateddi.api.Injectors;
import dev.the_fireplace.annotateddi.api.entrypoints.DIModuleCreator;
import dev.the_fireplace.annotateddi.impl.AnnotatedDIConstants;
import dev.the_fireplace.annotateddi.impl.domain.loader.ModInjectableData;
import net.fabricmc.loader.api.FabricLoader;

public final class FabricInjectorSetup
{
    private static boolean hasInitialized = false;

    public static synchronized void init() {
        if (hasInitialized) {
            return;
        }
        FabricLoader.getInstance().getEntrypointContainers("di-module", DIModuleCreator.class).forEach((entrypoint) -> {
            Injectors.INSTANCE
                .getAutoInjector(AnnotatedDIConstants.MODID)
                .getInstance(ModInjectableData.class)
                .addModules(entrypoint.getProvider().getMetadata().getId(), entrypoint.getEntrypoint().getModules());
        });
        hasInitialized = true;
    }
}
