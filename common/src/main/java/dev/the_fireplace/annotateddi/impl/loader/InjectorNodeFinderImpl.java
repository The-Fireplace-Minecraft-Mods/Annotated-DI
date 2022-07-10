package dev.the_fireplace.annotateddi.impl.loader;

import com.google.inject.Singleton;
import dev.the_fireplace.annotateddi.api.di.Implementation;
import dev.the_fireplace.annotateddi.impl.domain.loader.InjectorNodeFinder;

import javax.annotation.Nullable;
import javax.inject.Inject;
import java.util.Collection;
import java.util.Map;
import java.util.Set;

@Singleton
@Implementation
public final class InjectorNodeFinderImpl implements InjectorNodeFinder
{
    private final InjectorTreeBuilder injectorTreeBuilder;

    @Inject
    public InjectorNodeFinderImpl(InjectorTreeBuilder injectorTreeBuilder) {
        this.injectorTreeBuilder = injectorTreeBuilder;
    }

    @Override
    @Nullable
    public Collection<String> getParentNode(String modId) {
        InjectorNode parentNode = this.injectorTreeBuilder.getChildParentNodes().get(modId);

        return parentNode == null ? null : parentNode.getModIds();
    }

    @Override
    public Collection<String> getNode(String modId) {
        Map<String, InjectorNode> nodesByModId = this.injectorTreeBuilder.getNodesByModId();
        if (nodesByModId.containsKey(modId)) {
            return nodesByModId.get(modId).getModIds();
        }
        return Set.of(modId);
    }
}
