package dev.the_fireplace.annotateddi.impl.injector;

import com.google.inject.*;
import dev.the_fireplace.annotateddi.api.Injectors;
import dev.the_fireplace.annotateddi.api.di.Implementation;
import dev.the_fireplace.annotateddi.impl.domain.loader.ModInjectableData;
import dev.the_fireplace.annotateddi.impl.domain.loader.InjectorNodeFinder;
import dev.the_fireplace.annotateddi.impl.domain.injector.InjectorFactory;

import java.util.*;

@Singleton
@Implementation
public final class InjectorFactoryImpl implements InjectorFactory
{
    private final ModInjectableData modInjectableData;
    private final Injector rootInjector;
    private final InjectorNodeFinder injectorNodeFinder;

    @Inject
    public InjectorFactoryImpl(ModInjectableData modInjectableData, Injector rootInjector, InjectorNodeFinder injectorNodeFinder) {
        this.modInjectableData = modInjectableData;
        this.rootInjector = rootInjector;
        this.injectorNodeFinder = injectorNodeFinder;
    }

    @Override
    public Injector create(Collection<String> node) {
        Injector parentInjector = getParentInjector(node);
        Collection<AbstractModule> abstractModules = modInjectableData.getModules(node);

        return parentInjector.createChildInjector(abstractModules);
    }

    private Injector getParentInjector(Collection<String> node) {
        Collection<String> parentNode = node;
        do {
            parentNode = injectorNodeFinder.getParentNode(parentNode.toArray(new String[0])[0]);
        } while (parentNode != null && !modInjectableData.isInjectable(parentNode));

        if (parentNode == null) {
            return rootInjector;
        }

        return Injectors.INSTANCE.getAutoInjector(parentNode.toArray(new String[0])[0]);
    }
}
