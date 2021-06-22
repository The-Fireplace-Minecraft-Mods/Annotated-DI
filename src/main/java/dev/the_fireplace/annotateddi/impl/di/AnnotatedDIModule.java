package dev.the_fireplace.annotateddi.impl.di;

import com.google.inject.AbstractModule;
import com.google.inject.name.Names;
import dev.the_fireplace.annotateddi.api.di.Implementation;
import org.reflections.Reflections;

import java.util.Arrays;
import java.util.Set;

@SuppressWarnings({"rawtypes", "unchecked"})
public final class AnnotatedDIModule extends AbstractModule {
    @Override
    protected void configure() {
        Reflections reflections = new Reflections();
        Set<Class<?>> implementations = reflections.getTypesAnnotatedWith(Implementation.class);
        for (Class implementation: implementations) {
            Implementation annotation = (Implementation) implementation.getAnnotation(Implementation.class);
            Class<?>[] injectableInterfaces = annotation.value();
            String name = annotation.name();
            if (!Arrays.equals(injectableInterfaces, new Class[]{Object.class})) {
                for (Class injectableInterface : injectableInterfaces) {
                    bindWithOptionalName(injectableInterface, implementation, name);
                }
            } else {
                Class[] interfaces = implementation.getInterfaces();
                if (interfaces.length == 1) {
                    bindWithOptionalName(interfaces[0], implementation, name);
                } else if (interfaces.length > 1) {
                    throw new IllegalStateException(String.format("Multiple interfaces found for @Implementation annotated class %s, please set the value(s) to pick the correct one(s).", implementation.getCanonicalName()));
                } else {
                    throw new IllegalStateException(String.format("No interfaces found for @Implementation annotated class %s, please set the value(s) to pick the correct one(s).", implementation.getCanonicalName()));
                }
            }
        }
    }

    private void bindWithOptionalName(Class injectableInterface, Class implementation, String name) {
        if (name.isBlank()) {
            bind(injectableInterface).to(implementation);
        } else {
            bind(injectableInterface).annotatedWith(Names.named(name)).to(implementation);
        }
    }
}
