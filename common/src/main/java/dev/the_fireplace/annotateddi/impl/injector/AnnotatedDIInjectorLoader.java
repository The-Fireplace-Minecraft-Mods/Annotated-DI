package dev.the_fireplace.annotateddi.impl.injector;

import com.google.gson.*;
import com.google.inject.Guice;
import com.google.inject.Injector;
import dev.the_fireplace.annotateddi.impl.AnnotatedDIConstants;
import dev.the_fireplace.annotateddi.impl.di.AnnotatedDIConfigModule;
import dev.the_fireplace.annotateddi.impl.di.ImplementationContainer;
import dev.the_fireplace.annotateddi.impl.di.ImplementationData;
import dev.the_fireplace.annotateddi.impl.io.FileSystemUtil;
import dev.the_fireplace.annotateddi.impl.io.UrlUtil;

import javax.annotation.Nullable;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.zip.ZipError;

import static dev.the_fireplace.annotateddi.impl.di.ImplementationScanner.DI_CONFIG_FILE_NAME;

@SuppressWarnings({"rawtypes", "unchecked"})
public final class AnnotatedDIInjectorLoader
{
    public static Injector loadAnnotatedDIInjector() {
        return Guice.createInjector(new AnnotatedDIConfigModule(new AnnotatedDIInjectorLoader().findImplementation()));
    }

    private ImplementationContainer findImplementation() {
        try {
            Enumeration<URL> diConfigFileUrls = this.getClass().getClassLoader().getResources(DI_CONFIG_FILE_NAME);
            Set<Path> validConfigFilePaths = getConfigFilePaths(diConfigFileUrls);
            if (validConfigFilePaths.size() != 1) {
                StringBuilder formattedError = new StringBuilder(String.format("Only expected one implementation with Annotated DI's logo, found %s.", validConfigFilePaths.size()));
                for (Path path : validConfigFilePaths) {
                    formattedError.append("\r\n").append(path.toString());
                }
                throw new IllegalStateException(formattedError.toString());
            }

            return getImplementationContainerFromConfig(validConfigFilePaths.stream().findAny().get());
        } catch (IOException e) {
            AnnotatedDIConstants.getLogger().error("Exception when scanning for implementations!", e);
        }

        throw new IllegalStateException("No internal DI config found!");
    }

    private Set<Path> getConfigFilePaths(Enumeration<URL> mods) {
        Set<Path> modsList = new HashSet<>();

        while (mods.hasMoreElements()) {
            URL url;
            try {
                url = UrlUtil.getSource(DI_CONFIG_FILE_NAME, mods.nextElement());
            } catch (Exception e) {
                AnnotatedDIConstants.getLogger().error("Error getting DI config's source!", e);
                continue;
            }
            Path normalizedPath, configJsonPath;
            try {
                normalizedPath = UrlUtil.asPath(url).normalize();
            } catch (URISyntaxException e) {
                throw new RuntimeException("Failed to convert URL " + url + "!", e);
            }

            if (Files.isDirectory(normalizedPath)) {
                configJsonPath = normalizedPath.resolve(DI_CONFIG_FILE_NAME);
                if (!Files.exists(normalizedPath.resolve("AnnotatedDI.png"))) {
                    continue;
                }
            } else {
                // JAR file
                try {
                    FileSystemUtil.FileSystemDelegate jarFs = FileSystemUtil.getJarFileSystem(normalizedPath, false);
                    configJsonPath = jarFs.get().getPath(DI_CONFIG_FILE_NAME);
                    if (!Files.exists(jarFs.get().getPath("AnnotatedDI.png"))) {
                        continue;
                    }
                } catch (IOException e) {
                    AnnotatedDIConstants.getLogger().error("Failed to open JAR at " + normalizedPath + "!", e);
                    continue;
                } catch (ZipError e) {
                    AnnotatedDIConstants.getLogger().error("Jar at " + normalizedPath + " is corrupted!", e);
                    continue;
                }
            }
            modsList.add(configJsonPath);
        }

        return modsList;
    }

    private ImplementationContainer getImplementationContainerFromConfig(Path path) {
        ImplementationContainer implementationContainer = readImplementationContainerFromPath(path);

        if (implementationContainer == null) {
            throw new IllegalStateException("Unable to read Annotated DI injection config!");
        }

        return implementationContainer;
    }

    @Nullable
    private ImplementationContainer readImplementationContainerFromPath(Path path) {
        ImplementationContainer implementationContainer = null;

        JsonParser jsonParser = new JsonParser();
        try (BufferedReader br = new BufferedReader(new InputStreamReader(Files.newInputStream(path), StandardCharsets.UTF_8))) {
            JsonElement jsonElement = jsonParser.parse(br);
            if (jsonElement instanceof JsonObject) {
                JsonObject jsonObject = (JsonObject) jsonElement;
                implementationContainer = readImplementationContainerJson(jsonObject);
            }
        } catch (IOException | JsonParseException | ClassNotFoundException e) {
            AnnotatedDIConstants.getLogger().error("Exception when reading implementation file!", e);
        }

        return implementationContainer;
    }

    private ImplementationContainer readImplementationContainerJson(JsonObject jsonObject) throws ClassNotFoundException {
        JsonArray modImplementations = jsonObject.getAsJsonArray("implementations");
        Map<Class, List<ImplementationData>> implementationDatas = new HashMap<>();
        for (JsonElement element : modImplementations) {
            JsonObject implementationObj = (JsonObject) element;

            JsonArray interfaceNames = implementationObj.getAsJsonArray("interfaces");
            List<Class> interfaces = new ArrayList<>();
            for (JsonElement interfaceName : interfaceNames) {
                interfaces.add(stringToClass(interfaceName.getAsString()));
            }
            Class implementationClass = stringToClass(implementationObj.get("class").getAsString());
            ImplementationData implementationData = new ImplementationData(
                implementationClass,
                interfaces,
                implementationObj.has("namedImplementation")
                    ? implementationObj.get("namedImplementation").getAsString()
                    : "",
                implementationObj.has("useAllInterfaces") && implementationObj.get("useAllInterfaces").getAsBoolean(),
                null
            );
            for (Class interfaceClass : interfaces) {
                implementationDatas.computeIfAbsent(interfaceClass, c -> new ArrayList<>()).add(implementationData);
            }
        }

        return new ImplementationContainer(jsonObject.get("version").getAsString(), implementationDatas);
    }

    private Class stringToClass(String className) throws ClassNotFoundException {
        return Class.forName(className);
    }
}
