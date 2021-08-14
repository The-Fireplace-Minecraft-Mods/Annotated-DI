# Annotated DI
[![CurseForge](http://cf.way2muchnoise.eu/short_501373_downloads.svg)](https://minecraft.curseforge.com/projects/annotated-di) [![Maven Central](https://maven-badges.herokuapp.com/maven-central/dev.the-fireplace/Annotated-DI/badge.png)](https://maven-badges.herokuapp.com/maven-central/dev.the-fireplace/Annotated-DI)

Annotated DI adds Dependency Injection for Minecraft mods. It includes [Guice](https://github.com/google/guice) for the basic DI system, and adds a custom annotation to allow easily setting up a DI system configured only by annotations.

## Setup
To use this with your mod, include the following in `build.gradle`:
```
dependencies {
  modImplementation "dev.the-fireplace:Annotated-DI:${project.annotateddi_version}"
  annotationProcessor "dev.the-fireplace:Annotated-DI:${project.annotateddi_version}:processor"
}
```
And in `gradle.properties`:
```
annotateddi_version=<mod version>+<minecraft version>
```

After that, the easiest way to get access to the Injector (and make sure it's ready to use by the time your mod initializes) is to use the DI versions of the standard entrypoints. To do so, edit your `fabric.mod.json` to add `di-` in front of the `main`, `client`, or `server` entrypoint name, making it `di-main`, `di-client`, or `di-server`, respectively. Then switch the interface your entrypoint uses for the DI equivalent:

`ModInitializer` => `DIModInitializer`

`ClientModInitializer` => `ClientDIModInitializer`

`DedicatedServerModInitializer` => `DedicatedServerDIModInitializer`

The initialization functions will now take one parameter, the Injector.

## Usage
A simple example of how the custom `@Implementation` annotation is used:
NetworkInterface.java
```
public interface NetworkInterface {
    ByteBuf getBuffer();
}
```
BufferFactory.java
```
@Implementation
public class BufferFactory implements NetworkInterface {
    @Override
    public ByteBuf getBuffer() {
        // do stuff
    }
}
```
SomeOtherFile.java (Using Constructor Injection)
```
public class SomeOtherFile {
    private final NetworkInterface networkInterface;
    
    @Inject
    public SomeOtherFile(NetworkInterface interface) {
        this.networkInterface = interface;
    }
    
    public void doStuff() {
        ByteBuf buffer = this.networkInterface.getBuffer();
    }
}
```

`@Implementation` annotation with multiple interfaces:
NetworkInterface.java
```
public interface NetworkInterface {
    ByteBuf getBuffer();
}
```
BufferFactory.java
```
@Implementation({NetworkInterface.class})
public class BufferFactory implements NetworkInterface, SomeOtherInterface {
    @Override
    public ByteBuf getBuffer() {
        // do stuff
    }
}
```
SomeOtherFile.java (Using Constructor Injection)
```
public class SomeOtherFile {
    private final NetworkInterface networkInterface;
    
    @Inject
    public SomeOtherFile(NetworkInterface interface) {
        this.networkInterface = interface;
    }
    
    public void doStuff() {
        ByteBuf buffer = this.networkInterface.getBuffer();
    }
}
```

### Advanced
In some cases, it may be necessary to access the Injector directly, such as when you're in an entrypoint added by another mod - it's generally better to avoid it if possible, but you can also call `DIContainer.get()` to access to the Injector.

To do more advanced configuration of your injections, you can use the `di-module` entrypoint with a class implementing `DIModuleCreator`. See Guice's documentation for more information on Modules.

### Troubleshooting
Be sure to add `@Environment(EnvType.CLIENT)` to injectable classes that are only meant to be on the client side.