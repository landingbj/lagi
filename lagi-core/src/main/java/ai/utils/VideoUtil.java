package ai.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class VideoUtil {
    private static final Logger logger = LoggerFactory.getLogger(VideoUtil.class);

    public static boolean extractAudio(String videoPath, String audioPath) {
        try {
            String cmd = "D:/Tools/ffmpeg/bin/ffmpeg -i " + videoPath + " -y -f mp3  -ar 16000 -vn " + audioPath + " -loglevel quiet";
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
