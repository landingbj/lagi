package ai.video.adapter.impl;

import ai.annotation.Img2Video;
import ai.annotation.VideoEnhance;
import ai.common.ModelService;
import ai.common.exception.RRException;
import ai.oss.UniversalOSS;
import ai.video.adapter.Video2EnhanceAdapter;
import ai.video.pojo.*;
import ai.video.adapter.Image2VideoAdapter;
import cn.hutool.core.bean.BeanUtil;
import cn.hutool.json.JSONUtil;
import com.aliyun.tea.TeaException;
import com.aliyun.teaopenapi.models.Config;
import com.aliyun.videoenhan20200320.Client;
import com.aliyun.videoenhan20200320.models.*;
import com.google.common.collect.Maps;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.net.URL;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@VideoEnhance(modelNames = "vision")
@Img2Video(modelNames = "vision")
public class AlibabaVisionAdapter extends ModelService implements Image2VideoAdapter, Video2EnhanceAdapter {

    @Override
    public boolean verify() {
        if(getAccessKeyId() == null || getAccessKeyId().startsWith("you")) {
            return false;
        }
        if(getAccessKeySecret() == null || getAccessKeySecret().startsWith("you")) {
            return false;
        }
        return true;
    }

    private final Logger log = LoggerFactory.getLogger(AlibabaVisionAdapter.class);

    private UniversalOSS universalOSS;

    public Client createClient() {
        try {
            Config config = new Config()
                    .setAccessKeyId(accessKeyId)
                    .setAccessKeySecret(accessKeySecret);
            // Endpoint 请参考 https://api.aliyun.com/product/videoenhan
            config.endpoint = "videoenhan.cn-shanghai.aliyuncs.com";
            return new Client(config);
        } catch (Exception e) {
            log.error(e.getMessage());
        }
        return null;
    }

    private GenerateVideoRequest convert2GenerateVideoRequest(VideoGeneratorRequest videoGeneratorRequest) {
        if(videoGeneratorRequest.getInputFileList() == null) {
            throw new RRException("no file specified");
        }
        GenerateVideoRequest generateVideoRequest = new GenerateVideoRequest();
        List<GenerateVideoRequest.GenerateVideoRequestFileList> collect = videoGeneratorRequest.getInputFileList().stream().map(i ->{
            GenerateVideoRequest.GenerateVideoRequestFileList generateVideoRequestFileList = new GenerateVideoRequest.GenerateVideoRequestFileList();
            generateVideoRequestFileList.setType("image");
            if(isNetworkPath(i.getUrl())) {
                generateVideoRequestFileList.setFileUrl(i.getUrl());
                generateVideoRequestFileList.setFileName(UUID.randomUUID() + ".jpg");
            } else {
                File file = new File(i.getUrl());
                String s = universalOSS.upload("genVideo/" + file.getName(), file);
                generateVideoRequestFileList.setFileUrl(s);
                generateVideoRequestFileList.setFileName(file.getName());
            }
            return generateVideoRequestFileList;
        }).collect(Collectors.toList());
        generateVideoRequest.setFileList(collect);
        OutputVideoProperties outputVideoProperties = videoGeneratorRequest.getOutputVideoProperties();
        if(outputVideoProperties != null) {
            generateVideoRequest.setDuration(outputVideoProperties.getDuration());
            generateVideoRequest.setWidth(outputVideoProperties.getWidth());
            generateVideoRequest.setHeight(outputVideoProperties.getHeight());
            generateVideoRequest.setDurationAdaption(outputVideoProperties.getDurationAdaption());
        } else {
            generateVideoRequest.setDuration(5.0f);
            generateVideoRequest.setWidth(360);
            generateVideoRequest.setHeight(360);
        }
        return generateVideoRequest;
    }

    public boolean isNetworkPath(String path) {
        try {
            URL url = new URL(path);
            String protocol = url.getProtocol();
            return protocol.equals("http") || protocol.equals("https");
        } catch (Exception e) {
            return false;
        }
    }

    private VideoJobResponse wait2Result(String requestId) {
        int limit = 50;
        while (limit > 0) {
            limit --;
            VideoJobQueryResponse query = query(requestId);
            if(query.getStatus() == 1) {
                try {
                    Thread.sleep(1000);
                } catch (Exception ignored) {
                }
            }else if(query.getStatus() == 2) {
                if(query.getVideoUrl() == null) {
                    try {
                        Thread.sleep(1000);
                    } catch (Exception ignored) {
                    }
                    continue;
                }
                return VideoJobResponse.builder().jobId(requestId).data(query.getVideoUrl()).build();
            }
            else {
                break;
            }
        }
        return null;
    }

