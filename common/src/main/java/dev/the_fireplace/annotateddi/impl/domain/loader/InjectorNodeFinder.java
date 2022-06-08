package dev.the_fireplace.annotateddi.impl.domain.loader;

import javax.annotation.Nullable;
import java.util.Collection;

public interface InjectorNodeFinder
{
    @Nullable
    Collection<String> getParentNode(String modId);

    Collection<String> getNode(String modId);
}
