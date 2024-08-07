package ai.ocr.impl;

import ai.annotation.OCR;
import ai.common.ModelService;
import ai.ocr.IOcr;
import ai.ocr.pojo.BaiduOcrDocument;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import okhttp3.*;
import org.json.JSONObject;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.URLEncoder;
import java.util.Base64;
import java.util.List;

@OCR(company = "baidu", modelNames = "ocr")
public class BaiduOcrAdapter extends ModelService implements IOcr {
    private static final OkHttpClient HTTP_CLIENT = new OkHttpClient().newBuilder().build();

    @Override
    public boolean verify() {
        if(getApiKey() == null || getApiKey().startsWith("you")) {
            return false;
        }
        if(getSecretKey() == null || getSecretKey().startsWith("you")) {
            return false;
        }
        return true;
    }

    public String recognize(BufferedImage image) {
        String result;
        MediaType mediaType = MediaType.parse("application/x-www-form-urlencoded");
        String base64Image = convertBufferedImageToBase64(image, true);
        RequestBody body = RequestBody.create("image=" + base64Image +
                "&detect_direction=true&detect_language=true&paragraph=true&probability=false", mediaType);
        try {
            Request request = new Request.Builder()
                    .url("https://aip.baidubce.com/rest/2.0/ocr/v1/general_basic?access_token=" + getToken())
                    .method("POST", body)
                    .addHeader("Content-Type", "application/x-www-form-urlencoded")
                    .addHeader("Accept", "application/json")
                    .build();
            Response response = HTTP_CLIENT.newCall(request).execute();
            result = response.body().string();
            result = toFormatedText(result);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return result;
    }

    public String toFormatedText(String text) throws IOException {
        StringBuilder sb = new StringBuilder();
        ObjectMapper mapper = new ObjectMapper();
        BaiduOcrDocument doc = mapper.readValue(text, BaiduOcrDocument.class);
        List<BaiduOcrDocument.WordResult> wordsResult = doc.getWords_result();
        List<BaiduOcrDocument.ParagraphResult> paragraphsResult = doc.getParagraphs_result();
        for (BaiduOcrDocument.ParagraphResult paragraphResult : paragraphsResult) {
            List<Integer> wordsResultIdx = paragraphResult.getWords_result_idx();
            for (Integer idx : wordsResultIdx) {
                sb.append(wordsResult.get(idx).getWords());
            }
            sb.append("\n");
        }
        return sb.toString();
    }

    private String convertBufferedImageToBase64(BufferedImage image, boolean urlEncode) {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        try {
            ImageIO.write(image, "png", outputStream);
            byte[] imageBytes = outputStream.toByteArray();
            String base64String = Base64.getEncoder().encodeToString(imageBytes);
            if (urlEncode) {
                return URLEncoder.encode(base64String, "UTF-8");
            } else {
                return base64String;
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private String getToken() throws IOException {
        MediaType mediaType = MediaType.parse("application/x-www-form-urlencoded");
        String apiKey = getApiKey();
        String secretKey = getSecretKey();
        RequestBody body = RequestBody.create(mediaType, "grant_type=client_credentials&client_id=" + apiKey
                + "&client_secret=" + secretKey);
        Request request = new Request.Builder()
                .url("https://aip.baidubce.com/oauth/2.0/token")
                .method("POST", body)
                .addHeader("Content-Type", "application/x-www-form-urlencoded")
                .build();
        Response response = HTTP_CLIENT.newCall(request).execute();
        JSONObject jsonObject = new JSONObject(response.body().string());
        return jsonObject.getString("access_token");
    }
}
