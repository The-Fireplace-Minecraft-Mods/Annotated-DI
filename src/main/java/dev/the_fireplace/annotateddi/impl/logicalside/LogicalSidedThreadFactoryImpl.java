package dev.the_fireplace.annotateddi.impl.logicalside;

import dev.the_fireplace.annotateddi.api.injectable.LogicalSideChecker;
import dev.the_fireplace.annotateddi.api.injectable.LogicalSidedThreadFactory;
import dev.the_fireplace.annotateddi.impl.logicalside.thread.ClientThread;
import dev.the_fireplace.annotateddi.impl.logicalside.thread.ServerThread;
import net.fabricmc.api.EnvType;

import javax.inject.Inject;

public class LogicalSidedThreadFactoryImpl implements LogicalSidedThreadFactory
{
    private final LogicalSideChecker logicalSideChecker;

    @Inject
    public LogicalSidedThreadFactoryImpl(LogicalSideChecker logicalSideChecker) {
        this.logicalSideChecker = logicalSideChecker;
    }

    @Override
    public Thread createNewThread(Runnable runnable) {
        EnvType threadType = logicalSideChecker.getLogicalSide();
        if (threadType == null) {
            return new Thread(runnable);
        }
        if (threadType == EnvType.CLIENT) {
            return new ClientThread(runnable);
        }
        if (threadType == EnvType.SERVER) {
            return new ServerThread(runnable);
        }

        throw new IllegalStateException("Unknown thread type!");
    }
}
