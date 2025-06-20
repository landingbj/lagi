package ai.image.adapter.impl;

import ai.annotation.Img2Text;
import ai.annotation.ImgGen;
import ai.common.ModelService;
import ai.common.pojo.*;
import ai.image.adapter.IImage2TextAdapter;
import ai.image.adapter.IImageGenerationAdapter;
import ai.utils.*;
import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import com.baidubce.qianfan.Qianfan;
import com.baidubce.qianfan.core.auth.Auth;
import com.baidubce.qianfan.model.image.*;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.ArrayList;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Img2Text(modelNames = "Fuyu-8B")
@ImgGen(modelNames = "Stable-Diffusion-XL")
public class BaiduImageAdapter extends ModelService implements IImage2TextAdapter, IImageGenerationAdapter {

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


    private final Logger logger = LoggerFactory.getLogger(BaiduImageAdapter.class);

    private Qianfan buildQianfan() {
        return new Qianfan(Auth.TYPE_OAUTH, apiKey, secretKey);
    }


    private Image2TextRequest convertImage2TextRequest(FileRequest param) {
        Image2TextRequest image2TextRequest = new Image2TextRequest();
        BeanUtil.copyProperties(param.getExtendParam(), image2TextRequest, "user_id");
        String fileContentAsBase64 = null;
        if(param.getImageUrl().startsWith("http")) {
            byte[] imgData = ImageUtil.getFileStream(param.getImageUrl());
            if(imgData != null) {
                fileContentAsBase64 = Base64Util.encode(imgData);
            }
        } else {
            try {
                fileContentAsBase64 = ImageUtil.getFileContentAsBase64(param.getImageUrl());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        if (StrUtil.isBlank(fileContentAsBase64)) {
            return null;
        }
        image2TextRequest.setImage(fileContentAsBase64);
        if(StrUtil.isBlank(image2TextRequest.getPrompt())) {
            image2TextRequest.setPrompt("introduce the picture");
        }
        return image2TextRequest;
    }

    private Text2ImageRequest convertText2ImageResponse(ImageGenerationRequest request) {
        Text2ImageRequest text2ImageRequest = new Text2ImageRequest();
        BeanUtil.copyProperties(request, text2ImageRequest);
        return text2ImageRequest;
    }

    @Override
    public ImageToTextResponse toText(FileRequest param) {
        try {
            Image2TextRequest image2TextRequest = convertImage2TextRequest(param);
            Image2TextResponse image2TextResponse = buildQianfan().image2Text(image2TextRequest);
            return ImageToTextResponse.success(image2TextResponse.getResult());
        } catch (Exception e) {
            logger.error("error", e);
        }
        return ImageToTextResponse.error();
    }

    @Override
    public ImageGenerationResult generations(ImageGenerationRequest request) {
        if (getAccessToken()==null){
            Qianfan qianfan = buildQianfan();
            Text2ImageRequest text2ImageRequest = convertText2ImageResponse(request);
            Text2ImageResponse text2ImageResponse = qianfan.text2Image(text2ImageRequest);
            List<ImageGenerationData> dataList = text2ImageResponse.getData().stream().map(d -> ImageGenerationData.builder().base64Image(d.getB64Image()).build()).collect(Collectors.toList());
            return ImageGenerationResult.builder().created(text2ImageResponse.getCreated()).dataType("base64").data(dataList).build();
        }else {
                String json = "{\"prompt\":\""+request.getPrompt()+"\",\"size\":\"1024x1024\",\"n\":1,\"steps\":20,\"sampler_index\":\"Euler a\"}";
                HttpResponse response = HttpRequest.post("https://aip.baidubce.com/rpc/2.0/ai_custom/v1/wenxinworkshop/text2image/sd_xl")
                        .header("Content-Type", "application/json")
                        .header("Accept", "application/json")
                        .header("Authorization", "Bearer "+getAccessToken())
                        .body(json)
                        .timeout(300 * 1000) // 设置超时为 5 分钟
                        .execute();
                try {
                    ObjectMapper objectMapper = new ObjectMapper();
                    String msg = response.body().toString();
                    JsonNode rootNode = objectMapper.readTree(msg);
                    JsonNode dataArrayNode = rootNode.get("data");
                    List<ImageGenerationData> dataList = new ArrayList<>();
                    if (dataArrayNode.isArray()) {
                        for (JsonNode dataNode : dataArrayNode) {
                            String base64Image = dataNode.get("b64_image").asText();
                            dataList.add(ImageGenerationData.builder().base64Image(base64Image).build());
                        }
                    }
                    return ImageGenerationResult.builder()
                            .created(System.currentTimeMillis() / 1000L)
                            .dataType("base64")
                            .data(dataList)
                            .build();
                }catch (Exception e){
                    logger.error("error", e);
                }
         return null;
        }
    }



}
