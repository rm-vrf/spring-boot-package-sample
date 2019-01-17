# spring-boot-package-sample

## Maven

使用 Maven 开发 Spring Boot 程序，需要在 `pom.xml` 中添加几个构建参数：

- 设置 `org.springframework.boot:spring-boot-starter-parent` 父项目，这是一个 `pom` 类型的包，他管理一些重要的参数、定义依赖项版本、引入默认插件：

```xml
<parent>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-parent</artifactId>
    <version>2.0.5.RELEASE</version>
    <relativePath /> <!-- lookup parent from repository -->
</parent>
```

- 定义 Spring Cloud 依赖，对于 Spring Boot 程序来说这不是必须的。一般会这样做：

```xml
<dependencyManagement>
    <dependencies>
        <dependency>
            <groupId>org.springframework.cloud</groupId>
            <artifactId>spring-cloud-dependencies</artifactId>
            <version>${spring-cloud.version}</version>
            <type>pom</type>
            <scope>import</scope>
        </dependency>
    </dependencies>
</dependencyManagement>
```

- 选择项目需要的 `starter`，这里列出了两个常用的 `starter`：

```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-web</artifactId>
</dependency>
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-actuator</artifactId>
</dependency>
```

- 添加打包插件: `org.springframework.boot:spring-boot-maven-plugin`，这个插件用来构建 `executable jar`：

```xml
<build>
    <plugins>
        <plugin>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-maven-plugin</artifactId>
        </plugin>
    </plugins>
</build>
```

运行编译命令，在 `target` 目录生成产物：`spring-boot-package-sample-1.0.0-SNAPSHOT.jar`：

```shell
$ mvn package
```

## Executable Jar

编译产物是一个 `Spring Boot Executable Jar`，解开这个 `jar` 看看他的内部有什么：

```shell
$ jar -xvf spring-boot-package-sample-1.0.0-SNAPSHOT.jar

$ tree spring-boot-package-sample-1.0.0-SNAPSHOT
spring-boot-package-sample-1.0.0-SNAPSHOT
├── BOOT-INF
│   ├── classes
│   │   ├── application.properties
│   │   └── com
│   │       └── mydomain
│   │           └── app
│   │               └── package_
│   │                   └── Main.class
│   └── lib
│       ├── HdrHistogram-2.1.10.jar
│       ├── LatencyUtils-2.0.3.jar
│       ├── accessors-smart-1.2.jar
│       ...
│       ├── spring-boot-starter-2.0.5.RELEASE.jar
│       ├── spring-boot-starter-actuator-2.0.5.RELEASE.jar
│       ├── spring-boot-starter-json-2.0.5.RELEASE.jar
│       ├── spring-boot-starter-logging-2.0.5.RELEASE.jar
│       ├── spring-boot-starter-test-2.0.5.RELEASE.jar
│       └── xmlunit-core-2.5.1.jar
├── META-INF
│   ├── MANIFEST.MF
│   └── maven
│       └── com.mydomain.app
│           └── spring-boot-package-sample
│               ├── pom.properties
│               └── pom.xml
└── org
    └── springframework
        └── boot
            └── loader
                ├── ExecutableArchiveLauncher.class
                ├── JarLauncher.class
                ├── LaunchedURLClassLoader$UseFastConnectionExceptionsEnumeration.class
                ├── LaunchedURLClassLoader.class
                ├── Launcher.class
                ├── MainMethodRunner.class
                ├── PropertiesLauncher$1.class
                ├── PropertiesLauncher$ArchiveEntryFilter.class
                ├── PropertiesLauncher$PrefixMatchingArchiveFilter.class
                ├── PropertiesLauncher.class
                ├── WarLauncher.class
                ...
```

展开文件有很多，这里挑重点展示了一部分，介绍一下：

### 进入点

Spring Boot 程序有 2 个进入点：一个是 `com.mydomain.app.package_.Main`，这是开发者定义的进入点，里面有自己写的 `public static void main`。开发的时候在 IDE 里面使用这个进入点，Spring Boot 会启动一个内嵌的 `Tomcat`，开发者就不需要在 IDE 中做容器配置。开发调试方便了很多；

另一个进入点是 `executable jar` 的进入点，位置在 `org.springframework.boot.loader.JarLauncher`. 这个进入点是在 `MANIFEST.MF` 中定义的：

```
Main-Class: org.springframework.boot.loader.JarLauncher
```

当运行 `java -jar` 的时候，启动的就是这个进入点。

