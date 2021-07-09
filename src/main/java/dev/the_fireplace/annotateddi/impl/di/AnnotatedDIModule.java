package dev.the_fireplace.annotateddi.impl.di;

import com.google.inject.AbstractModule;
import com.google.inject.name.Names;
import dev.the_fireplace.annotateddi.api.di.Implementation;
import dev.the_fireplace.annotateddi.impl.reflections.MagicAtUrlType;
import net.fabricmc.loader.api.FabricLoader;
import org.reflections.Reflections;
import org.reflections.util.ConfigurationBuilder;
import org.reflections.vfs.Vfs;

import javax.annotation.Nullable;
import java.io.*;
import java.util.Arrays;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

@SuppressWarnings({"rawtypes", "unchecked"})
public final class AnnotatedDIModule extends AbstractModule {
    @Override
    protected void configure() {
        ImplementationCache implementationCache = ImplementationCache.load();
        Set<Class<?>> implementations;
        String modList = FabricLoader.getInstance().getAllMods().stream().map(
            modContainer -> modContainer.getMetadata().getId() + "@" + modContainer.getMetadata().getVersion()
        ).sorted().collect(Collectors.joining(";"));
        if (implementationCache != null && modList.equals(implementationCache.modList)) {
            bindImplementations(Arrays.asList(implementationCache.implementations));
            return;
        }
        implementations = getImplementationsUsingReflections();
        bindImplementations(implementations);
        implementationCache = new ImplementationCache();
        implementationCache.modList = modList;
        implementationCache.implementations = implementations.toArray(new Class[0]);
        implementationCache.save();
    }

    private void bindImplementations(Iterable<Class<?>> implementations) {
        for (Class implementation : implementations) {
            bindImplementationToInterface(implementation);
        }
    }

    private Set<Class<?>> getImplementationsUsingReflections() {
        Vfs.UrlType magicATUrl = new MagicAtUrlType();
        Vfs.addDefaultURLTypes(magicATUrl);
        ConfigurationBuilder reflectionConfig = getReflectionConfig();
        Reflections reflections = new Reflections(reflectionConfig);

        return reflections.getTypesAnnotatedWith(Implementation.class);
    }

    private void bindImplementationToInterface(Class implementation) {
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
                throw new ImplementationException(String.format("Multiple interfaces found for @Implementation annotated class %s, please set the value(s) to pick the correct one(s).", implementation.getCanonicalName()));
            } else {
                throw new ImplementationException(String.format("No interfaces found for @Implementation annotated class %s, please set the value(s) to pick the correct one(s).", implementation.getCanonicalName()));
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

    private ConfigurationBuilder getReflectionConfig() {
        return ConfigurationBuilder.build(
            Executors.newFixedThreadPool(128)
        );
    }

    private static final class ImplementationCache implements Serializable {
        @Serial
        private static final long serialVersionUID = 0x110;
        private transient static final String CACHE_FILE = FabricLoader.getInstance().getGameDir().resolve("di_implementations.ser").toString();
        private String modList;
        private Class<?>[] implementations;

        private ImplementationCache() {}

        @Nullable
        private static ImplementationCache load() {
            try (ObjectInputStream in = new ObjectInputStream(new FileInputStream(CACHE_FILE))) {
                return (ImplementationCache) in.readObject();
            } catch (Exception e) {
                return null;
            }
        }

        private void save() {
            try (ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(CACHE_FILE))) {
                out.writeObject(this);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
