package dev.the_fireplace.annotateddi.impl.logicalside.thread;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class ServerThread extends Thread
{
    public ServerThread() {
        super();
    }

    public ServerThread(Runnable target) {
        super(target);
    }

    public ServerThread(@Nullable ThreadGroup group, Runnable target) {
        super(group, target);
    }

    public ServerThread(@NotNull String name) {
        super(name);
    }

    public ServerThread(@Nullable ThreadGroup group, @NotNull String name) {
        super(group, name);
    }

    public ServerThread(Runnable target, String name) {
        super(target, name);
    }

    public ServerThread(@Nullable ThreadGroup group, Runnable target, @NotNull String name) {
        super(group, target, name);
    }

    public ServerThread(@Nullable ThreadGroup group, Runnable target, @NotNull String name, long stackSize) {
        super(group, target, name, stackSize);
    }

    public ServerThread(ThreadGroup group, Runnable target, String name, long stackSize, boolean inheritThreadLocals) {
        super(group, target, name, stackSize, inheritThreadLocals);
    }
}
