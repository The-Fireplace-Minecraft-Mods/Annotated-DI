package dev.the_fireplace.annotateddi.impl.di;

import com.google.gson.*;
import com.google.inject.AbstractModule;
import com.google.inject.name.Names;
import dev.the_fireplace.annotateddi.impl.AnnotatedDI;
import net.fabricmc.loader.launch.common.FabricLauncherBase;
import net.fabricmc.loader.util.FileSystemUtil;
import net.fabricmc.loader.util.UrlConversionException;
import net.fabricmc.loader.util.UrlUtil;

import javax.annotation.Nullable;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.zip.ZipError;

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
            Set<Path> validConfigFilePaths = getConfigFilePaths(diConfigFileUrls);

            return getImplementationContainersFromConfigs(validConfigFilePaths);
        } catch (IOException e) {
            AnnotatedDI.getLogger().error("Exception when scanning for implementations!", e);
        }

        return Set.of();
    }

    private Set<Path> getConfigFilePaths(Enumeration<URL> mods) {
        Set<Path> modsList = new HashSet<>();

        while (mods.hasMoreElements()) {
            URL url;
            try {
                url = UrlUtil.getSource(DI_CONFIG_FILE_NAME, mods.nextElement());
            } catch (UrlConversionException e) {
                AnnotatedDI.getLogger().error("Error getting DI config's source!", e);
                continue;
            }
            Path normalizedPath, configJsonPath;
            try {
                normalizedPath = UrlUtil.asPath(url).normalize();
            } catch (UrlConversionException e) {
                throw new RuntimeException("Failed to convert URL " + url + "!", e);
            }

            if (Files.isDirectory(normalizedPath)) {
                configJsonPath = normalizedPath.resolve(DI_CONFIG_FILE_NAME);
            } else {
                // JAR file
                try {
                    FileSystemUtil.FileSystemDelegate jarFs = FileSystemUtil.getJarFileSystem(normalizedPath, false);
                    configJsonPath = jarFs.get().getPath(DI_CONFIG_FILE_NAME);
                } catch (IOException e) {
                    AnnotatedDI.getLogger().error("Failed to open JAR at " + normalizedPath + "!", e);
                    continue;
                } catch (ZipError e) {
                    AnnotatedDI.getLogger().error("Jar at " + normalizedPath + " is corrupted!", e);
                    continue;
                }
            }
            modsList.add(configJsonPath);
        }

        return modsList;
    }

    private Set<ImplementationContainer> getImplementationContainersFromConfigs(Set<Path> configFilePaths) {
        Set<ImplementationContainer> implementationContainers = new HashSet<>();

        for (Path path : configFilePaths) {
            ImplementationContainer implementationContainer = readImplementationContainerFromPath(path);

            if (implementationContainer != null) {
                implementationContainers.add(implementationContainer);
            }
        }

        return implementationContainers;
    }

    @Nullable
    private ImplementationContainer readImplementationContainerFromPath(Path path) {
        ImplementationContainer implementationContainer = null;

        JsonParser jsonParser = new JsonParser();
        try (BufferedReader br = new BufferedReader(new InputStreamReader(Files.newInputStream(path), StandardCharsets.UTF_8))) {
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
