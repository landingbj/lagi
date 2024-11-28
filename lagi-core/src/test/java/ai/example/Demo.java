package ai.example;

import ai.audio.service.AudioService;
import ai.common.pojo.*;
import ai.config.ContextLoader;
import ai.database.impl.MysqlAdapter;
import ai.database.pojo.MysqlObject;
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
import com.google.gson.Gson;
import io.reactivex.Observable;
import org.apache.poi.ss.formula.functions.T;
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
        System.out.println(query("关于印发中国电信北京公司员工积分管理办法"));

    }

@Test
    public void tistco1i() {
        UpsertRecord upsertRecord = new UpsertRecord();
        upsertRecord.setId("2b237f5db53e4f50a85525ceb99aad60");
        upsertRecord.setDocument("升岗需要什么条件,这段话，我是用来做测试的");

        Map<String, String> metadata = new HashMap<>();
        metadata.put("category", "bj-telecom");
        metadata.put("file_id", "7f4b4e0bc7184e55ba3ccb24e2883c68");
        metadata.put("filename", "关于印发中国电信北京公司员工胜任力管理办法（试行）的通知中电信京〔2019〕82号.pdf");
        metadata.put("filepath", "202410221725349494.pdf");
        metadata.put("level", "user");
        metadata.put("parent_id", "332b10727e3b4acf896663d62c60ab33");
        metadata.put("seq", "1732190747701");
        upsertRecord.setMetadata(metadata);

        List<UpsertRecord> upsertRecords = new ArrayList<>();
        upsertRecords.add(upsertRecord);

        vectorStore.upsert(upsertRecords, "bj-telecom");
         System.out.println(query("计达到 4 个 A，且通过公司专业评审要求，岗位等级可晋升一等"));
    }