    @Override
    public VideoJobResponse image2Video(VideoGeneratorRequest videoGeneratorRequest) {
        Client client = createClient();
        GenerateVideoRequest generateVideoRequest = convert2GenerateVideoRequest(videoGeneratorRequest);
        try {
            GenerateVideoResponse generateVideoResponse = client.generateVideo(generateVideoRequest);
            String requestId = generateVideoResponse.getBody().getRequestId();
            if(requestId != null) {
                return wait2Result(requestId);
            }
            return VideoJobResponse.builder().jobId(generateVideoResponse.getBody().getRequestId()).build();
        } catch (TeaException error) {
            log.error(error.getMessage());
        } catch (Exception _error) {
            TeaException error = new TeaException(_error.getMessage(), _error);
            log.error(error.getMessage());
        }
        return null;
    }


    private VideoJobQueryResponse convertToVideoJobQueryResponse(GetAsyncJobResultResponse asyncJobResultWithOptions) {
        GetAsyncJobResultResponseBody.GetAsyncJobResultResponseBodyData data = asyncJobResultWithOptions.getBody().getData();
        String status =data.getStatus();
        Map<String, Integer> statusMap = Maps.newHashMap();
        statusMap.put("QUEUING", 0);
        statusMap.put("PROCESSING", 1);
        statusMap.put("PROCESS_SUCCESS", 2);
        statusMap.put("PROCESS_FAILED", 3);
        statusMap.put("TIMEOUT_FAILED", 4);
        statusMap.put("LIMIT_RETRY_FAILED", 5);
        VideoJobQueryResponse videoJobQueryResponse = new VideoJobQueryResponse();
        videoJobQueryResponse.setStatus(statusMap.get(status));
        videoJobQueryResponse.setTaskId(data.getJobId());
        if(statusMap.get(status) == 2) {
            String videoUrl = JSONUtil.parse(data.getResult()).getByPath("VideoUrl", String.class);
            if(videoUrl == null) {
                videoUrl = JSONUtil.parse(data.getResult()).getByPath("videoUrl", String.class);
            }
            videoJobQueryResponse.setVideoUrl(videoUrl);
        }
        return videoJobQueryResponse;
    }


    public VideoJobQueryResponse query(String jobId) {
        Client client = createClient();
        GetAsyncJobResultRequest getAsyncJobResultRequest = new GetAsyncJobResultRequest();
        getAsyncJobResultRequest.setJobId(jobId);
        try {
            GetAsyncJobResultResponse asyncJobResultWithOptions = client.getAsyncJobResult(getAsyncJobResultRequest);
            return convertToVideoJobQueryResponse(asyncJobResultWithOptions);
        } catch (TeaException error) {
            log.error(error.getMessage());
        } catch (Exception _error) {
            TeaException error = new TeaException(_error.getMessage(), _error);
            log.error(error.getMessage());
        }
        return null;
    }

    private EnhanceVideoQualityRequest convert2EnhanceVideoQualityRequest(VideoEnhanceRequest videoEnhanceRequest) {
        EnhanceVideoQualityRequest enhanceVideoQualityRequest = new EnhanceVideoQualityRequest();
        BeanUtil.copyProperties(videoEnhanceRequest, enhanceVideoQualityRequest);
        if(!isNetworkPath(enhanceVideoQualityRequest.getVideoURL())) {
            File file = new File(enhanceVideoQualityRequest.getVideoURL());
            String s = universalOSS.upload("enhanceVideo/" + file.getName(), file);
            enhanceVideoQualityRequest.setVideoURL(s);
        }
        return enhanceVideoQualityRequest;
    }

    @Override
    public VideoJobResponse enhance(VideoEnhanceRequest videoEnhanceRequest) {
        Client client = createClient();
        EnhanceVideoQualityRequest enhanceVideoQualityRequest =convert2EnhanceVideoQualityRequest(videoEnhanceRequest);
        try {
            EnhanceVideoQualityResponse enhanceVideoQualityResponse = client.enhanceVideoQuality(enhanceVideoQualityRequest);
            return wait2Result(enhanceVideoQualityResponse.getBody().getRequestId());
        } catch (TeaException error) {
            log.error(error.getMessage());
        } catch (Exception _error) {
            TeaException error = new TeaException(_error.getMessage(), _error);
            log.error(error.getMessage());
        }
        return null;
    }

}
