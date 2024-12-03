package ai.worker.audio;

import ai.audio.pojo.AsrResponse;
import ai.audio.service.AudioService;
import ai.common.pojo.AsrResult;
import ai.common.pojo.AudioRequestParam;
import ai.utils.LagiGlobal;
import ai.worker.Worker;
import ai.worker.pojo.Asr4FlightData;
import ai.worker.pojo.WorkData;

public class Asr4FlightsWorker extends Worker<Asr4FlightData, AsrResponse> {
    private final AudioService audioService = new AudioService();

    @Override
    public AsrResponse work(WorkData<Asr4FlightData> data) {
        return null;
    }

    @Override
    public AsrResponse call(WorkData<Asr4FlightData> data) {
        AudioRequestParam audioRequestParam = new AudioRequestParam();
        Asr4FlightData asr4FlightData = data.getData();
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

    @Override
    public void notify(WorkData<Asr4FlightData> data) {

    }

}
