# Annotated DI
[![CurseForge](http://cf.way2muchnoise.eu/short_501373_downloads.svg)](https://minecraft.curseforge.com/projects/annotated-di)

Annotated DI adds Dependency Injection for Minecraft mods. It includes [Guice](https://github.com/google/guice) for the basic DI system, and adds a custom annotation to allow easily setting up a DI system configured only by annotations.

## Setup
To use this with your mod, include the following in `build.gradle`:
```
dependencies {
  modImplementation "dev.the-fireplace:Annotated-DI:${project.annotateddi_version}"
}
```
And in `gradle.properties`:
```
annotateddi_version=<mod version>+<minecraft version>
```

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