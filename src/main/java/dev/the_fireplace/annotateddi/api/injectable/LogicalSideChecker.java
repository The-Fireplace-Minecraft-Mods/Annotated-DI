package dev.the_fireplace.annotateddi.api.injectable;

import net.fabricmc.api.EnvType;

import javax.annotation.Nullable;

public interface LogicalSideChecker
{
    /**
     * Attempt to get logical thread by all means available. The only missing cases (that I know of) are separate threads spawned outside of Minecraft and Annotated DI systems.
     * Please use {@link LogicalSidedThreadFactory} to create threads that work with this system.
     */
    @Nullable
    EnvType getLogicalSide();
}
