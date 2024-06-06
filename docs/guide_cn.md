# 大语言模型功能调用指南

## 文本对话功能
要使用文本对话功能首先需要创建一个 CompletionsService 的实例对象。 这个对象有两个方法 completions,streamCompletions。

__completions__  

`
ChatCompletionResult completions(ChatCompletionRequest chatCompletionRequest)
`  
一次性获取大模型的对话的回答结果  
参数：  
  chatCompletionRequest - 对话请求参数包含对话使用的模型,对话的上下文及一些模型参数  
返回：  
一个包含大模型结果的对象,对象的 choices属性，包含着大模型的返回的回答文本
示例：
```java
CompletionsService completionsService = new CompletionsService();
ChatCompletionResult result = completionsService.completions(chatCompletionRequest);
String text = result.getChoices().get(0).getMessage().getContent();
```

__streamCompletions__

`
Observable<ChatCompletionResult> streamCompletions(ChatCompletionRequest chatCompletionRequest)
`  
使用流的方式返回大模型对话的结果  
参数：  
chatCompletionRequest - 对话请求参数包含对话使用的模型,对话的上下文及一些模型参数    
返回：  
返回一个流的观察者对象。 可以通过这个对象获取流的返回结果, 你可以将写入到 HttpServletResponse 的输出流中  
示例：
```java
HttpServletResponse resp;
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
参数：  

audio - 音频地址，  
param - 语音识别请求参数。

返回：   
返回一个 AsrResult 对象，包含语音识别结果集。  
示例：  
```java
AudioService audioService = new AudioService();
AsrResult result = audioService.asr(audio, param);
```

## 文字转语音功能
要使用文字转语音功能，首先需要创建一个 AudioService 的实例对象，调用tts方法。

__tts__ 

`
TTSResult tts(TTSRequestParam param);
`

获取音频文件  
参数：  

TTSRequestParam - 请求转换的内容和用户模型的配置信息，包含用户的token，以及要转换的文本。

返回：  
返回一个 TTSResult 对象，包含文本识别结果集。  
示例：

```java
   AudioService audioService = new AudioService();
   TTSRequestParam ttsRequestParam = new TTSRequestParam();
   ttsRequestParam.setText(text2VoiceEntity.getText());
   ttsRequestParam.setEmotion(text2VoiceEntity.getEmotion());
   TSResult result = audioService.tts(ttsRequestParam);
```

## 图片生成功能
要使用图片生成功能，首先需要创建一个 ImageGenerationService 的实例对象，调用对应的generations方法。

__generations__

`
ImageGenerationResult generations(ImageGenerationRequest request);
`

获取生成图片路径地址  
参数：

ImageGenerationRequest - 请求结果集，包含请求文本信息。

返回：  
返回一个 ImageGenerationResult 对象，请求生成的结果集，包含图片路径地址信息。  
示例：

```java
ImageGenerationService service = new ImageGenerationService();
ImageGenerationResult result = service.generations(request);
```


## 上传私训学习文件功能

要使用上传私训学习文件功能，首先需要创建一个 VectorDbService 的实例对象，调用对应的addFileVectors方法。

__addFileVectors__

`
void addFileVectors(File file, Map<String, Object> metadatas, String category) throws IOException;
`

上传私训学习文件。  
参数：

file- 请求上传文件路径地址。
metadatas- 请求结果集，包含请求文件名，文件类型等信息。  
category - 文件类型。
返回：   
无返回值。
示例：

```java
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
          vectorDbService.addFileVectors(this.file, metadatas, category);
          UploadFile entity = new UploadFile();
                entity.setCategory(category);
                entity.setFilename(filename);
                entity.setFilepath(filepath);
                entity.setFileId(fileId);
                uploadFileService.addUploadFile(entity);
            } catch (IOException | SQLException e) {
                 e.printStackTrace();
            }
 }
```

## 看图说话功能

要使用看图说话功能，首先需要创建一个 AllImageService 的实例对象，调用toText方法。

__toText__

`
ImageToTextResponse toText(FileRequest param)
`

获取生成文本结果集。  
参数：

FileRequest - 请求结果集，包含图片路径地址信息。

返回：  
返回一个 ImageToTextResponse 对象，请求生成的结果集，包含生成文本信息。
示例：

```java
AllImageService allImageService = new AllImageService();
File file = new File(lastImageFile);
ImageToTextResponse text = allImageService.toText(FileRequest.builder().imageUrl(file.getAbsolutePath()).build());
```

## 视频追踪功能

要使用视频追踪功能，首先需要创建一个 VideoService 的实例对象，调用track方法。

__track__

`
VideoGenerationResult track(String videoUrl);
`

获取生成视频结果集。  
参数：

videoUrl - 请求追踪视频地址。

返回：  
返回一个 VideoGenerationResult 对象，请求生成的结果集，包含生成追踪后视频地址。
示例：

```java
VideoService videoService = new VideoService();
File file = new File(lastVideoFile);
VideoGenerationResult track = videoService.track(file.getAbsolutePath());
```

## 图像增强功能

要使用图像增强功能，首先需要创建一个 AllImageService 的实例对象，调用enhance方法。

__enhance__

`
ImageEnhanceResult enhance(String imageUrl);
`

获取生成图像结果集。  
参数：

imageUrl - 请求增强图片地址。

返回：  
返回一个 ImageEnhanceResult 对象，请求生成的结果集，包含请求生成增强后图片地址。
示例：

```java
AllImageService allImageService = new AllImageService();
ImageEnhanceResult enhance = allImageService.enhance(imageUrl);
```


## 图生视频

要使用图生视频功能，首先需要创建一个 AllVideoService 的实例对象，调用image2Video方法。

__image2Video__

`
VideoGenerationResult image2Video(String imageUrl);
`

获取生成视频结果集。  
参数：

imageUrl - 请求生成视频的图片地址。

返回：  
返回一个 VideoGenerationResult 对象，请求生成的结果集，包含请求生成视频地址。  
示例：

```java
AllVideoService allVideoService = new  AllVideoService();
VideoGenerationResult videoGenerationResult = allVideoService.image2Video(imageUrl);
```

## 视频增强

要使用视频增强功能，首先需要创建一个 AllVideoService 的实例对象，调用enhance方法。

__enhance__

`
ideoGenerationResult enhance(String videoUrl);
`

获取生成增强视频结果集。  
参数：

videoUrl - 请求增强视频地址。

返回：  
返回一个 ideoGenerationResult 对象，请求生成的结果集，包含请求生成增强后视频地址。  
示例：

```java
AllVideoService allVideoService = new  AllVideoService();
VideoGenerationResult videoGenerationResult = allVideoService.enhance(videoUrl);
```