### 类加载器

开发调试的时候 IDE 使用默认类加载器打开程序；

运行 `executable jar` 的时候，Spring Boot 会创建一个特殊的类加载器：`org.springframework.boot.loader.LaunchedURLClassLoader`. 这个类加载器在两个位置加载类：`BOOT-INF/lib/*`、`BOOT-INF/classes/`, 外部依赖项和开发者自己写的类分别保存在这两个位置。

> 这种类加载规则是 Spring Boot 独有的。有时候开发者在代码中使用其他的类加载器，如果这些类加载器没有严格遵守 “双亲委派模型”，可能会找不到 `bytecode` 的位置，造成类加载错误。这时候可以选择其他打包技术，比如 `fatjar`，效果也是相似的。

程序打包成 `executable jar` 有很多好处：

- 打包产物是一个自包含依赖的 `jar` 文件，部署方便；
- 运行的时候不需要处理 `-classpath` 参数，或者编写复杂的启动脚本，用 `java -jar` 就可以启动；
- 包含一个内嵌的 `Tomcat`, 不需要单独部署一个 `Tomcat`, 不会发生依赖冲突；
- 所有的部署产物在一个文件里，不用展开运行，也没有办法改动内部的任何代码和配置，避免了“配置漂移”，这是一个“不可变服务”.

## 其他打包技术

### Gradle

`Gradle` 是基于 `Groovy` 语言的构建工具，使用 `DSL` 语法定义构建过程，简洁、灵活、可读性强。`Gradle` 沿用了 `Maven` 的依赖管理体系，采用一致的目录结构，在构建周期和插件方面做了一些改进。示例代码提供 `build.gradle` 构建脚本，先看一下可用的任务名称：

```shell
$ gradle tasks

> Task :tasks

------------------------------------------------------------
All tasks runnable from root project
------------------------------------------------------------

Application tasks
-----------------
bootRun - Runs this project as a Spring Boot application.

Build tasks
-----------
assemble - Assembles the outputs of this project.
bootJar - Assembles an executable jar archive containing the main classes and their dependencies.
build - Assembles and tests this project.
buildDependents - Assembles and tests this project and all projects that depend on it.
buildNeeded - Assembles and tests this project and all projects it depends on.
classes - Assembles main classes.
clean - Deletes the build directory.
jar - Assembles a jar archive containing the main classes.
testClasses - Assembles test classes.
...
```

执行构建命令，生成 `executable jar`：

```shell
$ gradle bootJar
```

> 在项目中使用 `Gradle` 的推荐方式是 `Gradle wrapper`。先使用 `Gradle wrapper` 生成 `gradlew` 和相关的依赖项，再把这些依赖项提交到代码管理工具中。确保每个开发者使用的 `Gradle` 版本一致、依赖一致，避免出现构建不稳定的问题。 

### Ant

`Maven` 和 `Gradle` 倡导的自动化的依赖管理方式，规范构建过程。这样当然很方便，但是有些情况下不好使用，比如：

- 构建环境不允许连接到外网，不能下载公共 `Maven` 仓库里的依赖项；
- 技术部把内网的 `Maven` 仓库管理的一塌糊涂，缺少一些重要的包，还有一些版本是错误的，依赖关系也经常不对，编译过不去；
- 编译过程很复杂，比如要用 `JNI` 连接 `C++` 代码，还要用工具生成一些接口的调动桩代码……

这时候就可以用 `Ant` 来做构建。比起 `Maven` 和 `Gradle`，`Ant` 更像一个原始的构建脚本，给开发者更多的自由度，也要自己处理一些复杂的过程。示例代码提供了构建脚本 `build.xml`. 

依赖库在 `dependencies/lib` 和 `dependencies/test` 目录，开发者要自己管理。`dependencies/ant` 里面是打包 `executable jar` 需要使用的库。运行 `ant usage` 查看构建目标：

```shell
$ ant usage

usage:
     [echo]
     [echo] spring-boot-package-sample build file
     [echo] -----------------------------------
     [echo]
     [echo] Available targets are:
     [echo]
     [echo] clean --> clean the project and remove all files generated by the previous build
     [echo] init --> create the build directory if it doesn't exist
     [echo] compile --> compile the source code
     [echo] javadoc --> generate standard javadoc output
     [echo] test --> run unit tests
     [echo] package --> package compiled source code into the distributable format
     [echo]
```

使用 `package` 目标编译并且打包 `executable jar`:

```shell
$ ant package
```


