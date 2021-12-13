package dev.the_fireplace.annotateddi.impl.logicalside;

import dev.the_fireplace.annotateddi.api.injectable.LogicalSideChecker;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

import javax.annotation.Nullable;

@Environment(EnvType.SERVER)
public final class DedicatedLogicalSide implements LogicalSideChecker
{
    @Override
    public @Nullable
    EnvType getLogicalSide() {
        return EnvType.SERVER;
    }
}
