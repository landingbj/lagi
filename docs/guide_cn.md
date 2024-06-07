# 模型代码调用指南

## 概述
- **介绍**：这份Lag[i]的功能调用指南旨在为您提供清晰、详细的指导，帮助您理解并使用项目中提供的各种 AI 功能。通过这份指南，您可以轻松地将文本对话、语音识别、文字转语音、图片生成等 AI 功能集成到您的应用程序中，实现更智能、更人性化的交互体验。
- **背景**：随着人工智能技术的飞速发展，越来越多的应用场景需要与 AI 模型进行交互，例如智能客服、语音助手、图像处理等。为了满足这些需求，本项目提供了多种 AI 功能，旨在帮助您轻松地将 AI 技术应用于您的业务场景，提升用户体验和效率。

## 开始之前
- **配置要求**：您可以选择使用maven命令行工具进行封包，或者通过IntelliJ IDEA等主流的集成开发环境（IDE）进行运行。请确保您的JDK版本至少满足8的要求。    
- **引入依赖**：调用相关功能需引入依赖，可以通过 Maven引入或直接导入jar的方式。   
  ***Maven引入***：
  - 在项目的pom.xml的dependencies中加入以下内容:
    ```xml
        <depxmlendency>
          <groupId>com.landingbj</groupId>
          <artifactId>lagi-core</artifactId>
          <version>1.0.0</version>
        </depxmlendency>
    ```

  ***直接导入jar***：
  - 直接导入lagi-core-1.0.0.jar或导入以下jar，将它们放入到lagi-core的libs目录下即可：
    ```text
        ai_bp.jar;
        ai_core.jar;
        ai_gather.jar;
        ai_index.jar;
        ai_qa.jar;
    ```
 
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
    Text2VoiceEntity text2VoiceEntity;
    AudioService audioService = new AudioService();
    TTSRequestParam ttsRequestParam = new TTSRequestParam();
    ttsRequestParam.setText(text2VoiceEntity.getText());
    ttsRequestParam.setEmotion(text2VoiceEntity.getEmotion());
    TSResult result = audioService.tts(ttsRequestParam);
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
import ai.migrate.service.VectorDbService;
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
import ai.common.pojo.VideoGenerationResult;
import java.io.File;

public void Test() {
    String lastVideoFile;
    VideoService videoService = new VideoService();
    File file = new File(lastVideoFile);
    VideoGenerationResult track = videoService.track(file.getAbsolutePath());
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
import java.io.File;

public void Test() {
    String imageUrl;
    AllImageService allImageService = new AllImageService();
    ImageEnhanceResult enhance = allImageService.enhance(imageUrl);
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
import ai.common.pojo.VideoGenerationResult;

public void Test() {
    String imageUrl;
    AllVideoService allVideoService = new  AllVideoService();
    VideoGenerationResult videoGenerationResult = allVideoService.image2Video(imageUrl);
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
import ai.common.pojo.VideoGenerationResult;

public void Test() {
    String videoUrl;
    AllVideoService allVideoService = new  AllVideoService();
    VideoGenerationResult videoGenerationResult = allVideoService.enhance(videoUrl);
}
```