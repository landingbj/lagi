# Model code invocation guide

## Overview
- **Introduction**： This Lag[i] feature invocation guide is designed to give you clear, detailed guidance on understanding and using the various AI features provided in your project. With this guide, you can easily integrate AI features such as text conversation, speech recognition, text-to-speech, and image generation into your app for a smarter, more human-friendly interaction experience.
- **Background**： With the rapid development of artificial intelligence technology, more and more application scenarios require interaction with AI models, such as intelligent customer service, voice assistants, image processing, etc. To meet these needs, this project provides a variety of AI features designed to help you easily apply AI technologies to your business scenarios, improving user experience and efficiency.

## Before we begin
- **Configuration requirements**：You can choose to use the maven command-line tool for wrapping, or run it through a mainstream integrated development environment (IDE) like IntelliJ IDEA. Make sure your JDK version meets at least 8.
- **Import dependencies**： To access the functionality, we need to import dependencies, either from Maven or directly from the jar.   
  ***Maven import***：
    - Add the following to dependencies in the project pom.xml:
      ```xml
          <depxmlendency>
            <groupId>com.landingbj</groupId>
            <artifactId>lagi-core</artifactId>
            <version>1.0.0</version>
          </depxmlendency>
      ```

  ***Importing jar***：
    - Either import lagu-core-1.0.0.jar or import the following jars and place them in the libs directory of lagu-core:
      ```text
          ai_bp.jar;
          ai_core.jar;
          ai_gather.jar;
          ai_index.jar;
          ai_qa.jar;
      ```

## Docker Run
- **Dockerfile**：
  ```text
        # Use the official Tomcat image as the base image
        FROM tomcat:8.5.46-jdk8-openjdk
        
        # Set the working directory
        WORKDIR /usr/local/tomcat/webapps
        
        # Copy the project WAR file into the container
        COPY myproject.war .
        
        # expose port 8080
        EXPOSE 8080
        
        # Start command
        CMD ["catalina.sh", "run"]
  ```
- **How to use**：
  - Build images
  >  docker build -t myproject-tomcat:1.0 .
  - Run the container
  >  docker run -d -p 8080:8080 myproject-tomcat:1.0

- **Parameter description**：
    
    > -d: Run the container as a daemon.   

    > -p 8080:8080: Maps port 8080 of the host machine to port 8080 of the container.  

    > myproject-tomcat:1.0: Specifies the image name and label.   
- **Note**：

    Make sure your WAR file is built correctly and located in the current directory.   
    If you have additional configuration or dependencies, you may need to modify the Dockerfile or use Docker Compose for more complex configuration.   

