package ai.example;

import ai.audio.service.AudioService;
import ai.common.pojo.*;
import ai.config.ContextLoader;
import ai.image.pojo.ImageEnhanceRequest;
import ai.image.service.AllImageService;
import ai.llm.service.CompletionsService;
import ai.openai.pojo.*;
import ai.video.pojo.*;
import ai.video.service.AllVideoService;
import cn.hutool.json.JSONUtil;
import com.google.common.collect.Lists;
import io.reactivex.Observable;

import java.io.File;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;


public class Demo {

    static {
        //initialize Profiles
        ContextLoader.loadContext();
    }

    /**
     * completions Demo Method
     * Demonstration function for testing the chat completion feature.
     * This function initializes the environment, constructs a mock chat completion request, calls the completions method of the CompletionsService class,
     * and prints the content of the first completion result.
     */

    public static void chat() {

        //mock request
        ChatCompletionRequest chatCompletionRequest = new ChatCompletionRequest();
        chatCompletionRequest.setTemperature(0.8);
        chatCompletionRequest.setMax_tokens(1024);
        chatCompletionRequest.setCategory("default");
        ChatMessage message = new ChatMessage();
        message.setRole("user");
        message.setContent("你好");
        chatCompletionRequest.setMessages(Lists.newArrayList(message));
        // Set the stream parameter to false
        chatCompletionRequest.setStream(false);
        // Create an instance of CompletionsService
        CompletionsService completionsService = new CompletionsService();
        // Call the completions method to process the chat completion request
        ChatCompletionResult result = completionsService.completions(chatCompletionRequest);

        // Print the content of the first completion choice
        System.out.println("outcome:" + result.getChoices().get(0).getMessage().getContent());

    }

    /**
     * streamCompletions Demo Method
     * Demonstration function for chat completion.
     * Initializes and sends a chat completion request, then processes and outputs the completion results through an observable subscription.
     */

    public static void streamChat() {
        // Initialize the chat completion request
        ChatCompletionRequest chatCompletionRequest = new ChatCompletionRequest();
        chatCompletionRequest.setTemperature(0.8);
        chatCompletionRequest.setMax_tokens(1024);
        chatCompletionRequest.setCategory("default");
        ChatMessage message = new ChatMessage();
        message.setRole("user");
        message.setContent("你好");
        chatCompletionRequest.setMessages(Lists.newArrayList(message));
        // Create a CompletionsService instance for sending completion requests
        CompletionsService completionsService = new CompletionsService();
        // Set the chat completion request to stream mode
        chatCompletionRequest.setStream(true);
        // Send the request and get an observable for streaming results
        Observable<ChatCompletionResult> observable = completionsService.streamCompletions(chatCompletionRequest);
        // Use an array of size 2 to store the last two result states
        final ChatCompletionResult[] lastResult = {null, null};
        observable.subscribe(
                // Update the latest result
                data -> {
                    lastResult[0] = data;
                    // If the second latest result is null, initialize it with the latest resu
                    if (lastResult[1] == null) {
                        lastResult[1] = data;
                    } else {
                        // Merge the content of the same choice index in the latest two results
                        for (int i = 0; i < lastResult[1].getChoices().size(); i++) {
                            ChatCompletionChoice choice = lastResult[1].getChoices().get(i);
                            ChatCompletionChoice chunkChoice = data.getChoices().get(i);
                            // Merge the content
                            String chunkContent = chunkChoice.getMessage().getContent();
                            String content = choice.getMessage().getContent();
                            choice.getMessage().setContent(content + chunkContent);
                            // Output the merged content
                            System.out.println("outcome" + content);
                        }
                    }
                }
        );
    }

    /**
     * ASR Demo Method
     * This method demonstrates how to utilize the speech service for audio recognition.
     * It starts by specifying a string containing the path to the audio file, then creates
     * an audio request parameter object and an audio service object.
     * Subsequently, it uses the recognition method of the audio service object to identify
     * the audio file and prints the recognition result.
     */
    public static void asrAudio(){
        // Specify the path to the audio file
        String resPath = "C:\\temp\\audiofile.wav";
        // Create an audio request parameter object
        AudioRequestParam param = new AudioRequestParam();
        // Instantiate the audio service object
        AudioService audioService = new AudioService();
        // Perform audio recognition using the speech service
        AsrResult result = audioService.asr(resPath, param);
        // Print the recognition result
        System.out.println("outcome:" +result);
    }

