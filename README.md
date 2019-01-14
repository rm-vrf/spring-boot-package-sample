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

- 根据项目选择需要的 `starter`，这里选择了两个常用的 `starter`：

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

- 添加一个打包插件: `org.springframework.boot:spring-boot-maven-plugin`，这个插件可以构建 `executable jar`：

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

运行编译命令，可以在 `target` 目录看到产物，`spring-boot-package-sample-1.0.0-SNAPSHOT.jar`：

```shell
$ mvn package
```

## Executable Jar

编译产物一个 `Spring Boot Executable Jar`，解开这个 `jar` 包我们看看内部的文件：

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

展开的文件有很多，这里挑重点展示了一部分，介绍一下：

### 进入点

程序有 2 个进入点：一个是 `com.mydomain.app.package_.Main`，这是开发者自己的程序定义的进入点，里面有自己写的 `public static void main`。开发的时候在 IDE 里面使用这个进入点，Spring Boot 会启动一个内嵌的 `Tomcat`，这样开发者就不需要在 IDE 中做复杂的配置。开发调试方便多了；

另一个进入点是 Spring Boot 为 `executable jar` 提供的进入点，位置在 `org.springframework.boot.loader.JarLauncher`. 这个进入点是在 `MANIFEST.MF` 中定义的：

```
Main-Class: org.springframework.boot.loader.JarLauncher
```

当运行 `java -jar` 的时候，启动的是这个进入点。

### 类加载器

开发调试的时候 IDE 使用默认类加载器打开程序；

运行 `executable jar` 的时候，Spring Boot 会创建一个特殊的类加载器：`org.springframework.boot.loader.LaunchedURLClassLoader`. 这个类加载器在两个位置加载类：`BOOT-INF/classes/`, `BOOT-INF/lib/*`, 开发者自定义的类和依赖项就保存在这两个位置。

>> 这种类加载规则是 Spring Boot 独有的。有时候开发者需要使用其他的类加载器，如果没有严格遵守类加载器 “双亲委派模型”，有可能造成类加载错误。
>> 这时候可以选择其他的打包技术，比如 `fatjar`.

Spring Boot 把程序打包成 `executable jar` 有很多好处，除了刚才说到的开发调试方便之外，还有其他优点：

- 打包产物是一个自包含依赖项的 `jar` 文件，部署方便；
- 运行的时候不需要处理 `-classpath` 参数，或者编写复杂的启动脚本，用 `java  -jar` 就可以启动；
- 包含一个内嵌的 `Tomcat`, 不需要单独部署一个 `Tomcat`, 不会发生依赖冲突；
- 所有的部署产物在一个文件里，不用展开运行，也没有办法改动内部的任何代码和配置，避免了“配置漂移”，这是一个“不可变服务”.

## 其他打包技术

### Gradle

//TODO

### Ant

//TODO
