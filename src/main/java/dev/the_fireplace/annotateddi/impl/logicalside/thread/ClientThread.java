package dev.the_fireplace.annotateddi.impl.logicalside.thread;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class ClientThread extends Thread
{
    public ClientThread() {
        super();
    }

    public ClientThread(Runnable target) {
        super(target);
    }

    public ClientThread(@Nullable ThreadGroup group, Runnable target) {
        super(group, target);
    }

    public ClientThread(@NotNull String name) {
        super(name);
    }

    public ClientThread(@Nullable ThreadGroup group, @NotNull String name) {
        super(group, name);
    }

    public ClientThread(Runnable target, String name) {
        super(target, name);
    }

    public ClientThread(@Nullable ThreadGroup group, Runnable target, @NotNull String name) {
        super(group, target, name);
    }

    public ClientThread(@Nullable ThreadGroup group, Runnable target, @NotNull String name, long stackSize) {
        super(group, target, name, stackSize);
    }

    public ClientThread(ThreadGroup group, Runnable target, String name, long stackSize, boolean inheritThreadLocals) {
        super(group, target, name, stackSize, inheritThreadLocals);
    }
}