    /**
     * tts Demo Method
     * Converts text into audio.
     * This method demonstrates the process of using a Text-to-Speech (TTS) service to convert simple text into corresponding audio.
     * It initializes a TTS request parameter object, sets the text to be converted,
     * then employs the audio service class to execute the TTS operation and receives the converted audio result.
     * Lastly, it prints out the result of the operation.
     */
    public void Text2Audio(){
        // Initialize the TTS request parameter object and set the text for conversion
        TTSRequestParam request = new TTSRequestParam();
        request.setText("你好");

        // Instantiate the audio service
        AudioService audioService = new AudioService();

        // Invoke the tts method of the audio service to convert text into audio
        TTSResult result = audioService.tts(request);

        // Print the conversion result
        System.out.println("outcome" +result);
    }

    /**
     * generations Demo Method
     * Generates a landscape map image.
     * This method creates an image generation request, specifies the prompt for the map generation,
     * calls the image service to perform the generation, and outputs the result.
     * The purpose is to demonstrate how to use the image generation service to create a specific type of image.
     */
    public static void generationsImage() {
        // Create an image generation request object
        ImageGenerationRequest request = new ImageGenerationRequest();
        // Set the prompt for the image generation, specifying the desired landscape map
        request.setPrompt("Help me generate a landscape map");

        // Create an instance of the image service to handle the image generation request
        AllImageService imageService = new AllImageService();
        // Call the image service to perform the generation, passing in the request and receiving the result
        ImageGenerationResult result = imageService.generations(request);

        // Output the result of the image generation
        System.out.println("outcome:" +result.getData());
    }

    /**
     * toText Demo Method
     * Converts an image into text description.
     * This method utilizes an image processing service to convert the image located at a specified path into a textual description.
     * Primarily used for translating visual content into descriptive text, such as identifying objects or scenes within the image.
     */
    public static void Image2Text() {
        // Specifies the path of the image file to be converted
        // YOUR IMAGE URL
        String lastImageFile = "C:\\temp\\th.jpg";
        // Instantiates the image processing service
        AllImageService allImageService = new AllImageService();
        // Creates a File object based on the file path
        File file = new File(lastImageFile);
        // Invokes the service to convert the image into text description
        ImageToTextResponse text = allImageService.toText(FileRequest.builder().imageUrl(file.getAbsolutePath()).build());
        // Outputs the converted text description
        System.out.println("outcome:" +text.getCaption());
    }

    /**
     * track Demo Method
     * Tracks the status of a video processing task.
     * This function initiates a request to the video service using the provided video URL,
     * aimed at fetching the current status of the video processing job.
     * It serves to monitor the progress or result of a video upload or processing task.
     */
    public static void trackVideo() {
        // YOUR Video URL
        String lastVideoFile = "https://abc12345abc.oss-cn-hangzhou.aliyuncs.com/a8345c6f036787646fe807f9bfff7870.mp4";

        // Instantiate the video service to perform tracking operations
        AllVideoService videoService = new AllVideoService();

        // Construct a request for video tracking with the specified video URL
        VideoTackRequest videoTackRequest = VideoTackRequest.builder().videoUrl(lastVideoFile).build();

        // Invoke the tracking method on the video service and obtain the response detailing the video job's status
        VideoJobResponse track = videoService.track(videoTackRequest);

        // Print out the result of the video processing status
        System.out.println("outcome:" + track.getData());
    }

    /**
     * Image enhance Demo Method
     * Enhances the quality of an image.
     * This method invokes an image enhancement service to improve the quality of the specified image URL.
     * It is primarily used to enhance image clarity and color representation, suitable for image processing scenarios.
     */
    public static void enhanceImage() {
        // Set the URL of the image to be processed
        String imageUrl = "https://abc12345abc.oss-cn-hangzhou.aliyuncs.com/a.png";
        // Instantiate the image enhancement service
        AllImageService allImageService = new AllImageService();
        // Build the image enhancement request with the specified image URL
        ImageEnhanceRequest imageEnhanceRequest = ImageEnhanceRequest.builder().imageUrl(imageUrl).build();
        // Invoke the image enhancement service to process the request
        ImageEnhanceResult enhance = allImageService.enhance(imageEnhanceRequest);
        // Output the processing result
        System.out.println("outcome:" + enhance);
    }

