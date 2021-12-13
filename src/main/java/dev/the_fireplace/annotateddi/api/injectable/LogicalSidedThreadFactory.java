package dev.the_fireplace.annotateddi.api.injectable;

public interface LogicalSidedThreadFactory
{
    /**
     * Create a new thread with side data attached. Don't forget to start it!
     */
    Thread createNewThread(Runnable runnable);
}
