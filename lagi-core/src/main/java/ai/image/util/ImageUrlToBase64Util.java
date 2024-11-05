package ai.image.util;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.Base64;

public class ImageUrlToBase64Util {

    public static String imageUrlToBase64(String imageUrl) {
        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder()
                .url(imageUrl)
                .build();

        try (Response response = client.newCall(request).execute()) {
            if (response.isSuccessful()) {
                InputStream inputStream = response.body().byteStream();
                byte[] imageBytes = toByteArray(inputStream);
                return Base64.getEncoder().encodeToString(imageBytes);
            } else {
                throw new RuntimeException("Failed to download image: " + response.code());
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Failed to convert image URL to Base64", e);
        }
    }

    private static byte[] toByteArray(InputStream inputStream) throws Exception {
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        int nRead;
        byte[] data = new byte[1024];
        while ((nRead = inputStream.read(data, 0, data.length)) != -1) {
            buffer.write(data, 0, nRead);
        }
        buffer.flush();
        return buffer.toByteArray();
    }
}
