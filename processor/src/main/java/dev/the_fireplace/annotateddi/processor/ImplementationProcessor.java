package dev.the_fireplace.annotateddi.processor;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.google.gson.stream.JsonWriter;
import dev.the_fireplace.annotateddi.api.di.Implementation;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeMirror;
import javax.tools.Diagnostic;
import javax.tools.FileObject;
import javax.tools.StandardLocation;
import java.io.BufferedWriter;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

public abstract class ImplementationProcessor extends AbstractProcessor
{
    public static final String DI_CONFIG_FILE_NAME = "annotated-di.json";
    private static final String VERSION = "${version}";
    private final Gson gson = new Gson();

    @Override
    public Set<String> getSupportedAnnotationTypes() {
        return Set.of(Implementation.class.getCanonicalName());
    }

    @Override
    public SourceVersion getSupportedSourceVersion() {
        return SourceVersion.RELEASE_8;
    }

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        for (TypeElement annotation : annotations) {
            Set<? extends Element> annotatedElements = roundEnv.getElementsAnnotatedWith(annotation);
            List<Element> implementations = getValidAnnotatedElements(annotatedElements);
            processingEnv.getMessager().printMessage(Diagnostic.Kind.NOTE, "Annotated DI processing complete, valid @Implementation count: " + implementations.size());

            writeJsonToFile(convertImplementationsToJson(implementations));
        }

        return false;
    }

    private List<Element> getValidAnnotatedElements(Set<? extends Element> annotatedElements) {
        Map<Boolean, List<Element>> annotatedClasses = annotatedElements.stream().collect(
            Collectors.partitioningBy(this::isValidAnnotatedElement)
        );
        List<Element> validAnnotatedClasses = annotatedClasses.get(true);
        List<Element> invalidAnnotatedClasses = annotatedClasses.get(false);
        invalidAnnotatedClasses.forEach(this::logImplementationError);
        if (!invalidAnnotatedClasses.isEmpty()) {
            throw new ImplementationProcessingError();
        }

        return validAnnotatedClasses;
    }

    private JsonObject convertImplementationsToJson(List<Element> implementations) {
        JsonArray outputImplementationsJson = new JsonArray();

        for (Element implementationElement : implementations) {
            Implementation implAnnotation = implementationElement.getAnnotation(Implementation.class);
            List<String> interfaceNames = getInterfaceNames((TypeElement) implementationElement, implAnnotation);
            JsonObject implementationJson = createImplementationJsonObject(
                implementationElement,
                interfaceNames,
                implAnnotation.name(),
                implAnnotation.allInterfaces(),
                implAnnotation.environment(),
                implAnnotation.dependencyModIds()
            );

            outputImplementationsJson.add(implementationJson);
        }

        JsonObject outputJson = new JsonObject();
        outputJson.addProperty("version", VERSION);
        outputJson.add("implementations", outputImplementationsJson);

        return outputJson;
    }

    private void writeJsonToFile(JsonObject outputJson) {
        try {
            FileObject builderFile = processingEnv.getFiler().createResource(StandardLocation.CLASS_OUTPUT, "", DI_CONFIG_FILE_NAME);
            try (JsonWriter writer = new JsonWriter(new BufferedWriter(builderFile.openWriter()))) {
                gson.toJson(outputJson, writer);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private List<String> getInterfaceNames(TypeElement implementationElement, Implementation implAnnotation) {
        List<String> interfaces = new ArrayList<>();
        if (usesImplicitInterface(implAnnotation)) {
            if (implAnnotation.allInterfaces()) {
                interfaces.addAll(getImplicitInterfaces(implementationElement));
            } else {
                interfaces.add(getImplicitInterface(implementationElement));
            }
        } else {
            interfaces.addAll(getExplicitInterfaces(implAnnotation));
        }
        return interfaces;
    }

    private boolean isValidAnnotatedElement(Element element) {
        if (element.getKind() != ElementKind.CLASS) {
            return false;
        }
        Implementation implAnnotation = element.getAnnotation(Implementation.class);
        if (implAnnotation == null) {
            return false;
        }

        if (usesImplicitInterface(implAnnotation)) {
            int interfaceCount = ((TypeElement) element).getInterfaces().size();
            if (!implAnnotation.allInterfaces()) {
                return interfaceCount == 1;
            } else {
                return interfaceCount >= 1;
            }
        }

        return true;
    }

    private void logImplementationError(Element element) {
        processingEnv.getMessager().printMessage(
            Diagnostic.Kind.ERROR,
            "@Implementation must be applied to a class which implements a single Interface, or provide the name(s) of the Interface(s) to implement.",
            element
        );
    }

    private JsonObject createImplementationJsonObject(
        Element implementationElement,
        List<String> interfaceNames,
        String namedImplementationName,
        boolean useAllInterfaces,
        String environment,
        String[] dependencyModIds
    ) {
        JsonObject output = new JsonObject();

        output.addProperty("class", implementationElement.asType().toString());
        if (!namedImplementationName.isEmpty()) {
            output.addProperty("namedImplementation", namedImplementationName);
        }
        JsonArray interfacesJsonArray = new JsonArray();
        for (String interfaceName : interfaceNames) {
            interfacesJsonArray.add(new JsonPrimitive(interfaceName));
        }
        output.add("interfaces", interfacesJsonArray);
        output.addProperty("useAllInterfaces", useAllInterfaces);

        if (environment.isEmpty()) {
            environment = getEnvironmentFromExternalAnnotations(implementationElement);
        }

        if (!environment.isEmpty()) {
            output.addProperty("environment", environment);
        }

        JsonArray dependenciesJsonArray = new JsonArray();
        for (String dependencyModId : dependencyModIds) {
            dependenciesJsonArray.add(new JsonPrimitive(dependencyModId));
        }
        if (dependenciesJsonArray.size() > 0) {
            output.add("dependencyModIds", dependenciesJsonArray);
        }

        return output;
    }

    private String getImplicitInterface(TypeElement implementationElement) {
        return implementationElement.getInterfaces().get(0).toString();
    }

    private List<String> getImplicitInterfaces(TypeElement implementationElement) {
        return implementationElement.getInterfaces().stream()
            .map(TypeMirror::toString)
            .collect(Collectors.toList());
    }

    private List<String> getExplicitInterfaces(Implementation implAnnotation) {
        return Arrays.stream(implAnnotation.value()).collect(Collectors.toList());
    }

    private boolean usesImplicitInterface(Implementation implAnnotation) {
        return Arrays.equals(implAnnotation.value(), new String[]{""});
    }

    /**
     * Use loader-specific annotations to determine the environment.
     *
     * @return CLIENT, SERVER, or empty string if not constrained to one environment.
     */
    protected abstract String getEnvironmentFromExternalAnnotations(Element implementationElement);
}
