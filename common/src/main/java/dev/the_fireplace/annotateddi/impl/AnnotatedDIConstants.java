package dev.the_fireplace.annotateddi.impl;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public final class AnnotatedDIConstants
{
    public static final String MODID = "annotateddi";
    private static final Logger LOGGER = LogManager.getLogger(MODID);

    public static Logger getLogger() {
        return LOGGER;
    }
}
