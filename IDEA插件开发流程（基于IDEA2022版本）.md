# IDEA 插件开发流程

## 开发环境介绍

- 软件版本：JetBrain IDEA 2023.2.5
- JDK 版本：JDK 17

​	 需要注意的是，从IDEA2022.2开始，就要求用户必须要有Java17及以上的版本才可以进行插件的开发。如果你不清楚自己的idea做插件开发所要求的最低jdk版本，可以参考官网文档的介绍：[IDEA版本和JDK版本的对应关系](https://plugins.jetbrains.com/docs/intellij/build-number-ranges.html?from=jetbrains.org#intellij-platform-based-products-of-recent-ide-versions)，需要注意的是，如果只是做普通开发的话，那么并没有对idea的版本要求，按实际项目来就行。



## （一）创建一个 IDEA 插件项目

插件开发的方式有两种，一种是直接通过idea自带的插件项目模板来构建我们的插件项目，另外一种是自己搭建一个`gradle`项目，自己配置相关的插件项目构建脚本。我们这里就选择用IDEA自带的模板即可。



### 步骤一：新建一个插件项目

根据`File->New->New Project`菜单路径，打开新建项目的窗口，根据下图配置好项目的存放路径以及对应的jdk，这里我们选择用jdk17。

![image-20231113094506010](https://typora-1313423481.cos.ap-guangzhou.myqcloud.com/typora%2F202311130945074.png)



### 步骤二：打开我们的插件项目

我们可以看到，项目刚创建出来的时候，并没有我们熟悉的`pom.xml`文件，这是因为idea已经默认采用`gradle`来作为项目的构建工具了，如果想要切换成maven来构建项目的话，按官网的说法也是可以的，但这里暂且不说。笔者大概看了一下，如果不涉及比较复杂的开发的话，项目中自带的构建脚本还是可以看得懂的。

![image-20231113100417966](https://typora-1313423481.cos.ap-guangzhou.myqcloud.com/typora%2F202311131004030.png)



`plugin.xml`**文件介绍**

这个文件可以理解为我们插件的元数据文件，用于定义我们的插件名、开发人员、插件依赖以及插件包含的内容等信息，具体可以看下面的介绍。

```xml
<!-- Plugin Configuration File. Read more: https://plugins.jetbrains.com/docs/intellij/plugin-configuration-file.html -->
<idea-plugin>
    <!-- 插件id，不可重复，必须唯一。插件的升级后续也是依赖插件id来进行识别的。 -->
    <id>com.zipeng.IDE_Plugin_Demo</id>

    <!-- 插件名称 -->
    <name>IDE_Plugin_Demo</name>

    <!-- 插件开发人员，这里写一下开发者的个人信息。 -->
    <vendor email="support@yourcompany.com" url="https://www.yourcompany.com">YourCompany</vendor>

    <!-- 插件描述，这里一般写插件的1功能介绍啥的 -->
    <description><![CDATA[
    Enter short description for your plugin here.<br>
    <em>most HTML tags may be used</em>
  ]]></description>

    <!-- 插件依赖，这里我们默认引用idea自带的依赖即可 -->
    <depends>com.intellij.modules.platform</depends>

    <!-- 定义扩展点，比较少用到，一般是用于你去扩展其他人插件功能扩展点，或者是你的插件扩展了 IntelliJ 平台核心功能才会配置到这里 -->
    <extensions defaultExtensionNs="com.intellij">

    </extensions>
</idea-plugin>
```



`build.gradle.kts`**文件介绍**

这个文件定义了IDEA插件构建时依赖的环境，以及最终支持在哪些环境下面运行插件。这个文件是相当重要的，一般来说我们这里会根据实际情况来对这个文件进行修改，不会直接用默认的配置。

```java
// 项目依赖的插件，默认会依赖kotlin，但我们这里是直接用java来开发插件的，所以这里依赖我们可以去掉
plugins {
    id("java")
//    id("org.jetbrains.kotlin.jvm") version "1.8.21"
    id("org.jetbrains.intellij") version "1.5.2"
}

// 插件的一些基本信息，按实际情况填就行，不是很重要
group = "com.zipeng"
version = "1.0-SNAPSHOT"

// 插件等依赖的下载地址，默认会去中央仓库下载，这里我们一般是会改为直接去idea官网下载或者是用其他镜像
repositories {
    mavenCentral()
}

// 这里是很重要的配置，定义了gradle构建时依赖的idea版本，我们进行插件调试的时候，会使用这里定义的idea版本来进行测试的。
intellij {
    version.set("2022.2")
    type.set("IC") // Target IDE Platform

    plugins.set(listOf(/* Plugin Dependencies */))
}

// 定义构建的任务，主要是改一下编译的jdk版本，插件适用的idea版本等信息
tasks {
    // Set the JVM compatibility versions
    withType<JavaCompile> {
        sourceCompatibility = "11"
        targetCompatibility = "11"
    }

    patchPluginXml {
        sinceBuild.set("222")
        untilBuild.set("222.*")
    }

    signPlugin {
        certificateChain.set(System.getenv("CERTIFICATE_CHAIN"))
        privateKey.set(System.getenv("PRIVATE_KEY"))
        password.set(System.getenv("PRIVATE_KEY_PASSWORD"))
    }

    publishPlugin {
        token.set(System.getenv("PUBLISH_TOKEN"))
    }
}
```



如何查看当前使用的 IDEA 的版本号。

<img src="https://typora-1313423481.cos.ap-guangzhou.myqcloud.com/typora%2F202311131427659.png" alt="image-20231113142749541" style="zoom:67%;" />

<img src="https://typora-1313423481.cos.ap-guangzhou.myqcloud.com/typora%2F202311131027644.png" alt="image-20231113102700615" style="zoom:67%;" />



这里有2个地方需要额外注意一下：

（1）gradle在构建项目的时候会根据我们定义的idea版本去下载对应的idea安装包，有一说一，安装包比较大，大概有600多M，而且下载速度相对比较慢，最好做一下心里准备。如果想要换新的IDEA版本调试的话，那么也需要重新下载新的安装包。

（2）在比较高的版本中，idea默认会使用`kotlin`语法来解析`build.gradle.kts`和`settings.gradle.kts`这两个文件。不过笔者对`kotlin`不熟悉，所以我们会把配置切换为`gradle`语法，降低一下学习成本。详见步骤三。



### 步骤三：调整部署文件

#### （1）去掉`build.gradle.kts`的kts后缀，稍微改一下原有文件的内容。调整后的文件内容如下。

```java
plugins {
    id("java")
    id("org.jetbrains.intellij") version "1.13.3"
}

group = "com.zipeng"
version = "1.0-SNAPSHOT"

repositories {
    maven { url 'https://maven.aliyun.com/repository/central/'}
    maven { url 'https://maven.aliyun.com/repository/public/' }
    maven { url 'https://maven.aliyun.com/repository/google/' }
    maven { url 'https://maven.aliyun.com/repository/jcenter/'}
    maven { url 'https://maven.aliyun.com/repository/gradle-plugin'}
    //    mavenCentral()
}

dependencies {
    testImplementation 'org.junit.jupiter:junit-jupiter-api:5.9.3'
    testRuntimeOnly    'org.junit.jupiter:junit-jupiter-engine:5.9.3'
    testRuntimeOnly    'org.junit.vintage:junit-vintage-engine:5.9.3'
    testImplementation 'org.junit.jupiter:junit-jupiter-params:5.9.3'
}

intellij {
    version.set("2022.2")
}

tasks.withType(JavaCompile) {
    options.encoding = 'UTF-8'
    options.compilerArgs += ['-Xlint:unchecked', '-Xlint:deprecation', '-parameters']
}

patchPluginXml {
    //注意这个版本号不能高于上面intellij的version,否则runIde会报错
    sinceBuild = '222'
    untilBuild = '232.*'
}
```



#### （2）去掉`settings.gradle.kts`文件的后缀

文件里面的内容不需要调整



#### （3）删除src目录下的`kotlin`目录，新建`java`目录

最终得到的项目结构如下：

<img src="https://typora-1313423481.cos.ap-guangzhou.myqcloud.com/typora%2F202311131038554.png" alt="image-20231113103837498" style="zoom:67%;" />



### 步骤四：创建我们的插件

#### （1）了解IDEA支持的插件类型

简单了解的话，大概可以分成是开发语言类插件（用于支持自己开发的语言，大佬专用）、框架插件（比如挺多人在用的`mybatis插件`）、第三方工具插件（比如翻译插件）、UI交互类插件以及UI插件（纯UI美化）。

我们可以通过访问idea官网地址来查看idea支持的插件类型：[点此查看](https://plugins.jetbrains.com/docs/intellij/plugin-types.html)

![image-20231113145250965](https://typora-1313423481.cos.ap-guangzhou.myqcloud.com/typora%2F202311131452003.png)



#### （2）我们这里选择做一个简单的UI交互插件

想要实现的效果为，在`Help栏`中新增一个名为`showProjectName`的按钮，点击后展示当前项目的项目名称。



**新建一个插件Action类**

![image-20231113103938609](https://typora-1313423481.cos.ap-guangzhou.myqcloud.com/typora%2F202311131039687.png)



根据实际情况填写一下action的详细信息，这里的name是插件的名称。

![image-20231113145914546](https://typora-1313423481.cos.ap-guangzhou.myqcloud.com/typora%2F202311131459596.png)



一般来说，加完之后`plugin.xml`上面也会同步生成action的信息。

![image-20231113104447332](https://typora-1313423481.cos.ap-guangzhou.myqcloud.com/typora%2F202311131044410.png)



写一下`Action`的具体代码，代码很简单，只是做一个dialog的信息展示而已。

![image-20231113112451782](https://typora-1313423481.cos.ap-guangzhou.myqcloud.com/typora%2F202311131124814.png)



至此，插件的开发代码就完成了。至于插件的测试、构建和使用可以看下面的章节。



## （二）插件的测试

插件代码写完后，我们可以从两个地方进行插件的调试。



入口一：菜单栏`build`工具栏

如果是debug模式启动，我们可以正常使用我们的断点功能。

![image-20231113150922302](https://typora-1313423481.cos.ap-guangzhou.myqcloud.com/typora%2F202311131509384.png)



入口二：主窗口左/右侧的gradle菜单栏

选择`task->intellij`目录，里面有很多可执行的命令，我们选择使用runidea来启动项目

<img src="https://typora-1313423481.cos.ap-guangzhou.myqcloud.com/typora%2F202311131046726.png" alt="image-20231113104626675" style="zoom:67%;" />



首次启动项目，会根据我们之前在`build.gradle`文件中配置idea版本去网上下载对应的安装包，可能耗时会有点九。下载成功后，会打开一个新的idea，打开后我们随便选择某个项目或者新建一个项目就行。

![image-20231113151140429](https://typora-1313423481.cos.ap-guangzhou.myqcloud.com/typora%2F202311131511492.png)



我们可以在打开的项目中，点击`Help`菜单，就可以看到我们自己定义的`UI插件`了

![image-20231113111618406](https://typora-1313423481.cos.ap-guangzhou.myqcloud.com/typora%2F202311131116450.png)



点击后正常弹出了内容为项目名的弹框。

![image-20231113111644104](https://typora-1313423481.cos.ap-guangzhou.myqcloud.com/typora%2F202311131116163.png)



## （三）插件的构建

当自己的插件测试好了之后，希望打包出来，可以通过`buildPlugin`来打包我们的插件。

![image-20231113111740645](https://typora-1313423481.cos.ap-guangzhou.myqcloud.com/typora%2F202311131117678.png)



构建好后我们可以在`build/lib`目录下找到我们的jar包，拿到后后续可以在idea上面进行离线安装。

![image-20231113111957723](https://typora-1313423481.cos.ap-guangzhou.myqcloud.com/typora%2F202311131119760.png)



#### 离线安装插件步骤

选择`Settings->Plugin->Install Plugin from Disk`目录。

![image-20231113112341423](https://typora-1313423481.cos.ap-guangzhou.myqcloud.com/typora%2F202311131123472.png)



选择插件的路径。

![image-20231113112357292](https://typora-1313423481.cos.ap-guangzhou.myqcloud.com/typora%2F202311131123357.png)



安装成功。

![image-20231113112328942](https://typora-1313423481.cos.ap-guangzhou.myqcloud.com/typora%2F202311131123000.png)



## （四）插件校验

因为IDEA版本众多，如果你的插件希望可以被多个Idea版本兼容的话，那么在你发布到你的团队或者发布到idea插件市场上时，建议先走一次校验流程。这个检验流程会把所有版本的Idea自动走一次你的插件（这里应该不是走全流程，只是校验是否能正常编译运行而已）。当然了，由于这个版本会检验idea版本的兼容性，所以这里的耗时相对来说会比较长，因为要下载各个版本的idea去测试。

<img src="https://typora-1313423481.cos.ap-guangzhou.myqcloud.com/typora%2F202311131525409.png" alt="image-20231113152521344" style="zoom:67%;" />



## 小结

至此，IDEA插件的开发就到此结束了。入门的插件开发并不难，有兴趣的话可以从官网或者其他开源的IDEA插件中翻翻代码，会有更多的收获。

本篇文章的代码已上传至Git：https://github.com/LiangZipeng-1998/IDE_Plugin_Demo



## 参考文章：

https://www.jianshu.com/p/3b8b65ca73c3
