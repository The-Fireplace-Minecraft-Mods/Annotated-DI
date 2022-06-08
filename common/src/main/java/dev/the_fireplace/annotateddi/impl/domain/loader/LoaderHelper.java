package dev.the_fireplace.annotateddi.impl.domain.loader;

import java.nio.file.Path;
import java.util.Collection;
import java.util.Optional;

public interface LoaderHelper
{
    Collection<String> getLoadedMods();
    boolean isModLoaded(String modId);

    Collection<String> getDependencies(String modId);

    boolean isDevelopmentEnvironment();

    boolean isOnEnvironment(String environment);

    Optional<Path> findDiConfigPath(String modId);
}