    /**
     * image2Video Demo Method
     * Converts an image into a video. This method demonstrates the process of
     * transforming a single image into a video by invoking the image2Video method
     * from the AllVideoService class. Primarily used as an example to showcase
     * image-to-video conversion capabilities.
     */
    public static void image2Video() {
        // Specifies the path of the image to be converted
        String imageUrl = "C:\\temp\\th.jpg";
        // Instantiates AllVideoService to access video generation services
        AllVideoService allVideoService = new AllVideoService();

        // Constructs the request for video generation, specifying details of the input image
        VideoGeneratorRequest videoGeneratorRequest = VideoGeneratorRequest.builder()
                .inputFileList(Collections.singletonList(InputFile.builder().url(imageUrl).name("th").type("jpg").build()))
                .build();

        // Submits the request to convert the image into a video via the service interface
        VideoJobResponse videoGenerationResult = allVideoService.image2Video(videoGeneratorRequest);

        // Outputs the data portion of the generation result
        System.out.println("outcome:" + videoGenerationResult.getData());
    }

    /**
     * Video enhance Demo Method
     * A static method to enhance the quality of a video.
     * This method utilizes the enhance function from AllVideoService to process and improve the quality of a specified video file,
     * primarily serving as a demonstration of video enhancement capabilities.
     */
    public static void enhanceVideo(){
        // Specify the path of the video file to be processed
        String lastVideoFile = "https://abc12345abc.oss-cn-hangzhou.aliyuncs.com/a8345c6f036787646fe807f9bfff7870.mp4";
        // Instantiate AllVideoService to invoke video enhancement features
        AllVideoService allVideoService = new  AllVideoService();

        // Build a video enhancement request object with the video URL set
        VideoEnhanceRequest videoEnhanceRequest = new VideoEnhanceRequest();
        videoEnhanceRequest.setVideoURL(lastVideoFile);
        // Invoke the video enhancement service, passing the request object, and receive the processing result
        VideoJobResponse videoGenerationResult = allVideoService.enhance(videoEnhanceRequest);
        // Output the data portion of the processing result
        System.out.println("outcome:" + videoGenerationResult.getData());
    }


    public static void functionCall(){
        //mock request
        ChatCompletionRequest chatCompletionRequest = new ChatCompletionRequest();
        chatCompletionRequest.setTemperature(0.3);
        chatCompletionRequest.setMax_tokens(1024);
        ChatMessage message = new ChatMessage();
        message.setRole("user");
        message.setContent("编程判断 3214567 是否是素数。");
        chatCompletionRequest.setMessages(Lists.newArrayList(message));

        Tool tool = new Tool();
        tool.setType("function");
        Function function = new Function();
        tool.setFunction(function);

        function.setName("CodeRunner");
        function.setDescription("代码执行器，支持运行 python 和 javascript 代码");
        Parameters parameters = new Parameters();
        parameters.setType("object");
        Map<String, Property> properties = new HashMap<>();
        
        Property property1 = new Property();
        property1.setEnums(Lists.newArrayList("python", "javascript"));
        property1.setType("string");
        properties.put("language", property1);

        Property property2 = new Property();
        property2.setDescription("代码写在这里");
        property2.setType("string");
        properties.put("code", property2);

        parameters.setProperties(properties);
        function.setParameters(parameters);
        chatCompletionRequest.setTools(Lists.newArrayList(tool));

        // Set the stream parameter to false
        chatCompletionRequest.setStream(false);

        System.out.println("request:" + JSONUtil.toJsonStr(chatCompletionRequest));

        // Create an instance of CompletionsService
        CompletionsService completionsService = new CompletionsService();
        // Call the completions method to process the chat completion request
        ChatCompletionResult result = completionsService.completions(chatCompletionRequest);

        // Print the content of the first completion choice
//        System.out.println("outcome:" + result.getChoices().get(0).getMessage().getContent());
        System.out.println("outcome:" + JSONUtil.toJsonStr(result));
    }

    public static void main(String[] args) {
         //completions example
//         chat();

        //streamCompletions example
        //streamChat();

        //asr example
        //asrAudio();

        //tts example
        //Text2Audio();

        //generations example
        //generationsImage();

        //toText example
        //Image2Text();

        //track example
        //trackVideo();

        //Image enhance example
        //enhanceImage();

        //image2Video example
        //image2Video();

        //Video enhance example
        //enhanceVideo();

        functionCall();

    }


}
