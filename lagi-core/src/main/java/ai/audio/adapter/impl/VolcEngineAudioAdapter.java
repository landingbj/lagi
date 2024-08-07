package ai.audio.adapter.impl;

import ai.annotation.TTS;
import ai.audio.adapter.IAudioAdapter;
import ai.audio.adapter.IAudioCloneAdapter;
import ai.audio.pojo.*;
import ai.common.ModelService;
import ai.common.exception.RRException;
import ai.common.pojo.AsrResult;
import ai.common.pojo.AudioRequestParam;
import ai.common.pojo.TTSRequestParam;
import ai.common.pojo.TTSResult;
import ai.oss.UniversalOSS;
import ai.utils.Base64Util;
import ai.utils.LagiGlobal;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.google.common.io.Files;
import okhttp3.*;
import org.apache.commons.io.FilenameUtils;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;


@TTS(company = "volcengine", modelNames = "speech_synthesis")
public class VolcEngineAudioAdapter extends ModelService implements IAudioAdapter, IAudioCloneAdapter {

    private final Logger log = LoggerFactory.getLogger(VolcEngineAudioAdapter.class);

    private final String HOST = "openspeech.bytedance.com";

    private final String TTS_URL = "https://" + HOST + "/api/v1/tts";

    private final String TRAIN_UPLOAD_URL = "https://" + HOST + "/api/v1/mega_tts/audio/upload";

    private final String TRAIN_STATUS_URL = "https://" + HOST + "/api/v1/mega_tts/status";

    private final String ttsClusterId = "volcano_tts";

    private final String cloneClusterId = "volcano_mega";

    private final String userId = "388808087185088";

//    private UniversalOSS universalOSS = new LandingOSS();
    private UniversalOSS universalOSS;

    @Override
    public boolean verify() {
        if(getAccessToken() == null || getAccessToken().startsWith("you")) {
            return false;
        }
        return true;
    }


    private VolcTtsRequest convert(TTSRequestParam param) {
        String clusterId = ttsClusterId;
        if("clone".equals(param.getSource())) {
            clusterId = cloneClusterId;
        }

        String voiceType = StrUtil.isBlank(param.getVoice()) ? "BV406_V2_streaming" : param.getVoice();
        float speedRatio = param.getSpeech_rate() == null ? 1.0f : param.getSpeech_rate();
        int pitchRatio = param.getPitch_rate() == null ? 10 : param.getPitch_rate();
        int volumeRatio = param.getVolume() == null ? 10 : param.getVolume();
        String emotion = StrUtil.isBlank(param.getEmotion()) ? "happy" : param.getEmotion();
        String encoding = StrUtil.isBlank(param.getFormat()) ?  "mp3" : param.getFormat();
        VolcApp app = VolcApp.builder().appid(appId).token(accessToken).cluster(clusterId).build();
        VolcUser user = VolcUser.builder().uid(userId).build();
        VolcRequest request = VolcRequest.builder().text(param.getText()).reqid(UUID.randomUUID().toString()).text_type("plain").operation("query").build();
        VolcAudio audio = VolcAudio.builder().emotion(emotion).voice_type(voiceType).encoding(encoding)
                .speedRatio(speedRatio)
                .pitchRatio(pitchRatio)
                .volumeRatio(volumeRatio).build();
        return VolcTtsRequest.builder().app(app).user(user).request(request).audio(audio).build();
    }

    @Override
    public AsrResult asr(File audio, AudioRequestParam param) {
        return null;
    }

