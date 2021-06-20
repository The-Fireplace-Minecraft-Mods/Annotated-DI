package dev.the_fireplace.annotateddi.impl.di;

import com.google.inject.AbstractModule;
import dev.the_fireplace.annotateddi.api.di.Implementation;
import dev.the_fireplace.annotateddi.impl.AnnotatedDI;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.reflections.Reflections;

import java.util.Arrays;
import java.util.Set;

public final class AnnotatedDIModule extends AbstractModule {
    private static final Logger LOGGER = LogManager.getLogger(AnnotatedDI.MODID);

    @SuppressWarnings({"rawtypes", "unchecked"})
    @Override
    protected void configure() {
        Reflections reflections = new Reflections();
        Set<Class<?>> injectables = reflections.getTypesAnnotatedWith(Implementation.class);
        for (Class injectable: injectables) {
            Implementation annotation = (Implementation) injectable.getAnnotation(Implementation.class);
            Class<?>[] injectingForInterfaces = annotation.value();
            if (!Arrays.equals(injectingForInterfaces, new Class[]{Object.class})) {
                for (Class injectingFor : injectingForInterfaces) {
                    bind(injectingFor).to(injectable);
                }
            } else {
                Class[] interfaces = injectable.getInterfaces();
                if (interfaces.length == 1) {
                    bind(interfaces[0]).to(injectable);
                } else {
                    LOGGER.error("Multiple interfaces found for @Implementation annotated class {}, please set the value(s) to pick which ones.", injectable.getCanonicalName());
                }
            }
        }
    }
}
