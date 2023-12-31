English | **[简体中文](README_zh.md)**

[![License: Apache License Version 2.0](https://img.shields.io/badge/license-Apache%20License%202.0-blue.svg?style=flat)](LICENSE)
[![Language](https://img.shields.io/badge/Language-Kotlin-green)](https://kotlinlang.org/)
[![Maven Central](https://maven-badges.herokuapp.com/maven-central/com.kanyun.kudos/kudos-gradle-plugin/badge.svg)](https://maven-badges.herokuapp.com/maven-central/com.kanyun.kudos/kudos-gradle-plugin)

# Kudos

**Kudos** is short for **K**otlin **u**tilities for **d**eserializing **o**bjects. It is designed to make it safer and easier to deserializing Kotlin classes with Gson and Jackson. Moreover, it can also simplify the use of the high-performance deserialization framework, JsonReader.

## Background
### Null Safety and Default Values
When parsing JSON using common JSON serialization frameworks, Kotlin developers often face issues with no-arg constructors and property null safety. Let's illustrate these issues with some examples.

```kotlin
data class User(val name: String) {
    val firstName by lazy {
        name.split(" ").first()
    }
    val lastName = name.split(" ").last()
}
```

The data class `User` does not have a default no-arg constructor. When using a framework like Gson to parse the following JSON text:

```json
{"name": "Benny Huo"}
```

Gson will create an instance of `User` using `Unsafe` because `User` lacks a no-arg constructor, which means that the constructor of `User` won't be invoked correctly. In other words, `firstName` and `lastName` will not be initialized correctly. This is actually very risky. Some frameworks, like Jackson, will throw an error when they find that `User` lacks a no-arg constructor, refusing to deserialize it.

To address this issue, Kotlin provides the `NoArg` plugin, which generates a default no-arg constructor for types annotated with a specific annotation.

```kotlin
// build.gradle.kts
plugins {
    kotlin("plugin.noarg") version "$kotlinVersion"
}

noArg {
    annotation("com.kanyun.annotations.PoKo")
}

// User.kt
@PoKo
data class User(val name: String) {
    val firstName by lazy {
        name.split(" ").first()
    }
    val lastName = name.split(" ").last()
}
```

We generate a no-arg constructor for classes annotated with `@PoKo`. In this case, `User` will have a constructor that simply calls the parent class's no-arg constructor. This constructor has an empty body, so `firstName` and `lastName` will still not be initialized correctly.

```kotlin
// build.gradle.kts
noArg {
    ...
    invokeInitializers = true
}
```

Fortunately, the `NoArg` plugin also provides a option called `invokeInitializers`. By default, this option is disabled. When you enable it, the generated no-arg constructor can correctly initialize the `firstName` property. However, the bad news is that when the constructor tries to initialize `lastName`, it will throw a `NullPointerException` because `name` has not been initialized at that point.

Another common issue is dealing with default values for parameters in the primary constructor. For example:

```kotlin
@PoKo
data class User(
    val id: Long, 
    val name: String,
    val age: Int = -1,
    val tel: String = ""
)
```

Not all User instances provide their own age and phone number, so we expect that when parsing, if the JSON does not contain the corresponding fields, the default values should be used. 

JSON text：

```json
{"id": 12, "name":  "Benny Huo"}
```

The result:

```
User(id=12, name=Benny Huo, age=0, tel=null)
```

This exposes two problems: one is that the default values for parameters in the primary constructor are completely ignored, and the other is that the non-nullable `tel` property is set to null, completely undermining type safety.

For these reasons, we typically recommend using [Moshi](https://github.com/square/moshi) or [kotlinx.serialization](https://github.com/kotlin/kotlinx.serialization).

However, migrating to these frameworks is not easy. kotlinx.serialization does not support Java. Moshi, while supporting Java, still has some differences in detail compared to Gson.

Furthermore, Moshi is not always faster than Gson as benchmarks said. Most benchmark tests we have seen totally ignored the initialization time of Moshi. When using Moshi-codegen based on KAPT/KSP, Moshi often takes more time to initialize because it creates a dedicated `JsonAdapter` for each class. We were surprised to find that using Moshi to parse JSON took 2-3 times longer than using Gson during our app's startup time optimization.

We kept thinking, is there a way to provide null safety and default values support of the primary constructor parameters for frameworks like Gson? The answer is **Kudos**.

### Performance
In actual use, it can be found that powerful frameworks like Gson, Moshi do not have great advantages in performance, while the system's native JsonReader has a clear edge in performance, especially during code cold start stages when it's not fully JIT optimized. However, JsonReader is costly to use, requiring developers to manually parse JSON, which is unfriendly for most developers.

For this reason, Kudos provides a simple way for developers to use JsonReader through a customized compiler plugin, while ensuring the functionality of type null safety and primary constructor parameter default values.

## Quick Start

### 1. Introduce Kudos into Gradle Projects

#### 1.1 Add the plugin to classpath

```kotlin
// Option 1
// The classic way, add the following code to build.gradle.kts in the root directory
buildscript {
    repositories {
        mavenCentral()
        // for snapshots
        maven("https://s01.oss.sonatype.org/content/repositories/snapshots")
    }
    dependencies {
        classpath("com.kanyun.kudos:kudos-gradle-plugin:$latest_version")
    }
}

subprojects {
    repositories {
        mavenCentral()
        // for snapshots
        maven("https://s01.oss.sonatype.org/content/repositories/snapshots")
    }
}

// Option 2
// The new way, add the following code to settings.gradle.kts
pluginManagement {
    repositories {
        mavenCentral()
        // for snapshots
        maven("https://s01.oss.sonatype.org/content/repositories/snapshots")
    }
    plugins {
        id("com.kanyun.kudos") version "$latest_version" apply false
    }
}

dependencyResolutionManagement {
    repositories {
        mavenCentral()
        // for snapshots
        maven("https://s01.oss.sonatype.org/content/repositories/snapshots")
    }
}
````

#### 1.2 Apply the plugin

```kotlin
plugins {
    // Apply Kudos plugin. 
    // An enhanced no-arg constructor will be generated for classes annotated with @Kudos.
    id("com.kanyun.kudos")
}

kudos {
    // Enable Kudos.Gson. Generate @JsonAdapter for kudos classes and add kudos-gson to dependencies.
    gson = true
    // Enable Kudos.Jackson. Add kudos-jackson to dependencies.
    jackson = true
    // Enable Kudos.AndroidJsonReader. Add kudos-android-json-reader to dependencies.
    androidJsonReader = true
}
````

Dependencies below will be added when the plugins are applied.

```
com.kanyun.kudos:kudos-annotations
com.kanyun.kudos:kudos-runtime

// Only for Kudos.Gson
com.kanyun.kudos:kudos-gson

// Only for Kudos.Jackson
com.kanyun.kudos:kudos-jackson
```

You can add these libraries to your dependencies explicitly if needed.

### 2. Introduce Kudos into Maven Projects

#### 2.1 Project Dependencies

Add `kudos-gson` or `kudos-jackson` into the dependencies of your project. 

```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/maven-v4_0_0.xsd">
    ...
    <dependencies>
        ...
        <!-- for gson -->
        <dependency>
            <groupId>com.kanyun.kudos</groupId>
            <artifactId>kudos-gson</artifactId>
            <version>${kudos.version}</version>
        </dependency>
        <dependency>
            <groupId>com.google.code.gson</groupId>
            <artifactId>gson</artifactId>
            <version>2.10</version>
        </dependency>

        <!-- for jackson -->
        <dependency>
            <groupId>com.kanyun.kudos</groupId>
            <artifactId>kudos-jackson</artifactId>
            <version>${kudos.version}</version>
        </dependency>
        <dependency>
            <groupId>com.fasterxml.jackson.core</groupId>
            <artifactId>jackson-databind</artifactId>
            <version>2.15.0</version>
        </dependency>
    </dependencies>
    ...
</project>
```

#### 2.2 Configure the Compiler

```xml
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/maven-v4_0_0.xsd">
    ...

    <build>
        <sourceDirectory>src/main/java</sourceDirectory>
        <testSourceDirectory>src/test/java</testSourceDirectory>

        <plugins>
            <plugin>
                <groupId>org.jetbrains.kotlin</groupId>
                <artifactId>kotlin-maven-plugin</artifactId>
                <version>${kotlin.version}</version>

                <configuration>
                    <compilerPlugins>
                        <!-- Tell Kotlin compiler to load Kudos compiler plugin -->
                        <plugin>kudos</plugin>
                    </compilerPlugins>

                    <pluginOptions>
                        <!-- for gson -->
                        <option>kudos:gson=true</option>
                        <!-- for jackson -->
                        <option>kudos:jackson=true</option>
                    </pluginOptions>
                </configuration>

                <dependencies>
                    <!-- add Kudos compiler plugin to the classpath of Kotlin compiler -->
                    <dependency>
                        <groupId>com.kanyun.kudos</groupId>
                        <artifactId>kudos-maven-plugin</artifactId>
                        <version>${kudos.version}</version>
                    </dependency>
                </dependencies>

                <executions>...</executions>
            </plugin>
        </plugins>
    </build>

</project>
```

### 3. Annotate classes with `@Kudos`

For types that need to add `Kudos` parsing support, simply add the `@Kudos` annotation, for example:


```kotlin
@Kudos
data class User(
    val id: Long, 
    val name: String,
    val age: Int = -1,
    val tel: String = ""
)
```

After compilation, it is roughly equivalent to:

```kotlin
@Kudos
// If the 'com.kanyun.kudos.gson' plugin is enabled, the @JsonAdapter annotation is generated
@JsonAdapter(value = KudosReflectiveTypeAdapterFactory::class)
data class User(
    val id: Long, 
    val name: String,
    val age: Int = -1,
    val tel: String = ""
) : KudosValidator {
    constructor() { // Generated default no-arg constructor
        super() // Call the default no-arg constructor of the parent class
        init<User>() // Call the init block of User(including the initializations of the declared properties)
        this.age = -1 // Initialize the property with the default value from the primary constructor
        this.tel = "" // Initialize the property with the default value from the primary constructor
    }

    // Function for validating null safety
    override fun validate(status: Map<String, Boolean>) {
        validateField("id", status)
        validateField("name", status)
    }
}
```

Use `Gson` to parse the JSON text:

```kotlin
val user = kudosGson().fromJson("""{"id": 12, "name":  "Benny Huo"}""", User::class.java)
println(user) // User(id=12, name=Benny Huo, age=-1, tel=)
```

If the JSON lacks the `id` or `name` field, the parsing fails, ensuring that the properties of `User` is null safe.

The `Kudos` annotation also supports adding parameters like `KUDOS_GSON`, `KUDOS_JACKSON`, `KUDOS_ANDROID_JSON_READER`, to enable support for specific libraries, and also allows passing multiple parameters at the same time, for example:

```kotlin
@Kudos(KUDOS_GSON, KUDOS_ANDROID_JSON_READER)
data class User(
    val id: Long,
    val name: String,
    val age: Int = -1,
    val tel: String = ""
)
```

### 4. Collections

Properties of collection types declared in classes annotated with @Kudos, such as `List` or `Set`, will be handled carefully in the `validate` function to ensure the null safety for their elements. However, if the type to be parsed is like `List<User>`, Kudos will not be able to provide null safety guarantees at runtime because it cannot obtain whether the element type is nullable.

To solve this problem, Kudos provides `KudosList` and `KudosSet`. You can use these types in the required scenarios to ensure the null safety of elements, for example:

```kotlin
val list = kudosGson().fromJson("""[null]""", typeOf<KudosList<User>>().javaType)
// java.lang.NullPointerException: Element cannot be null for com.kanyun.kudos.collections.KudosList.
```

For more test cases, see [kudos-compiler/testData](kudos-compiler/testData).

## Benchmarks

Kudos is a little slower than the according JSON tool it supports for nullability check it provides.

The code of nullability check has been optimized carefully which is generated by Kotlin Compiler directly without any runtime reflection or annotation stuffs. Based on our tests, Kudos.Gson costs only 10%~20% more time than pure Gson when deserializing in exchange for null safety.

We also find out that Kudos.Gson performs better than Moshi on initializing. So it should be a better choice for low frequency deserialization scenarios with both performance(comparing to Moshi) and null safety(comparing to Gson).

### Multi-run test results
|                  | small json     | medium json    | large json     |
|------------------|----------------|----------------|----------------|
| Gson             | 412,375   ns   | 1,374,838   ns | 3,641,904   ns |
| Kudos-Gson       | 517,123   ns   | 1,686,568   ns | 4,311,910   ns |
| Jackson          | 1,035,010   ns | 1,750,709   ns | 3,450,974   ns |
| Kudos-Jackson    | 1,261,026   ns | 2,030,874   ns | 3,939,600   ns |
| JsonReader       | 190,302   ns   | 1,176,479   ns | 3,464,174   ns |
| Kudos-JsonReader | 215,974   ns   | 1,359,587   ns | 4,019,024   ns |

### One-run test results
|                  | small json      | medium json     | large json      |
|------------------|-----------------|-----------------|-----------------|
| Gson             | 3,974,219   ns  | 4,666,927   ns  | 8,271,355   ns  |
| Kudos-Gson       | 4,531,718   ns  | 6,244,479   ns  | 11,160,782   ns |
| Jackson          | 12,821,094   ns | 13,930,625   ns | 15,989,791   ns |
| Kudos-Jackson    | 13,233,750   ns | 15,674,010   ns | 18,641,302   ns |
| JsonReader       | 662,032   ns    | 2,056,666   ns  | 4,624,687   ns  |
| Kudos-JsonReader | 734,907   ns    | 2,362,010   ns  | 6,212,917   ns  |

For more detail, see [https://github.com/RicardoJiang/json-benchmark](https://github.com/RicardoJiang/json-benchmark)

## Version Support

### Kotlin 

Since the ABI of Kotlin compiler plugins has not been stable yet, there may be some compatibility issues between different versions. We strongly recommend using the **Kudos** version corresponding to the Kotlin version, for example, Kotlin 1.8.20 with Kudos 1.8.20-x.y.z.

BTW, We also provide full support for the experimental K2 compiler.

### Gson

Kudos is tested on Gson 2.4-2.10. We will also follow up with the new versions of Gson in the future.

### Jackson

Kudos is tested on Jackson 2.12.0-2.15.0. We will also follow up with the new versions of Jackson in the future.

## Feedback

If you encounter any problems, please feel free to submit an issue.

## License
````
Copyright (C) 2023 Kanyun, Inc.

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

   http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
````
