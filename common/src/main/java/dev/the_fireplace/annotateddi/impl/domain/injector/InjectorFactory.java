package dev.the_fireplace.annotateddi.impl.domain.injector;

import com.google.inject.Injector;

import java.util.Collection;

public interface InjectorFactory
{
    Injector create(Collection<String> node);
}
