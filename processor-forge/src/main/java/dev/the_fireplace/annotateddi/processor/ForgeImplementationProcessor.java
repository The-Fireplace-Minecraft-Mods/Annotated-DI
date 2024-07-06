package dev.the_fireplace.annotateddi.processor;

import com.google.auto.service.AutoService;

import javax.annotation.processing.Processor;
import javax.lang.model.element.Element;

@AutoService(Processor.class)
public final class ForgeImplementationProcessor extends ImplementationProcessor
{
    @Override
    protected String getEnvironmentFromExternalAnnotations(Element implementationElement) {
        return "";
    }
}
