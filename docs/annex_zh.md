# 附件
## Chroma安装

如果您想使用检索增强rag功能，一般需要搭建向量数据库，下面就以Chroma来示范介绍具体步骤：

### 方式一：Python

首先要确保有安装有Python运行环境

安装Chroma

```bash
pip install chromadb
```
创建数据库存储目录

```bash
mkdir db_data
```
启动数据库服务

```bash
# --path参数可以指定数据持久化路径
# 默认开启8000端口
chroma run --path db_data
```

### 方式二：Docker
首先要确保有安装有Docker运行环境

运行以下命令，拉取安装并启动Chroma。

```bash
# 启动Chroma数据库容器
#
# 该命令用于在Docker中启动一个名为Chroma的数据库服务容器。它通过一系列参数配置容器的运行环境和对外服务。
# 参数解释：
# -d: 后台运行容器并返回容器ID。
# --name: 为容器指定一个名称，便于后续管理和识别。
# -p: 容器内部端口与主机端口的映射，这里将容器的8000端口映射到主机的8000端口。
# -v: 容器内部目录与主机目录的绑定，实现数据的持久化存储。这里将主机的/mydata/docker/local/chroma/data目录绑定到容器的/study/ai/chroma目录。
# -e: 设置环境变量，用于配置容器内部的应用程序行为。这里设置了两个环境变量：
#     IS_PERSISTENT=TRUE 表示数据库数据将被持久化存储。
#     ANONYMIZED_TELEMETRY=TRUE 表示允许匿名遥测数据的收集。
# chromadb/chroma:latest: 指定使用的镜像为chromadb/chroma的最新版本。
docker run -d \
    --name chromadb \
    -p 8000:8000 \
    -v /mydata/docker/local/chroma/data:/study/ai/chroma \
    -e IS_PERSISTENT=TRUE \
    -e ANONYMIZED_TELEMETRY=TRUE \
    chromadb/chroma:latest
```

### 修改配置

为了在LinkMind中使用，修改lagi.yml配置文件中stores>vectors>url字段的值

 ```yaml
 vector:
   # Chroma is an AI-native open-source embedding database
   # The official website is https://www.trychroma.com/
   - name: chroma
     driver: ai.vector.impl.ChromaVectorStore
     default_category: default
     similarity_top_k: 10
     similarity_cutoff: 0.5
     parent_depth: 1
     child_depth: 1
     url: http://localhost:8000
 ```

## 快速集成

### 方式一：直接导入jar包

您可以直接通过import JAR包的方式使用LinkMind(联智),将一个传统的业务转换为大模型的业务。

1. **下载JAR包**：可直接下载LinkMind(联智) JAR包（[点击下载](https://downloads.saasai.top/lagi/lagi-core-1.0.2-jar-with-dependencies.jar)）。
  
2. **导入JAR包**：将下载的JAR包复制到你的项目的lib目录中。
  
3. **构建和运行项目**：在您的项目中，构建和运行项目。

通过这种方式，您可以将LinkMind(联智) 工程以JAR包直接集成到你的项目中。

**注意**：

   - 该jar内置默认配置文件lagi.yml，以外部配置优先解析。
   - 如您需要修改配置文件，您只需下载配置[lagi.yml](https://github.com/landingbj/lagi/blob/main/lagi-web/src/main/resources/lagi.yml)，将其放入您工程的resources目录下，即可开始启动构建项目了。
   - lagi.yml详细配置可以参考[配置文档](config_zh.md)。

### 方式二：在Eclipse和IntelliJ中

#### Eclipse中集成项目

1.  **导入项目**：
    - 打开Eclipse。
    - 选择 File > Import...。
    - 在 General > Existing Projects into Workspace 中选择LinkMind(联智) 项目文件夹。
    - 勾选你想要导入的项目，然后点击 Finish。

2.  **构建路径和依赖**：
    - 在 Project Explorer 中找到你的项目。
    - 右键点击项目，选择 Properties。
    - 在 Java Build Path > Libraries 中，点击 Add External JARs... 并选择LinkMind(联智) 的libs目录下的所有JAR包。

3.  **同步项目**：
    - 在 Project Explorer 中右键点击项目，选择 Build Project。

#### IDEA中集成项目

1.  **导入项目**：
    - 打开IntelliJ IDEA。
    - 选择 File > Open。
    - 选择LinkMind(联智) 项目文件夹。
    - 点击 OK。

2.  **添加依赖**：
    - 在 Project 窗口中找到你的项目。
    - 右键点击项目，选择 Add Dependency...。
    - 选择 Module Dependency 或 Project Dependency，然后选择LinkMind(联智) 的JAR包。

3.  **同步项目**：
    - 在 Project 窗口中右键点击项目，选择 Build Project。

#### **常见问题**

- **Eclipse或IDEA无法识别JAR包**：确保JAR包没有损坏，并且它的路径在Eclipse或IntelliJ的构建路径中是正确的。
- **依赖冲突**：如果你遇到了依赖冲突，可能需要手动调整项目的构建路径或者使用IDE的依赖解析功能来解决冲突。
- **注意**：这些步骤提供了一个大致的框架，具体的集成步骤可能会根据项目的复杂性和IDE的版本有所不同。

### 方式三：Docker镜像

Docker允许你将你的应用打包成一个可部署的容器。为了更便捷的使用LinkMind，我们提供了封装好的Docker镜像可以直接使用。

**1. 拉取LinkMind(联智) docker 镜像**

拉取镜像命令

```text
docker pull yinruoxi666/lagi-web:1.0.0
```

**2. 启动容器镜像**

容器启动命令

```text
docker run -d --name lagi-web -p 8080:8080 yinruoxi666/lagi-web:1.0.0
```

您也可以在自己Docker中引入一个LinkMind(联智)，并将它集成到自己的项目中。以下是一些基本步骤：

**1. 准备LinkMind(联智) 工程**

- 确保 LinkMind(联智) 工程的构建正确无误。
- 使用Maven等构建工具将 LinkMind(联智) 打包成 WAR 文件。

**2. 创建一个Dockerfile文件，并添加以下内容**

```dockerfile
# 使用官方 Tomcat 镜像作为基础镜像
FROM tomcat:8.5.46-jdk8-openjdk

VOLUME /usr/local/tomcat/temp
VOLUME /usr/local/tomcat/lib
ADD ./target/lagi-1.0.0.war /usr/local/tomcat/webapps/

# 暴露 8080 端口
EXPOSE 8080

# 启动命令
CMD ["catalina.sh", "run"]
```
请确保将 lagi-1.0.0.war 替换为实际的 WAR 包名称。

**3. 在项目根目录下运行以下命令来构建Docker镜像**

```shell
docker build -t lagi-image .
```
**4. 使用以下命令来运行容器：**

```shell
docker run -d -p 8080:8080 lagi-image
```
请确保将your-image-name替换为你在步骤2中使用的镜像名称。

**5. 集成到自己项目中**

在你的项目中，创建一个Dockerfile，指定如何构建包含 LinkMind(联智) 镜像的容器。

```dockerfile
FROM lagi-image
# 添加你的项目目录到容器
ADD . /app
# 暴露端口
EXPOSE 8080
# 启动命令
CMD ["catalina.sh", "run"]
```
构建Docker镜像。

```shell
docker build -t your-project-image .
```

运行Docker容器。

```dockerfile
docker run -d -p 8080:8080 your-project-image
```
