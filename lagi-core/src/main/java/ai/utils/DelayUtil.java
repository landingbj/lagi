package ai.utils;

public class DelayUtil {

    public static void delay(int millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException ignored) {
        }
    }

    public static void delaySeconds(int seconds) {
        try {
            Thread.sleep(seconds * 1000L);
        } catch (InterruptedException ignored) {
        }
    }
}
