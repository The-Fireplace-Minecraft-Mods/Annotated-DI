package dev.the_fireplace.annotateddi.impl.di;

import com.google.gson.*;
import dev.the_fireplace.annotateddi.impl.AnnotatedDIConstants;
import dev.the_fireplace.annotateddi.impl.domain.loader.LoaderHelper;

import javax.inject.Inject;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

@SuppressWarnings("rawtypes")
public final class ImplementationScanner
{
    public static final String DI_CONFIG_FILE_NAME = "annotated-di.json";

    private final LoaderHelper loaderHelper;

    @Inject
    public ImplementationScanner(LoaderHelper loaderHelper)
    {
        this.loaderHelper = loaderHelper;
    }

    public Optional<ImplementationContainer> readImplementationContainerFromPath(Path path) {
        ImplementationContainer implementationContainer = null;

        try (BufferedReader br = new BufferedReader(new InputStreamReader(Files.newInputStream(path), StandardCharsets.UTF_8))) {
            JsonElement jsonElement = new JsonParser().parse(br);
            if (jsonElement instanceof JsonObject jsonObject) {
                implementationContainer = readImplementationContainerJson(jsonObject);
                AnnotatedDIConstants.getLogger().debug("Found valid DI config at " + path);
            }
        } catch (IOException | JsonParseException | ClassNotFoundException e) {
            AnnotatedDIConstants.getLogger().error("Exception when reading implementation file!", e);
        }

        return Optional.ofNullable(implementationContainer);
    }

    private ImplementationContainer readImplementationContainerJson(JsonObject jsonObject) throws ClassNotFoundException {
        JsonArray modImplementations = jsonObject.getAsJsonArray("implementations");
        Map<Class, List<ImplementationData>> implementationDatas = new HashMap<>();
        for (JsonElement element : modImplementations) {
            JsonObject implementationObj = (JsonObject) element;
            if (isOnWrongEnvironment(implementationObj) || isMissingDependencies(implementationObj)) {
                continue;
            }

            JsonArray interfaceNames = implementationObj.getAsJsonArray("interfaces");
            List<Class<?>> interfaces = new ArrayList<>();
            for (JsonElement interfaceName : interfaceNames) {
                interfaces.add(stringToClass(interfaceName.getAsString()));
            }
            Class<?> implementationClass = stringToClass(implementationObj.get("class").getAsString());
            ImplementationData implementationData = new ImplementationData<>(
                implementationClass,
                interfaces,
                implementationObj.has("namedImplementation")
                    ? implementationObj.get("namedImplementation").getAsString()
                    : "",
                implementationObj.has("useAllInterfaces") && implementationObj.get("useAllInterfaces").getAsBoolean(),
                implementationObj.has("environment") ? implementationObj.get("environment").getAsString() : null
            );
            for (Class interfaceClass : interfaces) {
                implementationDatas.computeIfAbsent(interfaceClass, c -> new ArrayList<>()).add(implementationData);
            }
        }

        return new ImplementationContainer(jsonObject.get("version").getAsString(), implementationDatas);
    }

    private boolean isOnWrongEnvironment(JsonObject implementationObj) {
        return implementationObj.has("environment")
            && !loaderHelper.isOnEnvironment(implementationObj.get("environment").getAsString());
    }

    private boolean isMissingDependencies(JsonObject implementationObj) {
        if (!implementationObj.has("dependencyModIds")) {
            return false;
        }
        JsonArray dependencyModIds = implementationObj.getAsJsonArray("dependencyModIds");
        if (dependencyModIds.size() == 0) {
            return false;
        }
        for (JsonElement dependencyModId : dependencyModIds) {
            if (!loaderHelper.isModLoaded(dependencyModId.getAsString())) {
                return true;
            }
        }

        return false;
    }

    private Class stringToClass(String className) throws ClassNotFoundException {
        return Class.forName(className);
    }
}
