# Integration Development Guide

## Overview

- **Introduction**: This guide for LinkMind provides clear and detailed instructions to help you understand and use various AI functions offered in the project. With this guide, you can easily integrate AI features such as text conversations, speech recognition, text-to-speech, and image generation into your applications for smarter and more human-like interactions.
- **Background**: With the rapid advancement of artificial intelligence technology, more and more application scenarios require interaction with AI models, such as intelligent customer service, voice assistants, and image processing. To meet these needs, this project provides various AI functions aimed at helping you seamlessly apply AI technology to your business scenarios, enhancing user experience and efficiency.

## Before You Start

You can either directly import the jar file or use Maven to add dependencies and run the project in mainstream integrated development environments (IDEs) such as IntelliJ IDEA.

**Configuration Requirements**: Ensure that your JDK version is at least 8.

### 1. Direct Import of Jar File

If you choose to import the jar file directly, you only need to complete the following two steps to start using the methods.

- 1. Download the jar: Download the LinkMind jar file required for the relevant functions ([Click to download](https://downloads.saasai.top/lagi/lagi-core-1.0.2-jar-with-dependencies.jar)).

- 1. Import the jar: Place the jar file in your `lib` directory to use the relevant functions.

  Note: The jar file includes a default configuration file `lagi.yml`. External configuration files take precedence. If you need to modify the configuration file, you can directly download [lagi.yml](https://github.com/landingbj/lagi/blob/main/lagi-web/src/main/resources/lagi.yml) and place it in the `resources` directory of your project. (Refer to the [Configuration Document](config_en.md) for details about `lagi.yml`).

### 2. Maven Dependency Import

If you choose Maven to import dependencies, you only need to complete the following three steps to start using the methods.

- 1. Import the jar: Download and import the LinkMind jar file and place it in the `lib` directory.

- 1. Add the dependency: Add the following content to the `dependencies` section in your project's `pom.xml` file:

  ```xml
  <dependency>
      <groupId>com.landingbj</groupId>
      <artifactId>lagi-core-1.0.0</artifactId>
      <version>1.0</version>
      <scope>system</scope>
      <systemPath>${pom.basedir}/lib/lagi-core-1.0.0.jar</systemPath>
  </dependency>
  ```

  Note: The jar file includes a default configuration file `lagi.yml`. External configuration files take precedence. If you need to modify the configuration file, you can directly download [lagi.yml](https://github.com/landingbj/lagi/blob/main/lagi-web/src/main/resources/lagi.yml) and place it in the `resources` directory of your project. (Refer to the [Configuration Document](config_en.md) for details about `lagi.yml`).

## Example Calls

To help you get started quickly, we provide some [example code](https://github.com/landingbj/lagi/blob/main/lagi-core/src/test/java/ai/example/Demo.java), which you can modify and debug as needed.

### Text Conversation Feature

To use the text conversation feature, first, create an instance of `CompletionsService`.

**completions Method**: Fetch the AI model's response to a conversation in one go.

```java
ChatCompletionResult completions(ChatCompletionRequest chatCompletionRequest);
```

Method Parameters

| Name                  | Type                  | Description                                                  |
| --------------------- | --------------------- | ------------------------------------------------------------ |
| chatCompletionRequest | ChatCompletionRequest | Conversation request parameters including the model, context, and other model settings |

Method Return

| Name                 | Type                 | Description                                                  |
| -------------------- | -------------------- | ------------------------------------------------------------ |
| chatCompletionResult | ChatCompletionResult | Object containing the AI model results. The `choices` attribute contains the results |

**Example Call**:

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

**streamCompletions Method**: Return the AI model's conversation results as a stream.

```java
Observable<ChatCompletionResult> streamCompletions(ChatCompletionRequest chatCompletionRequest)
```

Method Parameters

| Name                  | Type                  | Description                                                  |
| --------------------- | --------------------- | ------------------------------------------------------------ |
| chatCompletionRequest | ChatCompletionRequest | Conversation request parameters including the model, context, and other model settings |

Method Return

| Name       | Type                             | Description                                                  |
| ---------- | -------------------------------- | ------------------------------------------------------------ |
| observable | Observable<ChatCompletionResult> | Stream observer object for obtaining results, which can be written to the `HttpServletResponse` output stream |

**Example Call**:

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

### Speech Recognition Feature

To use the speech recognition feature, create an instance of `AudioService` and call the `asr` method.

**asr Method**: Obtain speech recognition results.

```java
AsrResult asr(String audioFilePath, AudioRequestParam audioRequestParam);
```

Method Parameters

| Name              | Type              | Description                           |
| ----------------- | ----------------- | ------------------------------------- |
| audioRequestParam | AudioRequestParam | Speech recognition request parameters |
| audioFilePath     | String            | Path to the audio file                |

Method Return

| Name      | Type      | Description                              |
| --------- | --------- | ---------------------------------------- |
| asrResult | AsrResult | Collection of speech recognition results |

**Example Call**:

```java
import ai.audio.service.AudioService;
import ai.common.pojo.AsrResult;
import ai.common.pojo.AudioRequestParam;

public void Test() {
    AudioRequestParam param;
    String audio;
    AudioService audioService = new AudioService();
    AsrResult result = audioService.asr(audio, param);
}
```

### Text-to-Speech Feature

To use the text-to-speech feature, first, create an instance of `AudioService` and call the `tts` method.

**tts Method**: Obtain an audio file converted from text.

```java
TTSResult tts(TTSRequestParam param);
```

Method Parameters

| Name  | Type            | Description                                                  |
| ----- | --------------- | ------------------------------------------------------------ |
| param | TTSRequestParam | Includes the content to be converted and user model configurations, such as token and text to be converted |

Method Return

| Name   | Type      | Description                                                  |
| ------ | --------- | ------------------------------------------------------------ |
| result | TTSResult | A `TTSResult` object containing the result of the text-to-speech conversion |

**Example Call**:

```java
import ai.audio.service.AudioService;
import ai.common.pojo.TTSResult;
import ai.common.pojo.TTSRequestParam;

public void Test() {
    String text = "Hello";
    TTSRequestParam request = new TTSRequestParam();
    request.setText(text);
    AudioService audioService = new AudioService();
    TTSResult result = audioService.tts(request);
}
```

### Image Generation Feature

To use the image generation feature, first, create an instance of `ImageGenerationService` and call the `generations` method.

**generations Method**: Obtain the generated image.

```java
ImageGenerationResult generations(ImageGenerationRequest request);
```

Method Parameters

| Name    | Type                   | Description                                    |
| ------- | ---------------------- | ---------------------------------------------- |
| request | ImageGenerationRequest | Request collection containing text information |

Method Return

| Name   | Type                  | Description                              |
| ------ | --------------------- | ---------------------------------------- |
| result | ImageGenerationResult | Contains the path to the generated image |

**Example Call**:

```java
import ai.image.service.ImageGenerationService;
import ai.common.pojo.ImageGenerationResult;

public void Test() {
    ImageGenerationRequest request;
    ImageGenerationService service = new ImageGenerationService();
    ImageGenerationResult result = service.generations(request);
}
```

### Upload Personalized Training Files

To use this feature, first, create an instance of `VectorDbService` and call the `addFileVectors` method.

**addFileVectors Method**: Upload personalized training files.

```java
void addFileVectors(File file, Map<String, Object> metadatas, String category) throws IOException;
```

Method Parameters

| Name      | Type                | Description                              |
| --------- | ------------------- | ---------------------------------------- |
| file      | File                | Path to the file to be uploaded          |
| metadatas | Map<String, Object> | Metadata including file name, type, etc. |
| category  | String              | File type                                |

**Example Call**:

```java
import ai.vector.VectorDbService;
import ai.common.pojo.FileRequest;

public void Test() {
    String fileId = UUID.randomUUID().toString().replace("-", "");
    String filepath = file.getName();
    Map<String, Object> metadatas = new HashMap<>();
    metadatas.put("filename", filepath);
    metadatas.put("category", category);
    metadatas.put("filepath", filepath);
    metadatas.put("file_id", fileId);

    try {
        VectorDbService vectorDbService = new VectorDbService();
        vectorDbService.addFileVectors(file, metadatas, category);
    } catch (IOException | SQLException e) {
        e.printStackTrace();
    }
}
```

### Image Description Feature

To use the image description feature, create an instance of `AllImageService` and call the `toText` method.

**toText Method**: Obtain the generated text description of an image.

```java
ImageToTextResponse toText(FileRequest param);
```

Method Parameters

| Name  | Type        | Description                         |
| ----- | ----------- | ----------------------------------- |
| param | FileRequest | Contains the path to the image file |

Method Return

| Name   | Type                | Description                             |
| ------ | ------------------- | --------------------------------------- |
| result | ImageToTextResponse | Contains the generated text description |

**Example Call**:

```java
import ai.image.service.AllImageService;
import ai.common.pojo.ImageToTextResponse;
import ai.common.pojo.FileRequest;

public void Test() {
    String imagePath;
    AllImageService imageService = new AllImageService();
    File file = new File(imagePath);
    ImageToTextResponse text = imageService.toText(FileRequest.builder().imageUrl(file.getAbsolutePath()).build());
}
```

### Video Tracking Feature

To use the video tracking feature, first, create an instance of `VideoService` and call the `track` method.

**track Method**: Obtain the generated video tracking results.

```java
VideoGenerationResult track(String videoUrl);
```

Method Parameters

| Name     | Type   | Description               |
| -------- | ------ | ------------------------- |
| videoUrl | String | URL of the video to track |

Method Return

| Name   | Type                  | Description                                |
| ------ | --------------------- | ------------------------------------------ |
| result | VideoGenerationResult | Contains the results of the video tracking |

**Example Call**:

```java
import ai.video.service.AllVideoService;
import ai.video.pojo.VideoJobResponse;

public void Test() {
    String lastVideoFile;
    AllVideoService videoService = new AllVideoService();
    VideoTackRequest videoTackRequest = VideoTackRequest.builder().videoUrl(lastVideoFile).build();
    VideoJobResponse track = videoService.track(videoTackRequest);
}
```

### Image Enhancement Feature

To use the image enhancement feature, first, create an instance of `AllImageService` and call the `enhance` method.

**enhance Method**: Obtain the results of the image enhancement process.

```java
ImageEnhanceResult enhance(String imageUrl);
```

Method Parameters

| Name     | Type   | Description                 |
| -------- | ------ | --------------------------- |
| imageUrl | String | URL of the image to enhance |

Method Return

| Name   | Type               | Description                             |
| ------ | ------------------ | --------------------------------------- |
| result | ImageEnhanceResult | Contains the results of the enhancement |

**Example Call**:

```java
import ai.image.service.AllImageService;
import ai.common.pojo.ImageEnhanceResult;
import ai.image.pojo.ImageEnhanceRequest;

public void Test() {
    String imageUrl;
    AllImageService allImageService = new AllImageService();
    ImageEnhanceRequest imageEnhanceRequest = ImageEnhanceRequest.builder().imageUrl(imageUrl).build();
    ImageEnhanceResult enhance = allImageService.enhance(imageEnhanceRequest);
}
```

### Image-to-Video Feature

To use the image-to-video feature, first, create an instance of `AllVideoService` and call the `image2Video` method.

**image2Video Method**: Obtain the generated video based on an image.

```java
VideoGenerationResult image2Video(String imageUrl);
```

Method Parameters

| Name     | Type   | Description                          |
| -------- | ------ | ------------------------------------ |
| imageUrl | String | URL of the image to generate a video |

Method Return

| Name   | Type                  | Description                                 |
| ------ | --------------------- | ------------------------------------------- |
| result | VideoGenerationResult | Contains the results of the generated video |

**Example Call**:

```java
import ai.video.service.AllVideoService;
import ai.video.pojo.VideoJobResponse;

public void Test() {
    String imageUrl;
    AllVideoService allVideoService = new AllVideoService();
    VideoGeneratorRequest videoGeneratorRequest = VideoGeneratorRequest.builder()
            .inputFileList(Collections.singletonList(InputFile.builder().url(imageUrl).build()))
            .build();
    VideoJobResponse videoGenerationResult = allVideoService.image2Video(videoGeneratorRequest);
}
```

### Video Enhancement Feature

To use the video enhancement feature, first, create an instance of `AllVideoService` and call the `enhance` method.

**enhance Method**: Obtain the results of the video enhancement process.

```java
VideoGenerationResult enhance(String videoUrl);
```

Method Parameters

| Name     | Type   | Description                 |
| -------- | ------ | --------------------------- |
| videoUrl | String | URL of the video to enhance |

Method Return

| Name   | Type                  | Description                             |
| ------ | --------------------- | --------------------------------------- |
| result | VideoGenerationResult | Contains the results of the enhancement |

**Example Call**:

```java
import ai.video.service.AllVideoService;
import ai.video.pojo.VideoJobResponse;

public void Test() {
    String videoUrl;
    AllVideoService allVideoService = new AllVideoService();
    VideoEnhanceRequest videoEnhanceRequest = new VideoEnhanceRequest();
    videoEnhanceRequest.setVideoURL(videoUrl);
    VideoJobResponse videoGenerationResult = allImageService.enhance(videoEnhanceRequest);
}
```

