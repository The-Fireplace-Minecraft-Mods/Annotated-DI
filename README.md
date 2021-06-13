# Annotated DI
[![CurseForge](http://cf.way2muchnoise.eu/short_000000_downloads.svg)](https://minecraft.curseforge.com/projects/annotated-di)

Annotated DI adds Dependency Injection for Minecraft mods. It includes Guice for the basic DI system, and adds a custom annotation to allow easily setting up a DI system configured only by annotations.

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
A simple example:
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
Using SomeOtherFile without having to find the NetworkInterface to construct it yourself
Method 1 (Setter injection):
```
public class ThirdFile {

    private SomeOtherFile otherFile;

    public ThirdFile() {
        
    }

    public void callSomeOtherFile() {
        otherFile.doStuff();
    }
    
    @Inject
    public void setSomeOtherFile(SomeOtherFile otherFile) {
        this.otherFile = otherFile;
    }
}
```
Method 2 (Constructor injection again - this should be fine for most places, but something somewhere will have to be the way in):
```
public class ThirdFile {

    private final SomeOtherFile otherFile;

    @Inject
    public ThirdFile(SomeOtherFile otherFile) {
        this.otherFile = otherFile;
    }

    public void callSomeOtherFile() {
        otherFile.doStuff();
    }
}
```
Method 3 (Use sparingly - Constructor Injection is best for most places):
```
public void callSomeOtherFile() {
    AnnotatedDI.getInjector().getInstance(SomeOtherFile.class).doStuff();
}
```