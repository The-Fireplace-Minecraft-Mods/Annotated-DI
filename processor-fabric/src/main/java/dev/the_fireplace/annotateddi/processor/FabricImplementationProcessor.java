package dev.the_fireplace.annotateddi.processor;

import com.google.auto.service.AutoService;
import net.fabricmc.api.Environment;

import javax.annotation.processing.Processor;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;

@SupportedAnnotationTypes("dev.the_fireplace.annotateddi.api.di.Implementation")
@SupportedSourceVersion(SourceVersion.RELEASE_8)
@AutoService(Processor.class)
public final class FabricImplementationProcessor extends ImplementationProcessor
{
    @Override
    protected String getEnvironmentFromExternalAnnotations(Element implementationElement) {
        Environment annotation = implementationElement.getAnnotation(Environment.class);
        if (annotation != null) {
            return annotation.value().name();
        }

        return "";
    }
}
