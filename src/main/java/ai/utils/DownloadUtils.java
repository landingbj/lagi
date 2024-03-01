package ai.utils;

import java.io.File;
import java.io.IOException;
import java.util.UUID;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;
import okio.BufferedSink;
import okio.BufferedSource;
import okio.Okio;




public class DownloadUtils {
	public static WhisperResponse downloadFile(String url,String type,String savePath){

        String imageUrl = url;
        String filename = UUID.randomUUID()+"."+type;
        savePath =savePath  +filename;

        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder()
                .url(imageUrl)
                .build();
        WhisperResponse whisperResponse = null;
        try (Response response = client.newCall(request).execute()) {
            if (response.isSuccessful()) {
                // 获取响应体
                ResponseBody responseBody = response.body();

                if (responseBody != null) {
                    // 创建本地文件
                    File localFile = new File(savePath);

                    // 将响应体写入本地文件
                    try (BufferedSource source = responseBody.source();
                         BufferedSink sink = Okio.buffer(Okio.sink(localFile))) {
                        sink.writeAll(source);
                    }
                    whisperResponse = new WhisperResponse(1,filename);

                } else {
                    whisperResponse = new WhisperResponse(0,filename);
                }
            } else {
                whisperResponse = new WhisperResponse(0,filename);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return whisperResponse;
    }
	
	public static WhisperResponse downloadVoiceFile(String url,String type,String savePath,String uuid){

        String imageUrl = url;
        String filename = uuid;
        savePath =savePath  +filename;

        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder()
                .url(imageUrl)
                .build();
        WhisperResponse whisperResponse = null;
        try (Response response = client.newCall(request).execute()) {
            if (response.isSuccessful()) {
                // 获取响应体
                ResponseBody responseBody = response.body();

                if (responseBody != null) {
                    // 创建本地文件
                    File localFile = new File(savePath);

                    // 将响应体写入本地文件
                    try (BufferedSource source = responseBody.source();
                         BufferedSink sink = Okio.buffer(Okio.sink(localFile))) {
                        sink.writeAll(source);
                    }
                    whisperResponse = new WhisperResponse(1,filename);

                } else {
                    whisperResponse = new WhisperResponse(0,filename);
                }
            } else {
                whisperResponse = new WhisperResponse(0,filename);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return whisperResponse;
    }
}
