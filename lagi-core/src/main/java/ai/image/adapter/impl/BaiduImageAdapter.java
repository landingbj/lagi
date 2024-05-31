package ai.image.adapter.impl;

import ai.common.ModelService;
import ai.common.exception.RRException;
import ai.common.pojo.FileRequest;
import ai.common.pojo.ImageToTextResponse;
import ai.image.adapter.IImage2TextAdapter;
import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSON;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import lombok.Data;
import lombok.NoArgsConstructor;
import okhttp3.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Base64;
import java.util.List;

public class BaiduImageAdapter extends ModelService implements IImage2TextAdapter {

    private final Logger logger = LoggerFactory.getLogger(BaiduImageAdapter.class);
    private final String ACCESS_TOKEN_API = "https://aip.baidubce.com/oauth/2.0/token";
    private final String IMAGE_TO_TEXT_API = "https://aip.baidubce.com/rpc/2.0/ai_custom/v1/wenxinworkshop/image2text/fuyu_8b";
    private final OkHttpClient HTTP_CLIENT = new OkHttpClient().newBuilder().build();


    @NoArgsConstructor
    @Data
    static
    class Image2TextRequest  {
        private String prompt;
        private String image;
        private Boolean stream;
        private Double temperature;
        private Integer top_k;
        private Double top_p;
        private Double penalty_score;
        private List<String> stop;
        private String user_id;
    }


    @Override
    public ImageToTextResponse toText(FileRequest param) {
        try {
            Image2TextRequest image2TextRequest = new Image2TextRequest();
            BeanUtil.copyProperties(param.getExtendParam(), image2TextRequest, "user_id");
            image2TextRequest.setImage(getFileContentAsBase64(param.getImageUrl()));
            if(StrUtil.isBlank(image2TextRequest.prompt)) {
                image2TextRequest.setPrompt("detail");
            }
            MediaType mediaType = MediaType.parse("application/json");
            RequestBody body = RequestBody.create(mediaType, JSONUtil.toJsonStr(image2TextRequest));
            Request request = new Request.Builder()
                    .url(IMAGE_TO_TEXT_API + "?access_token=" + getAccessToken())
                    .method("POST", body)
                    .addHeader("Content-Type", "application/json")
                    .build();
            ResponseBody responseBody = HTTP_CLIENT.newCall(request).execute().body();
            if(responseBody != null) {
                JSON json = JSONUtil.parse(responseBody.string());
                if(json.getByPath("error_code", Integer.class) != null) {
                    throw new RRException(json.getByPath("error_msg", String.class));
                }
                String result = json.getByPath("result", String.class);
                System.out.println(json);
                return ImageToTextResponse.success(result);
            }
        } catch (Exception e) {
            logger.error("error", e);
        }
        return ImageToTextResponse.error();
    }


    private String getFileContentAsBase64(String path) throws IOException {
        byte[] b = Files.readAllBytes(Paths.get(path));
        return Base64.getEncoder().encodeToString(b);
    }



    private String getAccessToken() throws IOException {
        MediaType mediaType = MediaType.parse("application/x-www-form-urlencoded");
        RequestBody body = RequestBody.create(mediaType, "grant_type=client_credentials&client_id=" + apiKey
                + "&client_secret=" + secretKey);
        Request request = new Request.Builder()
                .url(ACCESS_TOKEN_API)
                .method("POST", body)
                .addHeader("Content-Type", "application/x-www-form-urlencoded")
                .build();
        Response response = HTTP_CLIENT.newCall(request).execute();
        return new JSONObject(response.body().string()).getStr("access_token");
    }

    public static void main(String[] args) {
        BaiduImageAdapter baiduImageAdapter = new BaiduImageAdapter();
        FileRequest build = FileRequest.builder().imageUrl("C:\\Users\\Administrator\\Desktop\\dog1.jpg").build();
        ImageToTextResponse text = baiduImageAdapter.toText(build);
        System.out.println(text);
    }

}
