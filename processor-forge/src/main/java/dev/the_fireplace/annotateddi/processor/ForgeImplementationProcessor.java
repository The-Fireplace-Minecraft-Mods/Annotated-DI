package dev.the_fireplace.annotateddi.processor;

import com.google.auto.service.AutoService;

import javax.annotation.processing.Processor;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;

@SupportedAnnotationTypes("dev.the_fireplace.annotateddi.api.di.Implementation")
@SupportedSourceVersion(SourceVersion.RELEASE_16)
@AutoService(Processor.class)
public final class ForgeImplementationProcessor extends ImplementationProcessor
{
    @Override
    protected String getEnvironmentFromExternalAnnotations(Element implementationElement) {
        return "";
    }
}
