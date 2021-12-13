package dev.the_fireplace.annotateddi.impl.di;

import com.google.inject.Provider;

public class ProxiedImplementationProvider<T> implements Provider<T>
{
    @Override
    public T get() {
        return null;
    }
}
