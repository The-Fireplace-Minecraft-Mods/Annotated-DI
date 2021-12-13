package dev.the_fireplace.annotateddi.impl.logicalside;

import dev.the_fireplace.annotateddi.api.injectable.LogicalSideChecker;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Environment(EnvType.SERVER)
public final class DedicatedLogicalSide implements LogicalSideChecker
{
    @Override
    public EnvType getLogicalSide() {
        return EnvType.SERVER;
    }
}
