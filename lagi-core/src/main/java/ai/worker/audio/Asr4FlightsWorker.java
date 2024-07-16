package ai.worker.audio;

import ai.audio.pojo.AsrResponse;
import ai.audio.service.AudioService;
import ai.common.pojo.AsrResult;
import ai.common.pojo.AudioRequestParam;
import ai.utils.LagiGlobal;
import ai.worker.Worker;
import ai.worker.pojo.Asr4FlightData;

public class Asr4FlightsWorker extends Worker {
    private final AudioService audioService = new AudioService();

    @Override
    public void work() {
    }

    @Override
    public void start() {
    }

    @Override
    public void stop() {
    }

    public AsrResponse process(Asr4FlightData data) {
        AudioRequestParam audioRequestParam = new AudioRequestParam();
        Asr4FlightData asr4FlightData = (Asr4FlightData) data;
        AsrResult result = audioService.asr(asr4FlightData.getResPath(), audioRequestParam);
        if (result.getStatus() == LagiGlobal.ASR_STATUS_SUCCESS) {
            String text = result.getResult();
            text = FlightAudio.correctFlightNumber(text);
            if (text != null) {
                return new AsrResponse(0, text);
            }
        }
        return new AsrResponse(1, "识别失败");
    }
}
