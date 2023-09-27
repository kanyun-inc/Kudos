**[English](README.md)** | 简体中文

[![License: Apache License Version 2.0](https://img.shields.io/badge/license-Apache%20License%202.0-blue.svg?style=flat)](LICENSE)
[![Language](https://img.shields.io/badge/Language-Kotlin-green)](https://kotlinlang.org/)
[![Maven Central](https://maven-badges.herokuapp.com/maven-central/com.kanyun.kudos/kudos-gradle-plugin/badge.svg)](https://maven-badges.herokuapp.com/maven-central/com.kanyun.kudos/kudos-gradle-plugin)

# Kudos

**Kudos** 是 **K**otlin **u**tilities for **d**eserializing **o**bjects 的缩写。它可以解决使用 Gson、Jackson 等框架反序列化 JSON 到 Kotlin 类时所存在的空安全问题和构造器默认值失效的问题。

## 问题背景

在使用常见的 JSON 序列化框架解析 JSON 时，Kotlin 开发者通常会面临无参构造器和属性空安全的问题。接下来我们通过举例来具体说明这几个问题。

```kotlin
data class User(val name: String) {
    val firstName by lazy {
        name.split(" ").first()
    }
    val lastName = name.split(" ").last()
}
```

数据类 `User` 没有默认的无参构造器，在使用 Gson 这样的框架解析如下 JSON 文本时：

```json
{"name": "Benny Huo"}
```

Gson 会因为 `User` 没有无参构造器而直接使用 `Unsafe` 来创建实例，这使得 `User` 的构造器不会正常被调用。也就是说，`firstName` 和 `lastName` 将不会被正确初始化。这实际上是非常危险的。 也有一些框架，例如 `Jackson`，它发现 `User` 没有无参构造器，则直接报错，拒绝对其进行反序列化。 

Kotlin 官方为了解决这个问题，推出了 `NoArg` 插件，这个插件会为使用特定注解标注的类型生成一个默认的无参构造器。

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

这里我们通过配置，为被 `@PoKo` 注解标注的类生成无参构造器，那么 `User` 会获得一个仅仅调用了父类无参构造器的构造器。这个构造器的函数体不包含任何指令，因此 `firstName` 和 `lastName` 仍然不会被正确初始化。

```kotlin
// build.gradle.kts
noArg {
    ...
    invokeInitializers = true
}
```

好在 NoArg 插件还提供了一个配置项 `invokeInitializers`。这个配置项默认是关闭的，打开它之后，会有一个好消息和一个坏消息，生成的无参构造器就可以正确初始化 `firstName` 属性了；坏消息是，构造器在尝试初始化 `lastName` 时，因为此时 `name` 尚未初始化，会直接抛出空指针异常。

实际开发中，更多的情况是主构造器的参数默认值的问题。例如：

```kotlin
@PoKo
data class User(
    val id: Long, 
    val name: String,
    val age: Int = -1,
    val tel: String = ""
)
```

不是所有的 `User` 都提供了自己的年龄和电话号码，我们希望在解析时，如果 JSON 中没有相应的字段，则直接使用默认值。但实际的情况是什么呢？

JSON 文本：

```json
{"id": 12, "name":  "Benny Huo"}
```

解析结果：

```
User(id=12, name=Benny Huo, age=0, tel=null)
```

这其中暴露了两个问题，一个是类主构造器中的参数默认值被完全忽略了，另一个则是不可空的 `tel` 属性的值被置为了 `null`，类型的空安全完全无法得到保障。

为此，我们通常的建议是使用 [Moshi](https://github.com/square/moshi) 或者 [kotlinx.serialization](https://github.com/kotlin/kotlinx.serialization)。

不过，切换框架往往并不容易。kotlinx.serialization 不支持 Java，适合纯 Kotlin 项目使用；Moshi 虽然也同时支持 Java，但它在解析时与 Gson 这样的框架在细节上仍然有不少差异。

另外，Moshi 也并不总是比 Gson 快的，我们看到的绝大多数评测在计算耗时时都忽略了 Moshi 的初始化耗时。在使用基于 KAPT/KSP 的代码生成方案时，由于 Moshi 会为每一个类创建专属的 `JsonAdapter`，它的初始化过程往往会比解析 JSON 本身耗费更多时间。我们在做 APP 的启动耗时优化时惊奇的发现，使用 Moshi 解析 JSON 的耗时居然比使用 Gson 多 2~3 倍。 

我们当时就一直在想，有没有什么办法为 Gson 这样的框架提供类型空安全和支持主构造器的参数默认值的能力呢？答案就是 **Kudos**。

## 快速上手

### 1. 添加插件到 classpath

```kotlin
// 方式 1
// 传统方式，在根目录的 build.gradle.kts 中添加以下代码
buildscript {
    repositories {
        mavenCentral()
        // SNAPSHOT 版本需要添加以下仓库
        maven("https://oss.sonatype.org/content/repositories/snapshots/")
    }
    dependencies {
        classpath("com.kanyun.kudos:kudos-gradle-plugin:$latest_version")
    }
}

subprojects {
    repositories {
        mavenCentral()
        // SNAPSHOT 版本需要添加以下仓库
        maven("https://oss.sonatype.org/content/repositories/snapshots/")
    }
}

// 方式 2
// 引用插件新方式，在 settings.gradle.kts 中添加以下代码
pluginManagement {
    repositories {
        mavenCentral()
        // SNAPSHOT 版本需要添加以下仓库
        maven("https://oss.sonatype.org/content/repositories/snapshots/")
    }
    plugins {
        id("com.kanyun.kudos") version "$latest_version" apply false
    }
}

dependencyResolutionManagement {
    repositories {
        mavenCentral()
        // SNAPSHOT 版本需要添加以下仓库
        maven("https://oss.sonatype.org/content/repositories/snapshots/")
    }
}
````

### 2. 在项目中启用插件

```kotlin
plugins {
    // 启用 Kudos 插件. 
    // 为被 @Kudos 注解标注的类生成优化版本的无参构造器
    id("com.kanyun.kudos")
}

kudos {
    // 启用 Kudos.Gson. 为被 @Kudos 标注的类同时生成 @JsonAdapter 注解，并添加 kudos-gson 依赖.
    gson = true
    // 启用 Kudos.Jackson. 添加 kudos-jackson 依赖.
    jackson = true
}
````

我们会为启用了以上插件的项目自动配置以下编译和运行时依赖：

```
com.kanyun.kudos:kudos-annotations
com.kanyun.kudos:kudos-runtime

// 仅当启用 Kudos.Gson 插件时
com.kanyun.kudos:kudos-gson

// 仅当启用 Kudos.Jackson 时
com.kanyun.kudos:kudos-jackson
```

当然，开发者也可以在合适的场景下手动引入这些依赖。

### 3. 为特定类启动 Kudos 的支持

对于需要添加 `Kudos` 解析支持的类型，直接添加 `@Kudos` 注解即可，例如：

```kotlin
@Kudos
data class User(
    val id: Long, 
    val name: String,
    val age: Int = -1,
    val tel: String = ""
)
```

编译时之后大致相当于：

```kotlin
@Kudos
// 如果启用了 com.kanyun.kudos.gson 插件，则生成 @JsonAdapter 注解
@JsonAdapter(value = KudosReflectiveTypeAdapterFactory::class)
data class User(
    val id: Long, 
    val name: String,
    val age: Int = -1,
    val tel: String = ""
) : KudosValidator {
    constructor() { // 生成的默认无参构造器
        super() // 调用父类默认无参构造器
        init<User>() // 调用 User 类内部的 init 块（包括定义在内部的属性初始化）
        this.age = -1 // 使用主构造器的参数默认值初始化属性
        this.tel = "" // 使用主构造器的参数默认值初始化属性
    }
    
    // 生成的用于校验字段空安全的函数
    override fun validate(status: Map<String, Boolean>) {
        validateField("id", status)
        validateField("name", status)
    }
}
```

接下来，使用 `Gson` 来解析 JSON 文本：

```kotlin
val user = kudosGson().fromJson("""{"id": 12, "name":  "Benny Huo"}""", User::class.java)
println(user) // User(id=12, name=Benny Huo, age=-1, tel=)
```

如果 JSON 中缺少 id 或者 name 字段，则解析失败，确保 User 属性的类型空安全。

### 4. 集合类型的支持

被 `@Kudos` 标注的类的属性类型如果是集合类型，包括 `List`、`Set` 等，解析之后会在 `validate` 函数中校验元素是否为 `null` 来确保类型空安全。但如果要解析的类型是 `List<User>`，Kudos 在运行时会因为无法获取到元素类型是否可空而无法提供类型空安全的保证。

为了解决这个问题，Kudos 提供了两个类型 `KudosList` 和 `KudosSet`，开发者可以在需要的场景下使用这两个类型来确保元素的类型空安全，例如：

```kotlin
val list = kudosGson().fromJson("""[null]""", typeOf<KudosList<User>>().javaType)
// java.lang.NullPointerException: Element cannot be null for com.kanyun.kudos.collections.KudosList.
```

更多测试用例，参见 [kudos-compiler/testData](kudos-compiler/testData)。

## 性能数据

基于 Kudos 的工作机制不难想到，Kudos 的运行耗时会略微多于对应的 JSON 序列化框架。

使用 Kudos.Gson 会比 Gson 多处理空安全校验等工作，这部分处理逻辑已经经过仔细优化，全部通过编译器生成 IR 来实现。空安全的校验耗时取决于数据类型的规模，基于现有的数据粗略估计 Kudos.Gson 的耗时为 Gson 的 1.1-1.2 倍。Kudos.Jackson 的情况类似。

在解析 JSON 时，考虑到冷启动的初始化耗时的情况，Kudos.Gson 比 Moshi 在大部分测试下性能更优（只有在多次解析同一数据类型时 Moshi 性能表现更好），因此 Kudos.Gson 在低频次的 JSON 解析场景下兼具了运行性能（优于 Moshi）和数据安全（优于 Gson）的优点。

>**说明** 详细数据和原始的测试工程仍然在开发当中，后续再进行补充。

## 版本兼容

### Kotlin

由于 Kotlin 编译器插件的 ABI 仍然没有公开发布，因此不同版本之间可能会存在一些兼容性问题。我们强烈建议使用与项目 Kotlin 版本对应的 Kudos 版本，例如 Kotlin 1.8.20 对应于 Kudos 1.8.20-x.y.z。

为了方便开发调试，我们也对实验中的 K2 编译器提供了完整的支持。

### Gson 

kudos-gson 模块依赖了 Gson 的一些内部逻辑，因此可能会存在一些兼容性问题。我们基于 2.4 ~ 2.10 等版本做了测试用例覆盖，并会在将来持续跟进对最新版本的支持。

### Jackson

kudos-jackson 模块依赖了 Jackson 的一些内部逻辑，因此可能会存在一些兼容性问题。我们基于 2.12.0 ~ 2.15.0 等版本做了测试用例覆盖，并会在将来持续跟进对最新版本的支持。

## 问题反馈

如果在使用的过程中遇到问题，欢迎提交 issue 与我们讨论。

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
