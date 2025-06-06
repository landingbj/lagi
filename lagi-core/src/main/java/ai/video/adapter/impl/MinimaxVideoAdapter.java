package ai.video.adapter.impl;

import ai.annotation.Img2Video;
import ai.annotation.Text2Video;
import ai.common.ModelService;
import ai.common.pojo.ImageGenerationRequest;
import ai.video.adapter.Image2VideoAdapter;
import ai.video.adapter.Text2VideoAdapter;
import ai.video.pojo.InputFile;
import ai.video.pojo.VideoGeneratorRequest;
import ai.video.pojo.VideoJobResponse;
import com.google.common.collect.Lists;
import org.json.JSONObject;

import javax.net.ssl.HttpsURLConnection;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Base64;


@Img2Video(modelNames = "I2V-01-Director")
@Text2Video(modelNames = "T2V-01-Director")
public class MinimaxVideoAdapter extends ModelService implements Image2VideoAdapter,Text2VideoAdapter {

    @Override
    public VideoJobResponse image2Video(VideoGeneratorRequest videoGeneratorRequest) {
        String urlString = "https://api.minimax.chat/v1/video_generation";

        File file = new File(videoGeneratorRequest.getInputFileList().get(0).getUrl());
        try {
            String base64Image = encodeImageToBase64(file.getPath());
            String payload = "{"
                    + "\"model\": \""+getModel()+"\","
                    + "\"prompt\": \"让图片中的物体动起来,核心主体要会唱会跳，还会打篮球,要求生成的视频足够精美，" +
                    "细腻，震撼，动态美，创意独特，色彩丰富，流畅，质感强烈，视觉冲击力强，音乐配合恰当\","
                    + "\"first_frame_image\": \"data:image/jpeg;base64," + base64Image + "\""
                    + "}";

            // 发送请求
            String response = sendPostRequest(urlString, getApiKey() , payload);
            System.out.println(response);
            VideoJobResponse videoJobResponse = new VideoJobResponse();
            videoJobResponse.setStatus("success");
            JSONObject responseJson = new JSONObject(response.toString());
            JSONObject baseResp = responseJson.getJSONObject("base_resp");
            int statusCode = baseResp.getInt("status_code");
            if (statusCode==0){
                String taskId = responseJson.getString("task_id");
                videoJobResponse.setJobId(taskId);
                while (true) {
                    //276992038994141
                    //277003532169484，277000239943963,277079472730394
                    String[] result = queryVideoGeneration(taskId);
                    String fileId = result[0];
                    String status = result[1];

                    if (!fileId.isEmpty()) {
                        String outUrlString = fetchVideoResult(fileId);
                        videoJobResponse.setData(outUrlString);
                        System.out.println("---------------生成成功---------------");
                        return videoJobResponse;
                    } else if ("Fail".equals(status) || "Unknown".equals(status)) {
                        System.out.println("---------------生成失败---------------"+result[2]);
                        return null;
                    }
                    Thread.sleep(3000);
                }
            }else if(statusCode==2013){
                System.out.println("任务失败"+"原因："+response);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public VideoJobResponse toVideo(ImageGenerationRequest request) {
        try {
            String urlString = "https://api.minimax.chat/v1/video_generation";
            URL url = new URL(urlString);

            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Content-Type", "application/json");
            connection.setRequestProperty("Authorization", "Bearer "+getApiKey() );
            connection.setDoOutput(true);

            String jsonInputString = "{"
                    + "\"model\": \"" + getModel() + "\","
                    + "\"prompt\": \"" + request.getPrompt() + "\""
                    + "}";

            // 写入请求体
            try (OutputStream os = connection.getOutputStream()) {
                byte[] input = jsonInputString.getBytes(StandardCharsets.UTF_8);
                os.write(input, 0, input.length);
            }

            // 获取响应
            int statusCode = connection.getResponseCode();
            VideoJobResponse videoJobResponse = new VideoJobResponse();

            if (statusCode != HttpURLConnection.HTTP_OK) {
                throw new IOException("HTTP request failed with status code " + statusCode);
            }
                try (BufferedReader br = new BufferedReader(new InputStreamReader(connection.getInputStream(), StandardCharsets.UTF_8))) {
                    StringBuilder response = new StringBuilder();
                    String responseLine;
                    while ((responseLine = br.readLine()) != null) {
                        response.append(responseLine.trim());
                    }
                    videoJobResponse.setStatus("success");
                    JSONObject responseJson = new JSONObject(response.toString());
                    String taskId = responseJson.getString("task_id");
                    videoJobResponse.setJobId(taskId);
                }
            while (true) {
                //276711775527187
                String[] result = queryVideoGeneration(videoJobResponse.getJobId());
                String fileId = result[0];
                String status = result[1];

                if (!fileId.isEmpty()) {
                    String outUrlString = fetchVideoResult(fileId);
                    videoJobResponse.setData(outUrlString);
                    System.out.println("---------------生成成功---------------");
                    return videoJobResponse;
                } else if ("Fail".equals(status) || "Unknown".equals(status)) {
                    System.out.println("---------------生成失败---------------");
                    return null;
                }
                Thread.sleep(3000);
            }
        }catch (Exception e){
            e.printStackTrace();
        }
        return null;
    }

    private static String encodeImageToBase64(String filePath) throws IOException {
        File file = new File(filePath);
        byte[] fileContent = new byte[(int) file.length()];
        try (FileInputStream fileInputStream = new FileInputStream(file)) {
            fileInputStream.read(fileContent);
        }
        return Base64.getEncoder().encodeToString(fileContent);
    }
    // 发送POST请求
    private static String sendPostRequest(String urlString, String apiKey, String payload) throws IOException {
        URL url = new URL(urlString);
        HttpsURLConnection connection = (HttpsURLConnection) url.openConnection();
        connection.setRequestMethod("POST");
        connection.setRequestProperty("Authorization", "Bearer " + apiKey);
        connection.setRequestProperty("Content-Type", "application/json");
        connection.setDoOutput(true);

        // 写入请求体
        try (OutputStream os = connection.getOutputStream()) {
            byte[] input = payload.getBytes(StandardCharsets.UTF_8);
            os.write(input, 0, input.length);
        }

        // 获取响应
        StringBuilder response = new StringBuilder();
        try (BufferedReader br = new BufferedReader(new InputStreamReader(connection.getInputStream(), StandardCharsets.UTF_8))) {
            String responseLine;
            while ((responseLine = br.readLine()) != null) {
                response.append(responseLine.trim());
            }
        }

        return response.toString();
    }


    public String[] queryVideoGeneration(String taskId) throws IOException {
        String urlString = "https://api.minimax.chat/v1/query/video_generation?task_id=" + taskId;
        URL url = new URL(urlString);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("GET");
        conn.setRequestProperty("Authorization", "Bearer " + getApiKey());

        int statusCode = conn.getResponseCode();
        if (statusCode == HttpURLConnection.HTTP_OK) {
            try (BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()))) {
                String inputLine;
                StringBuilder response = new StringBuilder();
                while ((inputLine = in.readLine()) != null) {
                    response.append(inputLine);
                }

                JSONObject responseJson = new JSONObject(response.toString());
                String status = responseJson.getString("status");
                if ("Success".equals(status)) {
                    String fileId = responseJson.getString("file_id");
                    return new String[]{fileId, "Finished"};
                } else {
                    return new String[]{"", status,responseJson.toString()};
                }
            }
        } else {
            throw new IOException("请求失败，状态码：" + statusCode);
        }
    }

    public String fetchVideoResult(String fileId) throws IOException {

        String urlString = "https://api.minimax.chat/v1/files/retrieve?file_id=" + fileId;
        URL url = new URL(urlString);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("GET");
        conn.setRequestProperty("Authorization", "Bearer " + getApiKey());

        int statusCode = conn.getResponseCode();
        if (statusCode == HttpURLConnection.HTTP_OK) {
            try (BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()))) {
                String inputLine;
                StringBuilder response = new StringBuilder();
                while ((inputLine = in.readLine()) != null) {
                    response.append(inputLine);
                }

                JSONObject responseJson = new JSONObject(response.toString());
                String downloadUrl = responseJson.getJSONObject("file").getString("download_url");
                return downloadUrl;
            }
        } else {
            throw new IOException("请求失败，状态码：" + statusCode);
        }
    }

    /**
     * 下载至本地版
     * @param fileId
     * @throws IOException
     */
    public void fetchVideoResult1(String fileId) throws IOException {
        String OUTPUT_FILE_NAME = "D:\\kaiyuan\\tts-ws-java\\src\\main\\resources\\output.mp4"; // 请在此输入生成视频的保存路径
        System.out.println("---------------视频生成成功，下载中---------------");

        String urlString = "https://api.minimax.chat/v1/files/retrieve?file_id=" + fileId;
        URL url = new URL(urlString);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("GET");
        conn.setRequestProperty("Authorization", "Bearer " + getApiKey());

        int statusCode = conn.getResponseCode();
        if (statusCode == HttpURLConnection.HTTP_OK) {
            try (BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()))) {
                String inputLine;
                StringBuilder response = new StringBuilder();
                while ((inputLine = in.readLine()) != null) {
                    response.append(inputLine);
                }

                JSONObject responseJson = new JSONObject(response.toString());
                String downloadUrl = responseJson.getJSONObject("file").getString("download_url");
                System.out.println("视频下载链接：" + downloadUrl);
                try (InputStream inputStream = new URL(downloadUrl).openStream()) {
                    Files.copy(inputStream, Paths.get(OUTPUT_FILE_NAME));
                }
                System.out.println("已下载在：" + new File(OUTPUT_FILE_NAME).getAbsolutePath());
            }
        } else {
            throw new IOException("请求失败，状态码：" + statusCode);
        }
    }
    public static void main(String[] args) {
        // 示例调用
        try {


            MinimaxVideoAdapter adapter = new MinimaxVideoAdapter();
//            ImageGenerationRequest request = new ImageGenerationRequest();
//            request.setModel("T2V-01-Director");
//            request.setPrompt("帮我生成小鸡啄米的视频");
//
//            VideoJobResponse response = adapter.toVideo(request);
//            System.out.println(response);
            InputFile file = new InputFile();
            file.setName("VCG211371648512.webp");
            file.setUrl("C:\\Users\\ruiqing.luo\\Desktop\\VCG211371648512.webp");
            file.setType("webp");

            VideoGeneratorRequest videoGeneratorRequest = new  VideoGeneratorRequest();
            videoGeneratorRequest.setModel("I2V-01-Director");
            videoGeneratorRequest.setInputFileList(Arrays.asList(file));
            videoGeneratorRequest.setIntPutText(Lists.newArrayList("aaaa"));
            VideoJobResponse response = adapter.image2Video(videoGeneratorRequest);
            System.out.println(response);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
