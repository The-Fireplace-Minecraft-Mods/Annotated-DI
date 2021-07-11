package dev.the_fireplace.annotateddi.processor;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.stream.JsonWriter;
import dev.the_fireplace.annotateddi.api.di.Implementation;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.TypeElement;
import javax.tools.Diagnostic;
import javax.tools.JavaFileObject;
import java.io.BufferedWriter;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

@SupportedAnnotationTypes("dev.the_fireplace.annotateddi.api.di.Implementation")
@SupportedSourceVersion(SourceVersion.RELEASE_16)
public final class ImplementationProcessor extends AbstractProcessor {
    private final Gson gson = new Gson();

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        for (TypeElement annotation : annotations) {
            Set<? extends Element> annotatedElements = roundEnv.getElementsAnnotatedWith(annotation);
            List<Element> implementations = getValidAnnotatedElements(annotatedElements);
            if (implementations.isEmpty()) {
                continue;
            }

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

        return validAnnotatedClasses;
    }

    private JsonArray convertImplementationsToJson(List<Element> implementations) {
        JsonArray outputJson = new JsonArray();

        for (Element implementationElement : implementations) {
            Implementation implAnnotation = implementationElement.getAnnotation(Implementation.class);
            List<String> interfaceNames = getInterfaceNames((TypeElement) implementationElement, implAnnotation);
            JsonObject implementationJson = createImplementationJsonObject(implementationElement, interfaceNames, implAnnotation.name());

            outputJson.add(implementationJson);
        }
        return outputJson;
    }

    private void writeJsonToFile(JsonArray outputJson) {
        try {
            JavaFileObject builderFile = processingEnv.getFiler().createSourceFile("annotated-di.json");
            try (JsonWriter writer = gson.newJsonWriter(new BufferedWriter(builderFile.openWriter()))) {
                gson.toJson(outputJson, writer);
            } catch (IOException e) {
                e.printStackTrace();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private List<String> getInterfaceNames(TypeElement implementationElement, Implementation implAnnotation) {
        List<String> interfaces = new ArrayList<>();
        if (usesImplicitInterface(implAnnotation)) {
            interfaces.add(getImplicitInterface(implementationElement));
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
            return ((TypeElement) element).getInterfaces().size() == 1;
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

    private JsonObject createImplementationJsonObject(Element implementationElement, List<String> interfaceNames, String namedImplementationName) {
        JsonObject output = new JsonObject();

        output.addProperty("class", implementationElement.asType().toString());
        if (!namedImplementationName.isBlank()) {
            output.addProperty("namedImplementation", namedImplementationName);
        }
        JsonArray interfacesJsonArray = new JsonArray();
        for (String interfaceName: interfaceNames) {
            interfacesJsonArray.add(interfaceName);
        }
        output.add("interfaces", interfacesJsonArray);

        return output;
    }

    private String getImplicitInterface(TypeElement implementationElement) {
        return implementationElement.getInterfaces().get(0).toString();
    }

    private List<String> getExplicitInterfaces(Implementation implAnnotation) {
        return Arrays.stream(implAnnotation.value()).map(Class::getName).collect(Collectors.toList());
    }

    private boolean usesImplicitInterface(Implementation implAnnotation) {
        return Arrays.equals(implAnnotation.value(), new Class[]{Object.class});
    }
}