@Test
    public void tistco2i() {
        String demand = "2024年全国协议酒店中5星级酒店有哪几家";
       String out = chat1(demand);
       System.out.println("out1的回答是："+out);
        String outcome =  "";
       List<Map<String, Object>> list = new ArrayList<>();
         Gson gson = new Gson();
            if (out != null){
                outcome =  extractContentWithinBraces(out);
                 //System.out.println("outcome:" + outcome);
                String sql = extractContentWithinBraces(outcome);
                System.out.println("sql:" + sql);
                list = new AiZindexUserDao().sqlToValue(sql);

                for (Object o : list) {
                    System.out.println(gson.toJson(o));
                }
            }else {
                System.out.println("chat1 is null");
            }

            String msg = chat2(demand,gson.toJson(list));
            System.out.println("最终的回答"+msg);

    }

    public String chat1(String demand) {
        //mock request
        ChatCompletionRequest chatCompletionRequest = new ChatCompletionRequest();
        chatCompletionRequest.setTemperature(0.8);
        chatCompletionRequest.setMax_tokens(1024);
        chatCompletionRequest.setCategory("default");
        ChatMessage message = new ChatMessage();
        message.setRole("user");
        message.setContent("现在你是一个数据分析师,MYSQL大神,请根据用户提供的表的信息，以及用户的需求，写出效率最高的SQL." +
                "表信息如下：表名:hotel_agreement； 字段:id主键，整型；tier表示档位，字符串类型；" +
                "province表示省份，字符串类型； city表示城市，字符串类型；" +
                "hotel_name表示酒店名称，字符串类型；star_rating表示星级， 字符串类型；" +
                "room_type表示房间类型，字符串类型；hotel_address表示酒店地址，字符串类型；" +
                "distance_from_group表示距离集团（公里）， 字符串类型；" +
                "unavailable_dates表示协议价不可用日期，字符串类型；contact_info表示酒店联系人/电话（预定方式），字符串类型；" +
                "data_source表示数据来源，字符串类型；applicable_brand表示适用品牌，字符串类型；" +
                "remarks表示备注，字符串类型；agreement_price2表示2024年协议价（元/间）， 字符串类型；" +
                "agreement_price3表示2024年协议价（元/间），字符串类型；agreement_price4表示2024年协议价（元/间），字符串类型。" +
                "输并且要求输出的S0L以#开头,以#结尾，样例如下:" +
                "{SELECT * FROM hotel_agreement;}  {SELECT COUNT(*)FROM hotel_agreement;}" +
                "注意不需要分析过程，" +
                "用户需求:" + demand
        );
        chatCompletionRequest.setMessages(Lists.newArrayList(message));
        // Set the stream parameter to false
        chatCompletionRequest.setStream(false);
        // Create an instance of CompletionsService
        CompletionsService completionsService = new CompletionsService();
        // Call the completions method to process the chat completion request
        ChatCompletionResult result = completionsService.completions(chatCompletionRequest);
        String out = null;
          if (result != null) {
              out = result.getChoices().get(0).getMessage().getContent();
          }
        return out;
    }

     public String chat2(String demand,String outMsg) {
        //mock request
        ChatCompletionRequest chatCompletionRequest = new ChatCompletionRequest();
        chatCompletionRequest.setTemperature(0.8);
        chatCompletionRequest.setMax_tokens(1024);
        chatCompletionRequest.setCategory("default");
        ChatMessage message = new ChatMessage();
        message.setRole("user");
        message.setContent("现在你是一个数据分析师,请根据用户提供的表的信息，用户的需求，以及你查询数据库返回的信息，给客户一个满意的回答." +
                "表信息如下：表名:hotel_agreement； 字段:id主键，整型；tier表示档位，字符串类型；" +
                "province表示省份 ；city表示城市；" +
                "hotel_name表示酒店名称；star_rating表示星级；" +
                "room_type表示房间类型；hotel_address表示酒店地址；" +
                "distance_from_group表示距离集团（公里）；" +
                "unavailable_dates表示协议价不可用的日期；contact_info表示酒店联系人/电话（预定方式）；" +
                "data_source表示数据来源；applicable_brand表示适用品牌；" +
                "remarks表示备注；agreement_price2表示2024年协议价（元/间）；" +
                "agreement_price3表示2024年协议价（元/间）；agreement_price4表示2024年协议价（元/间）。" +
                "用户需求:“" + demand+"”。" +
                "你结合需求查数据库返回的信息的信息是：“"+outMsg+
                "”注意不需要分析过程。");
        chatCompletionRequest.setMessages(Lists.newArrayList(message));
        // Set the stream parameter to false
        chatCompletionRequest.setStream(false);
        // Create an instance of CompletionsService
        CompletionsService completionsService = new CompletionsService();
        // Call the completions method to process the chat completion request
        ChatCompletionResult result = completionsService.completions(chatCompletionRequest);
        String out = null;
        if(result != null){
            out = result.getChoices().get(0).getMessage().getContent();
        }
        return out;
    }



    public static String extractContentWithinBraces(String input) {
       // input = input.replaceAll("\\s", "");
       // input = input.replace("，", ",");
        if (input == null || !input.contains("#") || !input.contains("#")) {
            return input;
        }
        if (input.matches(".*#.*#.*")) {
            int startIndex = input.indexOf("#");
            int endIndex = input.indexOf("#", startIndex + 1); // 从第一个#之后开始查找第二个#

             if (startIndex != -1 && endIndex != -1 && startIndex < endIndex) {
                return input.substring(startIndex + 1, endIndex);
            } else {
                System.out.println("只找到了一个 # 字符");
            }
        }
        //System.out.println("outcome:" + input);
        return input;
    }

    @Test
    public void mysql() {
        List<Map<String, Object>> list = new AiZindexUserDao().sqlToValue("SELECT * FROM hotel_agreement WHERE agreement_price4 BETWEEN 300 AND 500;");
        //System.out.println(list.toArray());
        Gson gson = new Gson();
        for (Object o : list) {
            System.out.println(gson.toJson(o));
        }
    }


    class AiZindexUserDao extends MysqlAdapter {
        /**
         * 查询
         *
         * @return
         */
            public List<Map<String,Object>> sqlToValue(String sql) {
            List<Map<String,Object>> list = select(sql);
            return list.size() > 0 && list != null ? list : new ArrayList<>();
        }
    }
}
