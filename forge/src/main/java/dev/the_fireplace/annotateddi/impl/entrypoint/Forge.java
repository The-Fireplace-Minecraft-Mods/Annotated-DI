package dev.the_fireplace.annotateddi.impl.entrypoint;

import dev.the_fireplace.annotateddi.impl.injector.AnnotatedDIInjectorLoader;
import net.minecraftforge.fml.IExtensionPoint;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.loading.FMLEnvironment;

@Mod("annotateddi")
public final class Forge
{
    public Forge() {
        AnnotatedDIInjectorLoader.setDevelopmentEnvironment(!FMLEnvironment.production);
        // Register as optional on both sides
        ModLoadingContext.get().registerExtensionPoint(IExtensionPoint.DisplayTest.class, () -> new IExtensionPoint.DisplayTest(() -> IExtensionPoint.DisplayTest.IGNORESERVERONLY, (a, b) -> true));
    }
}
