# Installation

ATick for Java is one Maven dependency. JNA and the matching native engine for your OS/arch ship
inside it and load automatically — there is no JNI build step on your side.

## Requirements

- **Java 8 or newer** (runs on 8, 11, 17, 21, …).
- A 32-bit **or** 64-bit JVM — both are supported.

## Maven

```xml
<dependency>
  <groupId>io.github.aniketc068</groupId>
  <artifactId>atick</artifactId>
  <version>1.0.3</version>
</dependency>
```

## Gradle

```groovy
implementation 'io.github.aniketc068:atick:1.0.3'
```

```groovy
// Kotlin DSL
implementation("io.github.aniketc068:atick:1.0.3")
```

## One artifact, every platform

The jar bundles a native engine per platform and JNA loads the right one at runtime, so the same
dependency works everywhere — Windows (64/32-bit), Linux and macOS — exactly like the Python package.

| Platform | Bundled |
|---|---|
| Windows 64-bit | `win32-x86-64` |
| Windows 32-bit | `win32-x86` |
| Linux x86-64 | `linux-x86-64` |
| Linux ARM64 | `linux-aarch64` |
| macOS Intel | `darwin-x86-64` |
| macOS Apple Silicon | `darwin-aarch64` |

## Verify the install

```java
import io.github.aniketc068.atick.Atick;

System.out.println(Atick.version());   // prints the engine version, e.g. 1.0.3
```
