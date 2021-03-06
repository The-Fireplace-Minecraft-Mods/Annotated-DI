package dev.the_fireplace.annotateddi.impl.di;

import com.google.inject.AbstractModule;
import dev.the_fireplace.annotateddi.api.CustomModuleRegistry;
import dev.the_fireplace.annotateddi.api.Injectors;
import dev.the_fireplace.annotateddi.impl.AnnotatedDIConstants;
import dev.the_fireplace.annotateddi.impl.domain.loader.ModInjectableData;
import dev.the_fireplace.annotateddi.impl.injector.InjectorFactoryImpl;
import net.minecraftforge.fml.ModLoadingContext;

import java.util.Collection;

public final class CustomModuleRegistryImpl implements CustomModuleRegistry
{
    @Override
    public void register(Collection<AbstractModule> customModules) {
        String modId = ModLoadingContext.get().getActiveContainer().getModId();
        Injectors.INSTANCE.getAutoInjector(AnnotatedDIConstants.MODID).getInstance(ModInjectableData.class).addModules(modId, customModules);
    }
}
