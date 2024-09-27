# 模型代码调用指南

## 概述
- **介绍**：这份Lag[i] (联基) 的功能调用指南旨在为您提供清晰、详细的指导，帮助您理解并使用项目中提供的各种 AI 功能。通过这份指南，您可以轻松地将文本对话、语音识别、文字转语音、图片生成等 AI 功能集成到您的应用程序中，实现更智能、更人性化的交互体验。
- **背景**：随着人工智能技术的飞速发展，越来越多的应用场景需要与 AI 模型进行交互，例如智能客服、语音助手、图像处理等。为了满足这些需求，本项目提供了多种 AI 功能，旨在帮助您轻松地将 AI 技术应用于您的业务场景，提升用户体验和效率。

## 开始之前

您可以选择直接导入jar，或使用或使用maven引入依赖，通过IntelliJ IDEA等主流的集成开发环境（IDE）进行运行。

**配置要求**：请确保您的JDK版本至少满足8的要求。

### 一.直接导入jar 

如您选择直接导入jar，您只需完成以下两步即可开始方法调用。

- 1.下载jar：调用相关功能需下载Lag[i] (联基) 的jar包（[点击下载](https://downloads.saasai.top/lagi/lagi-core-1.0.2-jar-with-dependencies.jar)）。

- 2.导入jar：调用相关功能需将jar放入您的lib目录下。

 注意：该jar内置默认配置文件lagi.yml，以外部配置优先解析。如您需要修改配置文件，可以直接下载配置[lagi.yml](https://github.com/landingbj/lagi/blob/main/lagi-web/src/main/resources/lagi.yml)，并将其放入您工程的resources目录下。（lagi.yml相关配置可以参考[配置文档](config_zh.md)）

### 二.maven引入依赖

如您选择maven引入依赖，您只需完成以下三步即可开始方法调用。

- 1.导入jar：调用相关功能需下载并导入Lag[i] (联基) 的jar包，将它放入lib目录下。

- 2.添加dependency：在项目的pom.xml的dependencies中加入以下内容:
    ```xml
        <dependency>
            <groupId>com.landingbj</groupId>
            <artifactId>lagi-core-1.0.0</artifactId>
            <version>1.0</version>
            <scope>system</scope>
            <systemPath>${pom.basedir}/lib/lagi-core-1.0.0.jar</systemPath>
        </dependency>
    ```

注意：该jar内置默认配置文件lagi.yml，以外部配置优先解析。如您需要修改配置文件，可以直接下载配置[lagi.yml](https://github.com/landingbj/lagi/blob/main/lagi-web/src/main/resources/lagi.yml)，并将其放入您工程的resources目录下。（lagi.yml相关配置可以参考[配置文档](config_zh.md)）

## 调用示列
- 为了快速上手，我们提供了一些[示例代码](https://github.com/landingbj/lagi/blob/main/lagi-core/src/test/java/ai/example/Demo.java)，您可以根据需要进行修改和调试。

## 文本对话功能
要使用文本对话功能首先需要创建一个 CompletionsService 的实例对象。 这个对象有两个方法 completions,streamCompletions。

__completions__  

`
ChatCompletionResult completions(ChatCompletionRequest chatCompletionRequest);
`  

一次性获取大模型的对话的回答结果：  

> 请求参数

|名称| 类型                    |说明|
|---|-----------------------|---|
|chatCompletionRequest| ChatCompletionRequest |对话请求参数包含对话使用的模型,对话的上下文及一些模型参数|


> 返回：  

| 名称                  | 类型                    | 说明                                    |
|---------------------|-----------------------|---------------------------------------|
|chatCompletionResult | ChatCompletionResult | 一个包含大模型结果的对象,对象的 choices属性,包含大模型结果的对象 |

> 调用示例：

```java
import ai.llm.service.CompletionsService;
import ai.openai.pojo.ChatCompletionRequest;
import ai.openai.pojo.ChatCompletionResult;

public class Test {
    CompletionsService completionsService = new CompletionsService();
    ChatCompletionResult result = completionsService.completions(chatCompletionRequest);
    String text = result.getChoices().get(0).getMessage().getContent();
}

```

__streamCompletions__

`
Observable<ChatCompletionResult> streamCompletions(ChatCompletionRequest chatCompletionRequest)
`  

使用流的方式返回大模型对话的结果：  

> 参数：  

|名称| 类型                    |说明|
|---|-----------------------|---|
|chatCompletionRequest| ChatCompletionRequest |对话请求参数包含对话使用的模型,对话的上下文及一些模型参数|

> 返回：  

| 名称                  | 类型                                | 说明                                    |
|---|-----------------------------------|---------------------------------------|
|observable | Observable\<ChatCompletionResult> | 一个流的观察者对象， 可以通过这个对象获取流的返回结果, 你可以将写入到 HttpServletResponse 的输出流中  |
 
> 调用示例：
```java

import ai.llm.service.CompletionsService;
import ai.openai.pojo.ChatCompletionRequest;
import ai.openai.pojo.ChatCompletionResult;
import com.google.gson.Gson;
import io.reactivex.rxjava3.core.Observable;

public void Test(HttpServletRequest req, HttpServletResponse resp) throws IOException {
    CompletionsService completionsService = new CompletionsService();
    Observable<ChatCompletionResult> observable = completionsService.streamCompletions(chatCompletionRequest);
    PrintWriter out = resp.getWriter();
    final ChatCompletionResult[] lastResult = {null};
    observable.subscribe(
            data -> {
                lastResult[0] = data;
                String msg = gson.toJson(data);
                out.print("data: " + msg + "\n\n");
                out.flush();
            },
            e -> logger.error("", e),
            () -> extracted(lastResult, indexSearchDataList, req, out)
    );
}

```

## 语音识别功能
要使用语音识别功能，首先需要上传语音，并指定语音文件保存路径savePath。 再创建一个 AudioService 的实例对象，调用asr方法。

__上传语音__  

指定语音文件保存路径savePath
```java
String savePath = "/path/to/save/files/"; // 替换为实际的文件保存路径
```
__asr__ 

`
AsrResult asr(String audioFilePath, AudioRequestParam audioRequestParam);
`  

获取语音识别结果  
>参数：  

|名称| 类型                |说明|
|---|-------------------|---|
|audioRequestParam| AudioRequestParam |语音识别请求参数|
|audioFilePath| String |音频地址|


>返回：   

|名称| 类型                |说明|
|---|-------------------|---|
|asrResult| AsrResult |语音识别结果集|

>调用示例：  
```java
import ai.audio.service.AudioService;
import ai.common.pojo.AsrResult;
import ai.common.pojo.AudioRequestParam;

public void Test() {
    AudioRequestParam param;
    String audio ;
    AudioService audioService = new AudioService();
    AsrResult result = audioService.asr(audio, param);
}

```

## 文字转语音功能
要使用文字转语音功能，首先需要创建一个 AudioService 的实例对象，调用tts方法。

__tts__ 

`
TTSResult tts(TTSRequestParam param);
`

获取音频文件  
>参数：

|名称| 类型 |说明|
|---|-------------------------|---|
|param| TTSRequestParam |请求转换的内容和用户模型的配置信息，包含用户的token，以及要转换的文本。|

>返回：

|名称| 类型 |说明|
|---|-------------------------|---|
|result| TTSResult |TTSResult 对象，包含文本识别结果集。 |

>调用示例：

```java
import ai.audio.service.AudioService;
import ai.common.pojo.TTSResult;
import ai.common.pojo.TTSRequestParam;
import ai.common.pojo.Text2VoiceEntity;

public void Test() {
    //Your text
    String text = "Hello";
    TTSRequestParam request = new TTSRequestParam();
    request.setText(text);
    AudioService audioService = new AudioService();
    TTSResult result = audioService.tts(request);
}
```

## 图片生成功能
要使用图片生成功能，首先需要创建一个 ImageGenerationService 的实例对象，调用对应的generations方法。

__generations__

`
ImageGenerationResult generations(ImageGenerationRequest request);
`

获取生成图片路径地址  
>参数：  

|名称| 类型 |说明|
|---|-------------------------|---|
|request| ImageGenerationRequest |请求结果集，包含请求文本信息。 |

>返回：  

|名称| 类型 |说明|
|---|-------------------------|---|
|result| ImageGenerationResult | ImageGenerationResult 对象，包含图片路径地址信息。 |
> 调用示例：

```java
import ai.image.service.ImageGenerationService;
import ai.common.pojo.ImageGenerationResult;
import ai.common.pojo.ImageGenerationResult;
public void Test() {
    ImageGenerationResult request ;
    ImageGenerationService service = new ImageGenerationService();
    ImageGenerationResult result = service.generations(request);
}
```


## 上传私训学习文件功能

要使用上传私训学习文件功能，首先需要创建一个 VectorDbService 的实例对象，调用对应的addFileVectors方法。

__addFileVectors__

`
void addFileVectors(File file, Map<String, Object> metadatas, String category) throws IOException;
`

上传私训学习文件。  
>参数：

|名称| 类型 |说明|
|---|---|---|
|file| File |请求上传文件路径地址。|
|metadatas| Map<String, Object> |请求结果集，包含请求文件名，文件类型等信息。|
|category| String |文件类型。|
>返回：  

无返回值。   

> 调用示例：

```java
import ai.vector.VectorDbService;
import ai.common.pojo.FileRequest;
import ai.vector.VectorStoreService;

public void Test() {
    String fileId = UUID.randomUUID().toString().replace("-", "");
    String filepath = file.getName();
    Map<String, Object> metadatas = new HashMap<>();
    metadatas.put("filename", filename);
    metadatas.put("category", category);
    metadatas.put("filepath", filepath);
    metadatas.put("file_id", fileId);
    if (level == null) {
        metadatas.put("level", "user");
    } else {
        metadatas.put("level", "system");
    }

    try {
        VectorDbService vectorDbService = new VectorDbService();
        vectorDbService.addFileVectors(this.file, metadatas, category);
    } catch (IOException | SQLException e) {
        e.printStackTrace();
    }
}

```

## 看图说话功能

要使用看图说话功能，首先需要创建一个 AllImageService 的实例对象，调用toText方法。

__toText__

`
ImageToTextResponse toText(FileRequest param);
`

获取生成文本结果集。  
>参数：

|名称| 类型 |说明|
|---|-------------------------|---|
|param| FileRequest |请求结果集，包含请求图片路径地址信息。|

>返回：

|名称| 类型 |说明|
|---|-------------------------|---|
|result| ImageToTextResponse |一个 ImageToTextResponse 对象，包含生成文本结果集。 |
>调用示例：

```java
import ai.image.service.AllImageService;
import ai.common.pojo.ImageToTextResponse;
import ai.common.pojo.FileRequest;
import java.io.File;

public void Test() {
    String lastImageFile;
    AllImageService allImageService = new AllImageService();
    File file = new File(lastImageFile);
    ImageToTextResponse text = allImageService.toText(FileRequest.builder().imageUrl(file.getAbsolutePath()).build());
}
```

## 视频追踪功能

要使用视频追踪功能，首先需要创建一个 VideoService 的实例对象，调用track方法。

__track__

`
VideoGenerationResult track(String videoUrl);
`

获取生成视频结果集。  

>参数：
> 
|名称| 类型 |说明|
|---|-------------------------|---|
|videoUrl| String |请求追踪视频地址。|

>返回：  

|名称| 类型 |说明|
|---|-------------------------|---|
|result| VideoGenerationResult |一个 VideoGenerationResult 对象，包含生成视频结果集。 |

> 调用示例：

```java
import ai.video.service.AllVideoService;
import ai.video.pojo.VideoJobResponse;
import java.io.File;

public void Test() {
    String lastVideoFile;
    AllVideoService videoService = new AllVideoService();
    VideoTackRequest videoTackRequest = VideoTackRequest.builder().videoUrl(lastVideoFile).build();
    VideoJobResponse track = videoService.track(videoTackRequest);
}
```

## 图像增强功能

要使用图像增强功能，首先需要创建一个 AllImageService 的实例对象，调用enhance方法。

__enhance__

`
ImageEnhanceResult enhance(String imageUrl);
`

获取生成图像结果集。  
>参数：

|名称| 类型 |说明|
---|---|---|
|imageUrl| String |请求增强图片地址。|

>返回：  

|名称| 类型 |说明|
---|---|---|
|result| ImageEnhanceResult |ImageEnhanceResult 对象，包含生成图像结果集。 |

> 调用示例：

```java
import ai.image.service.AllImageService;
import ai.common.pojo.ImageEnhanceResult;
import ai.image.pojo.ImageEnhanceRequest;
import java.io.File;

public void Test() {
    String imageUrl;
    AllImageService allImageService = new AllImageService();
    ImageEnhanceRequest imageEnhanceRequest = ImageEnhanceRequest.builder().imageUrl(imageUrl).build();
    ImageEnhanceResult enhance = allImageService.enhance(imageEnhanceRequest);
}

```

## 图生视频

要使用图生视频功能，首先需要创建一个 AllVideoService 的实例对象，调用image2Video方法。

__image2Video__

`
VideoGenerationResult image2Video(String imageUrl);
`

获取生成视频结果集。  
>参数：

|名称| 类型 |说明|
---|---|---|
|imageUrl| String |请求生成视频的图片地址。|

>返回：  

| 名称 | 类型 |说明|
|----|---|---|
|result| VideoGenerationResult |VideoGenerationResult 对象，包含生成视频结果集。 | 
> 调用示例：

```java
import ai.video.service.AllVideoService;
import ai.video.pojo.VideoJobResponse;

public void Test() {
    String imageUrl;
    AllVideoService allVideoService = new  AllVideoService();
    VideoGeneratorRequest videoGeneratorRequest = VideoGeneratorRequest.builder()
            .inputFileList(Collections.singletonList(InputFile.builder().url(imageUrl).build()))
            .build();
    VideoJobResponse videoGenerationResult = allVideoService.image2Video(videoGeneratorRequest);
}


```

## 视频增强

要使用视频增强功能，首先需要创建一个 AllVideoService 的实例对象，调用enhance方法。

__enhance__

`
ideoGenerationResult enhance(String videoUrl);
`

获取生成增强视频结果集。  
>参数：

|名称| 类型 |说明|
---|---|---|
|videoUrl| String |请求生成视频地址。|

>返回：  

|名称| 类型 |说明|
---|---|---|
|result| VideoGenerationResult |一个 VideoGenerationResult 对象，包含生成视频结果集。 |

>调用示例：

```java
import ai.video.service.AllVideoService;
import ai.video.pojo.VideoJobResponse;

public void Test() {
    String videoUrl;
    AllVideoService allVideoService = new  AllVideoService();
    VideoEnhanceRequest videoEnhanceRequest = new VideoEnhanceRequest();
    videoEnhanceRequest.setVideoURL(lastVideoFile);
    VideoJobResponse videoGenerationResult = allVideoService.enhance(videoEnhanceRequest);
}
```

## 快速集成进您的项目
### 方式一: 直接导入jar包

您可以直接通过import JAR包的方式使用lag[i] (联基),将一个传统的业务转换为大模型的业务。

1. **下载JAR包**：
    - 可直接下载lag[i] (联基) JAR包（[点击下载](https://downloads.saasai.top/lagi/lagi-core-1.0.2-jar-with-dependencies.jar)）。

2. **导入JAR包**：
    - 将下载的JAR包复制到你的项目的lib目录中。

3. **构建和运行项目**：
    - 在您的项目中，构建和运行项目。

通过这种方式，您可以将lag[i] (联基) 工程以JAR包直接集成到你的项目中。

**注意**：
   - 该jar内置默认配置文件lagi.yml，以外部配置优先解析。
   - 如您需要修改配置文件，您只需下载配置[lagi.yml](https://github.com/landingbj/lagi/blob/main/lagi-web/src/main/resources/lagi.yml)，将其放入您工程的resources目录下，即可开始启动构建项目了。
   - lagi.yml详细配置可以参考[配置文档](config_zh.md)

### 方式二: 在Eclipse和IntelliJ中
**Eclipse中集成项目**
1. ***导入项目***：
    - 打开Eclipse。
    - 选择 File > Import...。
    - 在 General > Existing Projects into Workspace 中选择lag[i] (联基) 项目文件夹。
    - 勾选你想要导入的项目，然后点击 Finish。

2. ***构建路径和依赖***：
    - 在 Project Explorer 中找到你的项目。
    - 右键点击项目，选择 Properties。
    - 在 Java Build Path > Libraries 中，点击 Add External JARs... 并选择lag[i] (联基) 的libs目录下的所有JAR包。

3. ***同步项目***：
    - 在 Project Explorer 中右键点击项目，选择 Build Project。

**IDEA中集成项目**
1. ***导入项目***：
    - 打开IntelliJ IDEA。
    - 选择 File > Open。
    - 选择lag[i] (联基) 项目文件夹。
    - 点击 OK。

2. ***添加依赖***：
    - 在 Project 窗口中找到你的项目。
    - 右键点击项目，选择 Add Dependency...。
    - 选择 Module Dependency 或 Project Dependency，然后选择lag[i] (联基) 的JAR包。

3. ***同步项目***：
    - 在 Project 窗口中右键点击项目，选择 Build Project。

**常见问题**

- **Eclipse或IDEA无法识别JAR包**：确保JAR包没有损坏，并且它的路径在Eclipse或IntelliJ的构建路径中是正确的。
- **依赖冲突**：如果你遇到了依赖冲突，可能需要手动调整项目的构建路径或者使用IDE的依赖解析功能来解决冲突。
- **注意**：这些步骤提供了一个大致的框架，具体的集成步骤可能会根据项目的复杂性和IDE的版本有所不同。

### 方式三:微服务docker

微服务Docker集成是一种流行的方法，它允许你将你的应用打包成一个可部署的容器。为了更便捷的使用lagi，我们提供了封装好的docker镜像您可以直接使用。

**1.拉取lag[i] (联基) docker 镜像：**

- 拉取命令：
```text
docker pull yinruoxi666/lagi-web:1.0.0
```

**2.启动容器镜像：**

- 容器启动命令
```text
docker run -d --name lagi-web -p 8080:8080 yinruoxi666/lagi-web:1.0.0
```

**您也可以在自己Docker中引入一个lag[i] (联基)，并将它集成到自己的项目中。以下是一些基本步骤：**

**1.准备lag[i] (联基) 工程：**
- 确保 lag[i] (联基) 工程的构建正确无误。
- 使用Maven等构建工具将 lag[i] (联基) 打包成 WAR 文件。

**2.创建一个Dockerfile文件，并添加以下内容：**
```text
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

**3.在项目根目录下运行以下命令来构建Docker镜像：**
```text
     docker build -t lagi-image .
```
**4.使用以下命令来运行容器：**
```text
     docker run -d -p 8080:8080 lagi-image
```
请确保将your-image-name替换为你在步骤2中使用的镜像名称。

**5.集成到自己项目中：**
- 在你的项目中，创建一个Dockerfile，指定如何构建包含 lag[i] (联基) 镜像的容器。
```text
    FROM lagi-image
    # 添加你的项目目录到容器
    ADD . /app
    # 暴露端口
    EXPOSE 8080
    # 启动命令
    CMD ["catalina.sh", "run"]
```
- 构建Docker镜像。
```text
     docker build -t your-project-image .
```

- 运行Docker容器。
```text
     docker run -d -p 8080:8080 your-project-image
```
