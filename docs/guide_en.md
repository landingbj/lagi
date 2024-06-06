# Large Language model function Invocation Guide

## chat function
To use chat function you need to create an instance object of CompletionsService. This object has two methods completions, streamCompletions.

__completions__

`
ChatCompletionResult completions(ChatCompletionRequest chatCompletionRequest)
`  
Get the answer results of the large model conversation at once   
Parameters：  
  chatCompletionRequest - Session request parameters include the model used by the session, the context of the session, and some model parameters 
Returns：   
An object containing the results of the large model, the object's choices property, contains the answer text returned by the large model   
Example：
```java
CompletionsService completionsService = new CompletionsService();
ChatCompletionResult result = completionsService.completions(chatCompletionRequest);
String text = result.getChoices().get(0).getMessage().getContent();
```

__streamCompletions__

`
Observable<ChatCompletionResult> streamCompletions(ChatCompletionRequest chatCompletionRequest)
`  
Returns the results of a large model conversation using a stream   
Parameters：  
chatCompletionRequest - Session request parameters include the model used by the session, the context of the session, and a set of model parameters    
Returns：   
Session request parameters include the model used by the session, the context of the session, and a set of model parameters   
Example：
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
Parameters: 

audio - audio address,  
param - Speech recognition request parameters.

Returns:    
Returns an AsrResult object containing the set of speech recognition results.   
Examples:   
```java
AudioService audioService = new AudioService();
AsrResult result = audioService.asr(audio, param);
```

## Text-to-Speech features
To use the text-to-speech feature, you first need to create an instance object of AudioService and call the tts method.  

__tts__

`
TTSResult tts(TTSRequestParam param);
`

Getting audio files  
Parameters:

TTSRequestParam - The content of the request transformation and configuration information of the user model, including the user token, and the text to be transformed.

Returns:    
Returns a TTSResult object containing the text recognition result set.  
Examples: 

```java
   AudioService audioService = new AudioService();
   TTSRequestParam ttsRequestParam = new TTSRequestParam();
   ttsRequestParam.setText(text2VoiceEntity.getText());
   ttsRequestParam.setEmotion(text2VoiceEntity.getEmotion());
   TSResult result = audioService.tts(ttsRequestParam);
```

## Image generation functionality  
To use image generation, you first need to create an instance of ImageGenerationService and call its generations method.

__generations__

`
ImageGenerationResult generations(ImageGenerationRequest request);
`

Get the generated image path address  
parameters：

ImageGenerationRequest - Request result set, containing request text information.  

Returns:  
Returns an ImageGenerationResult object that requests the generated result set, including the image path address information.  
Examples:

```java
ImageGenerationService service = new ImageGenerationService();
ImageGenerationResult result = service.generations(request);
```


## The ability to upload private training files

To use this feature, you first need to create an instance of VectorDbService and call its addFileVectors method.  

__addFileVectors__

`
void addFileVectors(File file, Map<String, Object> metadatas, String category) throws IOException;
`

Upload private training files.    
Parameters:

file - The URL of the file requested to be uploaded.
metadatas - Request result set, including request file name, file type and other information.    
category - File type.  
Returns:    
No return value.  
Examples:

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

## The visual-speak feature

To use this feature, you first need to create an instance of AllImageService and call the toText method.

__toText__

`
ImageToTextResponse toText(FileRequest param)
`

Get the generated text result set.     
Parameters:

FileRequest - Request result set, containing image path and address information.

Returns:  
Returns an ImageToTextResponse object that requests the generated result set, containing the generated text information.   
Examples:   

```java
AllImageService allImageService = new AllImageService();
File file = new File(lastImageFile);
ImageToTextResponse text = allImageService.toText(FileRequest.builder().imageUrl(file.getAbsolutePath()).build());
```

## Video Tracking feature

To use video tracking, you first need to create an instance of VideoService and call the track method.

__track__

`
VideoGenerationResult track(String videoUrl);
`

Obtain the generated video result set.     
Parameters:   

videoUrl - Request to track the video address.

Returns:   
Returns a VideoGenerationResult object that requests the generated result set, including the video address after generating the trace.
Examples:   

```java
VideoService videoService = new VideoService();
File file = new File(lastVideoFile);
VideoGenerationResult track = videoService.track(file.getAbsolutePath());
```

## Image Enhancements

To use image enhancement, you first need to create an instance of AllImageService and call the enhance method.  

__enhance__

`
ImageEnhanceResult enhance(String imageUrl);
`

Obtain the generated image result set.   
Parameters:  

imageUrl - Request enhanced image address.

Returns:  
Returns an ImageEnhanceResult object that requests the generated result set, including the address of the requested enhanced image.  
Examples:

```java
AllImageService allImageService = new AllImageService();
ImageEnhanceResult enhance = allImageService.enhance(imageUrl);
```


## Image to Video

To use the graph-generated video functionality, you first need to create an instance object of AllVideoService and call the image2Video method.   

__image2Video__

`
VideoGenerationResult image2Video(String imageUrl);
`

Obtain the generated video result set.    
Parameters:  

imageUrl - Request the image address of the generated video.

Returns:  
Returns a VideoGenerationResult object, the result set of the request generation, containing the address of the requested generation video.  
Examples:

```java
AllVideoService allVideoService = new  AllVideoService();
VideoGenerationResult videoGenerationResult = allVideoService.image2Video(imageUrl);
```

## Video Enhancements

To use video enhancement, you first need to create an instance object of AllVideoService and call the enhance method.   

__enhance__

`
ideoGenerationResult enhance(String videoUrl);
`

Obtain the generated augmented video result set.   
Parameters:

videoUrl - Request enhanced video address.

Returns:  
Returns an ideoGenerationResult object, the result set of the request generation, containing the address of the request generation enhanced video.    
Examples: 

```java
AllVideoService allVideoService = new  AllVideoService();
VideoGenerationResult videoGenerationResult = allVideoService.enhance(videoUrl);
