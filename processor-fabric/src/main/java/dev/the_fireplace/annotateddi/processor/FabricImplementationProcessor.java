package dev.the_fireplace.annotateddi.processor;

import com.google.auto.service.AutoService;
import net.fabricmc.api.Environment;

import javax.annotation.processing.Processor;
import javax.lang.model.element.Element;

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
