package ai.example;

import ai.audio.service.AudioService;
import ai.common.pojo.*;
import ai.config.ContextLoader;
import ai.embedding.Embeddings;
import ai.embedding.impl.ErnieEmbeddings;
import ai.embedding.impl.TelecomGteEmbeddings;
import ai.image.pojo.ImageEnhanceRequest;
import ai.image.service.AllImageService;
import ai.llm.service.CompletionsService;
import ai.medusa.pojo.InstructionPairRequest;
import ai.openai.pojo.ChatCompletionChoice;
import ai.openai.pojo.ChatCompletionRequest;
import ai.openai.pojo.ChatCompletionResult;
import ai.openai.pojo.ChatMessage;
import ai.vector.VectorStoreService;
import ai.vector.impl.ChromaVectorStore;
import ai.vector.pojo.IndexRecord;
import ai.vector.pojo.QueryCondition;
import ai.vector.pojo.UpsertRecord;
import ai.video.pojo.*;
import ai.video.service.AllVideoService;
import com.google.common.collect.Lists;
import io.reactivex.Observable;
import org.junit.Test;

import java.io.File;
import java.util.*;
import java.util.stream.Collectors;


public class Demo {

    static {
        //initialize Profiles
        ContextLoader.loadContext();
    }
    private final Embeddings ef = new TelecomGteEmbeddings(ContextLoader.configuration.getFunctions().getEmbedding().get(0));


    private final ChromaVectorStore vectorStore = new ChromaVectorStore(ContextLoader.configuration.getStores().getVectors().get(0),ef);

