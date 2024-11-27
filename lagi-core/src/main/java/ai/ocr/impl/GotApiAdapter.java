package ai.ocr.impl;

import ai.common.ModelService;
import ai.ocr.IOcr;
import com.google.gson.Gson;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import okhttp3.*;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.List;

@Slf4j
public class GotApiAdapter extends ModelService implements IOcr {

    private Gson gson = new Gson();

    private String getRecognizeUrl() {
        return getEndpoint() +  "/recognize";
    }

    @Override
    public String recognize(BufferedImage image) {
        try {
            File file = write2File(image);
            return recognize(file);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public String recognize(BufferedImage image, List<String> languages) {
        OkHttpClient client = new OkHttpClient();
        File tempFile = null;
        try {
            tempFile = File.createTempFile("tempImage", ".png");
            boolean result = ImageIO.write(image, "png", tempFile);
            if (!result) {
                RequestBody fileBody = RequestBody.create(MediaType.parse("image/png"), tempFile);
                RequestBody requestBody = new MultipartBody.Builder()
                        .setType(MultipartBody.FORM)
                        .addPart(Headers.of("Content-Disposition", "form-data; name=\"file\"; filename=\"" + tempFile.getName() + "\""),
                                fileBody)
                        .addPart(Headers.of("Content-Disposition", "form-data; name=\"lans\""),
                                RequestBody.create(MediaType.parse("text/plain"), String.join(",", languages)))
                .build();
                Request request = new Request.Builder()
                        .url(getRecognizeUrl())
                        .post(requestBody)
                        .build();
                try (Response response = client.newCall(request).execute()) {
                    if (response.isSuccessful()) {
                        IResult iResult = gson.fromJson(response.body().string(), IResult.class);
                        if(!iResult.getStatus().equals("success")) {
                            throw new RuntimeException("识别图片文字失败.");
                        }
                        return iResult.getResult();
                    } else {
                        throw new RuntimeException("识别图片文字失败..");
                    }
                } catch (Exception e) {
                    throw new RuntimeException("识别图片文字失败...");
                }
            }
        } catch (IOException e) {
            throw new RuntimeException("识别图片文字失败....");
        } finally {
            // 删除临时文件（可选）
            if (tempFile != null) {
                tempFile.deleteOnExit();
            }
        }
        throw new RuntimeException("识别图片文字失败.....");
    }

    private String recognize(File file) {
        OkHttpClient client = new OkHttpClient();
        RequestBody fileBody = RequestBody.create(MediaType.parse("image/png"), file);
        RequestBody requestBody = new MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addPart(Headers.of("Content-Disposition", "form-data; name=\"file\"; filename=\"" + file.getName() + "\""),
                        fileBody)
                .build();
        Request request = new Request.Builder()
                .url(getRecognizeUrl())
                .post(requestBody)
                .build();
        try (Response response = client.newCall(request).execute()) {
            if (response.isSuccessful()) {
                IResult iResult = gson.fromJson(response.body().string(), IResult.class);
                if(!iResult.getStatus().equals("success")) {
                    throw new RuntimeException("识别图片文字失败");
                }
                return iResult.getResult();
            } else {
                throw new RuntimeException("识别图片文字失败");
            }
        } catch (Exception e) {
            throw new RuntimeException("识别图片文字失败");
        }
    }

    private File write2File(BufferedImage image) throws Exception {
        // 获取系统临时目录
        String tempDirPath = System.getProperty("java.io.tmpdir");
        File tempDir = new File(tempDirPath);
        // 创建临时文件
        File tempFile;
        try {
            tempFile = File.createTempFile("tempImage", ".png", tempDir);
            System.out.println("tempFile = " + tempFile.getAbsoluteFile());
            // 写入图像到临时文件
            ImageIO.write(image, "png", tempFile);
        } catch (IOException e) {
            log.error("无法创建或写入临时文件", e);
            throw new Exception("无法创建或写入临时文件");
        }
        return tempFile;
    }

    @Data
    static
    class IResult {
        private String status;
        private String result;
    }

}