    @Override
    public TTSResult tts(TTSRequestParam param) {
        OkHttpClient client = new OkHttpClient();
        VolcTtsRequest ttsRequest =  convert(param);
        RequestBody body = RequestBody.create(JSONUtil.toJsonStr(ttsRequest), MediaType.get("application/json; charset=utf-8"));
        Request request = new Request.Builder()
                .url(TTS_URL)
                .post(body)
                .header("Authorization", "Bearer; " + accessToken)
                .build();
        try (Response response = client.newCall(request).execute()) {
            if(response.body() == null) {
                return null;
            }
            String string = response.body().string();
            log.info(string);
            VolcTtsResponse bean = JSONUtil.toBean(string, VolcTtsResponse.class);
            if(bean.getCode() != 3000) {
                throw new RRException("volc tts  error :" + bean.getMessage());
            }
            String base64Audio = bean.getData();
            String tempFilePath = Files.createTempDir().getAbsolutePath() + File.separator + bean.getReqid() + "." + ttsRequest.getAudio().getEncoding();
            File file = Base64Util.toFile(tempFilePath, base64Audio);
            String url = universalOSS.upload("tts/" + file.getName(), file);
            TTSResult ttsResult = new TTSResult();
            ttsResult.setResult(url);
            ttsResult.setStatus(LagiGlobal.TTS_STATUS_SUCCESS);
            return ttsResult;
        } catch (IOException e) {
            log.error(e.getMessage());
        }
        return null;
    }

    private VolcAudioTrainRequest convert(UploadRequest uploadRequest) {
        List<VolcAudioTrain> audios = uploadRequest.getAudioTrains().stream()
                .map(a -> VolcAudioTrain.builder()
                        .audio_bytes(Base64Util.fileToBase64(a.getFile().getAbsolutePath()))
                        .text(a.getText())
                        .audio_format(FilenameUtils.getExtension(a.getFile().getName()))
                        .build())
                .collect(Collectors.toList());
        return VolcAudioTrainRequest.builder().appid(appId).speaker_id(uploadRequest.getSpeakerId()).source(2).audios(audios).build();
    }

    @Override
    public void upload(UploadRequest uploadRequest) {
        OkHttpClient client = new OkHttpClient();
        VolcAudioTrainRequest trainRequest = convert(uploadRequest);
        String trainBody = JSONUtil.toJsonStr(trainRequest);
        Request request = getRequest(trainBody, TRAIN_UPLOAD_URL);
        try (Response response = client.newCall(request).execute()) {
            if(response.body() == null) {
                return ;
            }
            String string = response.body().string();
            log.info(string);
            UploadAudioResponse bean = JSONUtil.toBean(string, UploadAudioResponse.class);
            if(bean == null || bean.getBaseResp() == null || bean.getBaseResp().getStatusCode() != 0){
                throw new RRException("volc upload fail" + string);
            }
        } catch (IOException e) {
            log.error(e.getMessage());
        }
    }

    private AudioRequest convert(AudioRequest AudioRequest) {
        return  ai.audio.pojo.AudioRequest.builder().appid( appId).speaker_id(AudioRequest.getSpeaker_id()).build();
    }

    @Override
    public AudioTrainStatus query(AudioRequest AudioRequest) {
        OkHttpClient client = new OkHttpClient();
        String statusReq = JSONUtil.toJsonStr(convert(AudioRequest));
        Request request = getRequest(statusReq, TRAIN_STATUS_URL);
        try (Response response = client.newCall(request).execute()) {
            if(response.body() == null) {
                return null;
            }
            String string = response.body().string();
            log.info(string);
            VolcAudioTrainStatus bean = JSONUtil.toBean(string, VolcAudioTrainStatus.class);
            if(bean == null || bean.getBaseResp() == null || bean.getBaseResp().getStatusCode() != 0){
                throw new RRException("volc query fail" + string);
            }
//            enum { NotFound = 0 Training = 1 Success = 2 Failed = 3 Active = 4 }
            return AudioTrainStatus.builder()
                    .createTime(bean.getCreate_time())
                    .version(bean.getVersion())
                    .extend(bean.getDemo_audio())
                    .message(bean.getSpeaker_id())
                    .status(bean.getStatus()).build();

        } catch (IOException e) {
            log.error(e.getMessage());
        }
        return null;
    }

    private @NotNull Request getRequest(String trainBody, String url) {
        RequestBody body = RequestBody.create(trainBody, MediaType.get("application/json; charset=utf-8"));
        Headers headers = new Headers.Builder()
                .add("Authorization", "Bearer;" + accessToken)
                .add("Resource-Id", "volc.megatts.voiceclone")
                .add("Content-Type", "application/json")
                .build();
        return  new Request.Builder()
                .url(url)
                .post(body)
                .headers(headers)
                .build();
    }



}
