package ai.audio.adapter.impl;

import ai.audio.adapter.IAudioAdapter;
import ai.common.ModelService;
import ai.common.pojo.AsrResult;
import ai.common.pojo.AudioRequestParam;
import ai.common.pojo.TTSRequestParam;
import ai.common.pojo.TTSResult;
import ai.utils.LagiGlobal;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.file.Files;

public class WhisperAudioAdapter extends ModelService implements IAudioAdapter {

    private final String HOST = "localhost:9100";
    private final String UPLOAD_FILE = "http://" + HOST + "/upload_file";
    private final String AUDIO_TO_TEXT = "http://" + HOST + "/audio2text";
    private final String TEXT_TO_AUDIO = "http://" + HOST + "/text2audio";

    // AudioToText
    @Override
    public AsrResult asr(File audio, AudioRequestParam param) {
        AsrResult result = new AsrResult();
        try {
            String filename = uploadFile(audio);
            if (filename == null) {
                result.setStatus(LagiGlobal.ASR_STATUS_FAILURE);
                result.setMessage("File upload failed");
                return result;
            }

            URL url = new URL(AUDIO_TO_TEXT);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setDoOutput(true);

            // Build the request body, including language and uploaded file name
            String jsonInputString = "{\"filename\":\"" + filename + "\",\"language\":\"" + param.getModel() + "\"}";

            try (DataOutputStream wr = new DataOutputStream(conn.getOutputStream())) {
                wr.writeBytes(jsonInputString);
                wr.flush();
            }

            BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            String inputLine;
            StringBuilder content = new StringBuilder();
            while ((inputLine = in.readLine()) != null) {
                content.append(inputLine);
            }
            in.close();

            String response = content.toString();
            String textResult = parseResult(response, "result");

            result.setStatus(LagiGlobal.ASR_STATUS_SUCCESS);
            result.setResult(textResult);
        } catch (Exception e) {
            e.printStackTrace();
            result.setStatus(LagiGlobal.ASR_STATUS_FAILURE);
            result.setMessage("Audio to text processing failed");
        }
        return result;
    }

    // TextToAudio
    @Override
    public TTSResult tts(TTSRequestParam param) {
        TTSResult result = new TTSResult();
        try {
            URL url = new URL(TEXT_TO_AUDIO);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setDoOutput(true);

            // Build a request body, including necessary parameters
            String jsonInputString = String.format(
                    "{\"emotion\":\"%s\", \"text\":\"%s\", \"voice\":\"%s\", \"volume\":%d, \"speech_rate\":%d, \"pitch_rate\":%d}",
                    param.getEmotion(), param.getText(), param.getVoice(), param.getVolume(), param.getSpeech_rate(), param.getPitch_rate()
            );

            try (DataOutputStream wr = new DataOutputStream(conn.getOutputStream())) {
                wr.writeBytes(jsonInputString);
                wr.flush();
            }

            BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            String inputLine;
            StringBuilder content = new StringBuilder();
            while ((inputLine = in.readLine()) != null) {
                content.append(inputLine);
            }
            in.close();

            String response = content.toString();
            String audioUrl = parseResult(response, "data");

            result.setStatus(LagiGlobal.ASR_STATUS_SUCCESS);
            result.setResult(audioUrl);
        } catch (Exception e) {
            e.printStackTrace();
            result.setStatus(LagiGlobal.ASR_STATUS_FAILURE);
            result.setMessage("Text to voiced voice processing failed");
        }
        return result;
    }

    private String uploadFile(File file) {
        try {
            URL url = new URL(UPLOAD_FILE);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setDoOutput(true);
            conn.setRequestProperty("Content-Type", "multipart/form-data; boundary=--Boundary");

            try (DataOutputStream wr = new DataOutputStream(conn.getOutputStream())) {
                wr.writeBytes("--Boundary\r\n");
                wr.writeBytes("Content-Disposition: form-data; name=\"file\"; filename=\"" + file.getName() + "\"\r\n");
                wr.writeBytes("Content-Type: application/octet-stream\r\n\r\n");
                Files.copy(file.toPath(), wr);
                wr.writeBytes("\r\n--Boundary--\r\n");
                wr.flush();
            }

            BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            String inputLine;
            StringBuilder content = new StringBuilder();
            while ((inputLine = in.readLine()) != null) {
                content.append(inputLine);
            }
            in.close();

            return parseResult(content.toString(), "filename");
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private String parseResult(String response, String key) {
        int start = response.indexOf(key + "\":\"") + key.length() + 3;
        int end = response.indexOf("\"", start);
        return response.substring(start, end);
    }
}
