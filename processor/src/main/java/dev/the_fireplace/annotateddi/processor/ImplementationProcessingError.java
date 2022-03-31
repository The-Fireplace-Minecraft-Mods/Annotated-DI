package dev.the_fireplace.annotateddi.processor;

public class ImplementationProcessingError extends Error
{
    @Override
    public String getMessage() {
        return "Errors occurred when processing @Implementation annotated classes. See logs for more details.";
    }
}
