package dev.the_fireplace.annotateddi.impl.loader;

import com.google.common.collect.Sets;

import java.util.Set;

class InjectorNode
{
    private final Set<String> modIds;

    InjectorNode(Set<String> modIds) {
        this.modIds = modIds;
    }

    public Set<String> getModIds() {
        return modIds;
    }

    public InjectorNode with(String modId) {
        return withAll(Set.of(modId));
    }

    public InjectorNode withAll(Set<String> modIds) {
        return new InjectorNode(Sets.union(this.modIds, modIds));
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (obj instanceof InjectorNode otherNode) {
            return this.modIds.equals(otherNode.modIds);
        }
        return false;
    }
}
