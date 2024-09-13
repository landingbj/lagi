package ai.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;

public class VideoUtil {
    private static final Logger logger = LoggerFactory.getLogger(VideoUtil.class);

    private static String FFMPEG_PATH = "D:/Tools/ffmpeg/bin/ffmpeg.exe";

    static {
        File file = new File(FFMPEG_PATH);
        if (!file.exists()) {
            FFMPEG_PATH = "ffmpeg";
        }
    }

    public static boolean extractAudio(String videoPath, String audioPath) {
        try {
            String cmd = FFMPEG_PATH + " -i " + videoPath + " -y -f mp3  -ar 16000 -vn " + audioPath + " -loglevel quiet";
            Process process = Runtime.getRuntime().exec(cmd);
            process.waitFor();
            if (process.exitValue() == 0) {
                return true;
            }
        } catch (Exception e) {
            logger.error("extract audio failed", e);
        }
        return false;
    }
}