## Calling the examples
- To get started quickly, We provide some [Calling the examples](https://github.com/landingbj/lagi/blob/main/lagi-core/src/test/java/ai/example/Demo.java), you can modify and debug as needed.

## Text conversation feature
To use chat function you need to create an instance object of CompletionsService. This object has two methods completions, streamCompletions.

__completions__

`
ChatCompletionResult completions(ChatCompletionRequest chatCompletionRequest)
`  

Get the answer results of the large model conversation at once   

> Parameters：  

| Name | type | Description |
|---|-----------------------|---|
|chatCompletionRequest| ChatCompletionRequest |Session request parameters include the model used by the session, the context of the session, and some model parameters |
 

>Returns：   

| Name | type | Description |
|---|-----------------------|---|
|ChatCompletionResult| ChatCompletionResult |An object containing the results of the large model, the object's choices property, contains the answer text returned by the large model |

>Example invocation:

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

Returns the results of a large model conversation using a stream   

>Parameters：  

| Name | type | Description |
|---|-----------------------|---|
|chatCompletionRequest| ChatCompletionRequest |Session request parameters include the model used by the session, the context of the session, and some model parameters |

>Returns：   

| Name                              | type | Description |
|-----------------------------------|-----------------------|---|
| Observable\<ChatCompletionResult> | Observable<ChatCompletionResult> |An object containing the results of the large model, the object's choices property, contains the answer text returned by the large model |

>Example invocation:
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

## Speech recognition function
To use the speech recognition function, you first need to upload the speech and specify the voice file saving path savePath. Then create an instance object of AudioService and call the asr method.

__Upload voice__

Specify the savePath to save the voice file
```java
String savePath = "/path/to/save/files/"; // Replace with the actual file save path
```
__asr__

`
AsrResult asr(String audioFilePath, AudioRequestParam audioRequestParam);
`

Get the speech recognition results  
>Parameters: 

| Name | type | Description |
|------|-----------------------|----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
|audioRequestParam| AudioRequestParam |Speech recognition request parameters|
| audioFilePath | String | The path of the audio file to be uploaded. |


>Returns:    

| Name | type | Description |
|------|-----------------------|----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| AsrResult | AsrResult | An object containing the results of the speech recognition, the object's choices property, contains the

>Example invocation:
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

## Text-to-Speech features
To use the text-to-speech feature, you first need to create an instance object of AudioService and call the tts method.  

__tts__

`
TTSResult tts(TTSRequestParam param);
`

Getting audio files  
>Parameters:

| Name | type | Description |
|------|-----------------------|--------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
|param| TTSRequestParam |The content of the request transformation and configuration information of the user model, including the user token, and the text to be transformed.|

>Returns:    

| Name | type | Description |
|------|-----------------------|--------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| TTSResult | TTSResult |An object containing the results of the text-to-speech, the object's choices property, contains the audio file path address returned by

>Example invocation:

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

## Image generation functionality  
To use image generation, you first need to create an instance of ImageGenerationService and call its generations method.

__generations__

`
ImageGenerationResult generations(ImageGenerationRequest request);
`

Get the generated image path address  
>parameters：

| Name | type | Description |
|------|-----------------------|--------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
|request| ImageGenerationRequest |Request result set, containing request text information.|

>Returns:  

| Name | type | Description |
|------|-----------------------|--------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| ImageGenerationResult | ImageGenerationResult |An object containing the results of the image generation, the object's choices property, contains the image file path address returned by|
>Example invocation:

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


## Upload the private training files

To use this feature, you first need to create an instance of VectorDbService and call its addFileVectors method.  

__addFileVectors__

`
void addFileVectors(File file, Map<String, Object> metadatas, String category) throws IOException;
`

Upload private training files.    
>Parameters:

| Name | type | Description |
|------|-----------------------|----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
|file| File |The URL of the file requested to be uploaded.|
|metadatas| Map<String, Object> |Request result set, including request file name, file type and other information.|
|category| String |File type.|
>Returns:

No return value.  

>Example invocation:

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

## The visual-speak feature

To use this feature, you first need to create an instance of AllImageService and call the toText method.

__toText__

`
ImageToTextResponse toText(FileRequest param)
`

Get the generated text result set.     
>Parameters:

| Name | type | Description |
|------|-----------------------|----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
|param| FileRequest |Request result set, containing image path and address information.|

>Returns:

| Name | type | Description |
|------|-----------------------|--------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
|result| ImageToTextResponse |An object containing the results of the image generation, the object's choices property, contains the image file path address returned by|
>Example invocation:   

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

## Video Tracking feature

To use video tracking, you first need to create an instance of VideoService and call the track method.

__track__

`
VideoGenerationResult track(String videoUrl);
`

Obtain the generated video result set.     

>Parameters:   

| Name | type | Description |
|------|-----------------------|--------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
|videoUrl| String |Request to track the video address.|

>Returns:   

| Name | type | Description |
|------|-----------------------|--------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
|result| VideoGenerationResult |An object containing the results of the video generation, the object's choices property, contains the video file path address returned by|

>Example invocation:

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

## Image Enhancements feature

To use image enhancement, you first need to create an instance of AllImageService and call the enhance method.  

__enhance__

`
ImageEnhanceResult enhance(String imageUrl);
`

Obtain the generated image result set.   
>Parameters:  

| Name | type | Description |
|------|-----------------------|---------------|
|imageUrl| String |Request enhanced image address.|

>Returns:  

| Name | type | Description |
|------|-----------------------|---------------|
|result| ImageEnhanceResult |An object containing the results of the image generation, the object's choices property, contains the image file path address returned by|

>Example invocation:

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

## Image to Video feature

To use the graph-generated video functionality, you first need to create an instance object of AllVideoService and call the image2Video method.   

__image2Video__

`
VideoGenerationResult image2Video(String imageUrl);
`

Obtain the generated video result set.    
>Parameters:  

| Name | type | Description |
|------|-----------------------|--------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
|imageUrl| String |Request the image address of the generated video.|

>Returns:  

| Name | type | Description |
|------|-----------------------|----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
|result| VideoGenerationResult |An object containing the results of the video generation, the object's choices property, contains the video file
>Examples invocation:

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

## Video Enhancements feature

To use video enhancement, you first need to create an instance object of AllVideoService and call the enhance method.   

__enhance__

`
ideoGenerationResult enhance(String videoUrl);
`

Obtain the generated augmented video result set.   
>Parameters:

| Name | type | Description |
|------|-----------------------|----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
|videoUrl| String |Request enhanced video address.|

>Returns:  

| Name | type | Description |
|------|-----------------------|----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
|result| VideoGenerationResult |An object containing the results of the video generation, the object's choices property, contains the video file path address returned by|

>Examples invocation: 

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