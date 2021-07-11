package dev.the_fireplace.annotateddi.impl.di;

import com.google.gson.*;
import com.google.inject.AbstractModule;
import com.google.inject.name.Names;
import dev.the_fireplace.annotateddi.impl.AnnotatedDI;
import net.fabricmc.loader.launch.common.FabricLauncherBase;
import net.fabricmc.loader.util.UrlConversionException;
import net.fabricmc.loader.util.UrlUtil;

import javax.annotation.Nullable;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.net.URL;
import java.util.*;

@SuppressWarnings({"rawtypes", "unchecked"})
public final class AnnotatedDIModule extends AbstractModule {
    public static final String DI_CONFIG_FILE_NAME = "annotated-di.json";

    @Override
    protected void configure() {
        Set<ImplementationContainer> implementations = findImplementations();
        bindImplementations(implementations);
    }

    private void bindImplementations(Iterable<ImplementationContainer> modImplementations) {
        for (ImplementationContainer modImplementationData : modImplementations) {
            for (ImplementationData implementationData : modImplementationData.implementations) {
                bindImplementationToInterface(
                    implementationData.implementation,
                    implementationData.interfaces.toArray(new Class[0]),
                    implementationData.name
                );
            }
        }
    }

    private Set<ImplementationContainer> findImplementations() {
        try {
            Enumeration<URL> diConfigFileUrls = FabricLauncherBase.getLauncher().getTargetClassLoader().getResources(DI_CONFIG_FILE_NAME);
            Set<URL> validConfigFileUrls = validateConfigFileUrls(diConfigFileUrls);

            return getImplementationContainersFromConfigs(validConfigFileUrls);
        } catch (IOException e) {
            AnnotatedDI.getLogger().error("Exception when scanning for implementations!", e);
        }

        return Set.of();
    }

    private Set<ImplementationContainer> getImplementationContainersFromConfigs(Set<URL> configFileUrls) {
        Set<ImplementationContainer> implementationContainers = new HashSet<>();

        for (URL url : configFileUrls) {
            ImplementationContainer implementationContainer = readImplementationContainerFromUrl(url);

            if (implementationContainer != null) {
                implementationContainers.add(implementationContainer);
            }
        }

        return implementationContainers;
    }

    private Set<URL> validateConfigFileUrls(Enumeration<URL> mods) {
        Set<URL> modsList = new HashSet<>();

        while (mods.hasMoreElements()) {
            try {
                modsList.add(UrlUtil.getSource(DI_CONFIG_FILE_NAME, mods.nextElement()));
            } catch (UrlConversionException e) {
                AnnotatedDI.getLogger().debug("Unable to read DI config!", e);
            }
        }

        return modsList;
    }

    @Nullable
    private ImplementationContainer readImplementationContainerFromUrl(URL url) {
        ImplementationContainer implementationContainer = null;
        File implementationDataFile;

        try {
            implementationDataFile = UrlUtil.asFile(url);
        } catch (UrlConversionException e) {
            return null;
        }

        JsonParser jsonParser = new JsonParser();
        try (BufferedReader br = new BufferedReader(new FileReader(implementationDataFile))) {
            JsonElement jsonElement = jsonParser.parse(br);
            if (jsonElement instanceof JsonObject jsonObject) {
                implementationContainer = readImplementationContainerJson(jsonObject);
            }
        } catch (IOException | JsonParseException | ClassNotFoundException e) {
            AnnotatedDI.getLogger().error("Exception when reading implementation file!", e);
        }

        return implementationContainer;
    }

    private ImplementationContainer readImplementationContainerJson(JsonObject jsonObject) throws ClassNotFoundException {
        JsonArray modImplementations = jsonObject.getAsJsonArray("implementations");
        List<ImplementationData> implementationDatas = new ArrayList<>();
        for (JsonElement element : modImplementations) {
            JsonObject implementationObj = (JsonObject) element;
            JsonArray interfaceNames = implementationObj.getAsJsonArray("interfaces");
            List<Class> interfaces = new ArrayList<>();
            for (JsonElement interfaceName : interfaceNames) {
                interfaces.add(stringToClass(interfaceName.getAsString()));
            }
            implementationDatas.add(new ImplementationData(
                stringToClass(implementationObj.get("class").getAsString()),
                interfaces,
                implementationObj.has("namedImplementation")
                    ? implementationObj.get("namedImplementation").getAsString()
                    : ""
            ));
        }

        return new ImplementationContainer(jsonObject.get("version").getAsString(), implementationDatas);
    }

    private void bindImplementationToInterface(Class implementation, Class[] injectableInterfaces, String name) {
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

    private Class stringToClass(String className) throws ClassNotFoundException {
        return Class.forName(className);
    }

    private record ImplementationContainer(String version, List<ImplementationData> implementations) {}
    private record ImplementationData(Class implementation, List<Class> interfaces, String name) {}
}
