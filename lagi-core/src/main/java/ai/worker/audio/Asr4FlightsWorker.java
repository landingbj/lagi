package ai.worker.audio;

import ai.audio.pojo.AsrResponse;
import ai.audio.service.AudioService;
import ai.common.pojo.AsrResult;
import ai.common.pojo.AudioRequestParam;
import ai.utils.LagiGlobal;
import ai.worker.Worker;
import ai.worker.pojo.Asr4FlightData;

public class Asr4FlightsWorker extends Worker<Asr4FlightData, AsrResponse> {
    private final AudioService audioService = new AudioService();

    @Override
    public AsrResponse work(Asr4FlightData data) {
        return null;
    }

    @Override
    public AsrResponse call(Asr4FlightData data) {
        AudioRequestParam audioRequestParam = new AudioRequestParam();
        AsrResult result = audioService.asr(data.getResPath(), audioRequestParam);
        if (result.getStatus() == LagiGlobal.ASR_STATUS_SUCCESS) {
            String text = result.getResult();
            text = FlightAudio.correctFlightNumber(text);
            if (text != null) {
                return new AsrResponse(0, text);
            }
        }
        return new AsrResponse(1, "识别失败");
    }

    @Override
    public void notify(Asr4FlightData data) {

    }

}
