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
            Map<Boolean, List<Element>> annotatedClasses = annotatedElements.stream().collect(
                Collectors.partitioningBy(element -> {
                    //TODO Filter out invalid
                    return true;
                }));
            List<Element> implementations = annotatedClasses.get(true);
            List<Element> notImplementations = annotatedClasses.get(false);
            notImplementations.forEach(element ->
                processingEnv.getMessager().printMessage(
                    Diagnostic.Kind.ERROR,
                    "@Implementation must be applied to a class which implements a single Interface, or provide the name(s) of the Interface(s) to implement.",
                    element
                )
            );
            if (implementations.isEmpty()) {
                continue;
            }

            JsonArray outputObject = new JsonArray();

            for (Element implementationElement : implementations) {
                Implementation implAnnotation = implementationElement.getAnnotation(Implementation.class);

                List<String> interfaces = new ArrayList<>();
                if (Arrays.equals(implAnnotation.value(), new Class[]{Object.class})) {
                    interfaces.add(((TypeElement)implementationElement).getInterfaces().get(0).toString());
                } else {
                    interfaces.addAll(Arrays.stream(implAnnotation.value()).map(Class::getName).collect(Collectors.toList()));
                }

                String name = implAnnotation.name();
                JsonObject implementationData = new JsonObject();
                implementationData.addProperty("class", implementationElement.asType().toString());
                if (!name.isBlank()) {
                    implementationData.addProperty("namedImplementation", name);
                }
                JsonArray interfaceArray = new JsonArray();
                for (String interfaceStr: interfaces) {
                    interfaceArray.add(interfaceStr);
                }
                implementationData.add("interfaces", interfaceArray);

                outputObject.add(implementationData);
            }

            try {
                JavaFileObject builderFile = processingEnv.getFiler().createSourceFile("test.json");
                try (JsonWriter writer = gson.newJsonWriter(new BufferedWriter(builderFile.openWriter()))) {
                    gson.toJson(outputObject, writer);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        return false;
    }
}