    private final VectorStoreService vectorStoreService = new VectorStoreService();
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
        message.setContent("你好，你是那个公司开发的");
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
    public static void Text2Audio(){
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

    public static void main(String[] args) {
         //completions example
         chat();

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

    }

        /**
     * 查询相似度
     * @param
     * @return
     */
    public List<IndexRecord> query(String ttt){
        int n = 32;
        Map<String, String> where = new HashMap<>();
        List<IndexRecord> list1 = new ArrayList<>();
        while (n>0) {
            QueryCondition queryCondition = new QueryCondition();
            queryCondition.setN(n);
            queryCondition.setText(ttt);
            queryCondition.setWhere(where);
            list1 =  vectorStore.query(queryCondition, "bj-telecom");
            list1 = list1.stream()
                    //.filter(record -> record.getDistance() < 0.5)
                    .collect(Collectors.toList());
            n=-1;
        }
        return list1;
    }
    @Test
    public void ss(){
        UpsertRecord upsertRecord = new UpsertRecord();
        upsertRecord.setId("a375a16d303b4894bf2c4a16a917ae35111");
        upsertRecord.setDocument("这个是传统类型");
        Map<String, String> metadata = new HashMap<>();
        // 添加键值对到Map中
        metadata.put("seq", "1732167567766");
        metadata.put("category", "bj-telecom");
        metadata.put("file_id", "399bb6919b2a43cab340b88c06daee01");
        metadata.put("filename", "（中电信京〔2024〕168号）《关于印发中国电信北京公司员工职业发展管理办法（2024版）的通知》.pdf");
        metadata.put("filepath", "202410221728017583.pdf");
        metadata.put("parent_id", "59124c97754947c3ab0a443fe536ccf1");
        upsertRecord.setMetadata(metadata);
        List<UpsertRecord> upsertRecords = new ArrayList<>();
        upsertRecords.add(upsertRecord);
        vectorStore.upsert(upsertRecords, "bj-telecom");

        System.out.println(query("16升17岗条件是什么，需要多少积分"));

    }


         @Test
        public void xiugai() {
UpsertRecord upsertRecord = new UpsertRecord();
upsertRecord.setId("40e9abd133304878ae0c2b3c2d1a14f1");

String fff = "积分标准说明："+
    "我们的晋升体系为不同岗位等级设定了明确的积分标准，以确保员工晋升的公平性和透明性。具体如下：\n"+
    "辅助级：1升2岗需积分为20分。" +
    "辅助级：2升3岗需积分为20分。" +
    "辅助级：3升4岗需积分为20分。" +

    "初级：4升5岗需积分为30分。" +
    "初级：5升6岗需积分为30分。" +
    "初级：6升7岗需积分为30分。" +
    "初级：7升8岗需积分为30分。" +

    "中级：8升9岗需积分为50分。" +
    "中级：9升10岗需积分为50分。" +
    "中级：10升11岗需积分为50分。" +
    "中级：11升12岗需积分为50分。" +

    "高级：12升13岗需积分为70分。" +
    "高级：14升15岗需积分为70分。" +
    "高级：15升16岗需积分为70分。" +
    "高级：16升17岗需积分为70分。" +

    "晋升流程："+
    "公司每年将统一组织员工的职业发展和晋升工作。在满足晋升条件的基础上，员工可以根据自己的积分和个人意愿，选择是否晋升以及晋升的具体形式。"+
    "积分使用规则：员工在晋升至相应的岗位等级或工资档次后，需从个人积分中扣除相应的积分数额，之后继续积累新的积分。"+
    "若员工因提任而晋升超过一个等级，需扣除相应的晋升积分。若积分不足，将在新岗位等级上从0分重新开始积累。"+
    "附则：本晋升办法自2024年6月28日起正式实施。若此前的规定与本办法存在冲突，应以本办法为准。"+
    "本办法的最终解释权归北京公司人力资源部所有。"+
    "发布信息："+
    "本办法由中国电信北京公司办公室于2024年10月9日印发。";

upsertRecord.setDocument(fff);



Map<String, String> metadata = new HashMap<>();
metadata.put("seq", "1732167567766"); // 假设这个seq是你需要的
metadata.put("category", "bj-telecom");
metadata.put("file_id", "7f4b4e0bc7184e55ba3ccb24e2883c68");
metadata.put("filename", "（中电信京〔2024〕165号）《关于印发中国电信北京公司员工积分管理办法（2024版）的通知》.pdf");
metadata.put("filepath", "202411211311536951.pdf");
metadata.put("level", "user");
metadata.put("parent_id", "8085d8a3db8b4b9ab7c5c3ea03219f32");
upsertRecord.setMetadata(metadata);

List<UpsertRecord> upsertRecords = new ArrayList<>();
upsertRecords.add(upsertRecord);

         vectorStore.upsert(upsertRecords, "bj-telecom");
         System.out.println(query("计达到 4 个 A，且通过公司专业评审要求，岗位等级可晋升一等"));
    }
@Test
    public void tistco1i() {
       UpsertRecord upsertRecord = new UpsertRecord();
        upsertRecord.setId("4a05db980da3421784432a8684528c78");
        upsertRecord.setDocument("升岗需要什么条件？/这个问题需要根据提供的信息，从晋升路径，晋升需要多少积分，胜任力要求，积分是否保留来完整的综合回答我"
        +"积分标准说明："+
            "我们的晋升体系为不同岗位等级设定了明确的积分标准，以确保员工晋升的公平性和透明性。具体如下：\n"+
            "辅助级：1升2岗需积分为20分。" +
            "辅助级：2升3岗需积分为20分。" +
            "辅助级：3升4岗需积分为20分。" +

            "初级：4升5岗需积分为30分。" +
            "初级：5升6岗需积分为30分。" +
            "初级：6升7岗需积分为30分。" +
            "初级：7升8岗需积分为30分。" +

            "中级：8升9岗需积分为50分。" +
            "中级：9升10岗需积分为50分。" +
            "中级：10升11岗需积分为50分。" +
            "中级：11升12岗需积分为50分。" +

            "高级：12升13岗需积分为70分。" +
            "高级：14升15岗需积分为70分。" +
            "高级：15升16岗需积分为70分。" +
            "高级：16升17岗需积分为70分。"
        );

Map<String, String> metadata = new HashMap<>();
metadata.put("category", "bj-telecom");
metadata.put("file_id", "7f4b4e0bc7184e55ba3ccb24e2883c68");
metadata.put("filename", "（中电信京〔2024〕165号）《关于印发中国电信北京公司员工积分管理办法（2024版）的通知》.pdf");
metadata.put("filepath", "202411211311536951.pdf");
metadata.put("level", "user");
metadata.put("parent_id", "a870295c0a8044479fd65b4e37ef1fe8");
metadata.put("seq", "1732188524545");
upsertRecord.setMetadata(metadata);

List<UpsertRecord> upsertRecords = new ArrayList<>();
upsertRecords.add(upsertRecord);
         System.out.println(query("计达到 4 个 A，且通过公司专业评审要求，岗位等级可晋升一等"));
    }
}
