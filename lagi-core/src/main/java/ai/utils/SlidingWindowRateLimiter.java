package ai.utils;
import java.time.Instant;
import java.util.LinkedList;
import java.util.Queue;

public class SlidingWindowRateLimiter {
    private final Queue<Instant> requestTimes = new LinkedList<>();
    private final int maxRequestsPerSecond;
    private final long windowSizeMs;

    public SlidingWindowRateLimiter(int windowSizeMs,  int maxRequestsPerSecond) {
        this.maxRequestsPerSecond = maxRequestsPerSecond;
        this.windowSizeMs = windowSizeMs;
    }

    public synchronized boolean allowRequest() {
        Instant now = Instant.now();

        while (!requestTimes.isEmpty() && now.toEpochMilli() - requestTimes.peek().toEpochMilli() > windowSizeMs) {
            requestTimes.poll();
        }

        if (requestTimes.size() >= maxRequestsPerSecond) {
            return false;
        }

        requestTimes.offer(now);
        return true;
    }

    public static void main(String[] args) {
        SlidingWindowRateLimiter rateLimiter = new SlidingWindowRateLimiter(1000, 1);

        for (int i = 0; i < 20; i++) {
            if (rateLimiter.allowRequest()) {
                System.out.println("Request " + i + " processed.");
            } else {
                System.out.println("Request " + i + " rejected.");
            }
            try {
                Thread.sleep(200);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}
