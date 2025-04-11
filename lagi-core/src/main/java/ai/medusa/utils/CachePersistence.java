package ai.medusa.utils;

import ai.medusa.pojo.CacheItem;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class CachePersistence {
    private static final Logger logger = LoggerFactory.getLogger(CachePersistence.class);
    private static final int BATCH_SIZE = PromptCacheConfig.CACHE_PERSISTENT_BATCH_SIZE;
    private final BlockingQueue<CacheItem> queue = new LinkedBlockingQueue<>();
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final String jsonlFilePath;
    private static final String persistenceDirectory;
    private static volatile CachePersistence instance;

    static {
        persistenceDirectory = PromptCacheConfig.CACHE_PERSISTENT_PATH;
    }

    private CachePersistence() {
        File dir = new File(persistenceDirectory);
        if (!dir.exists()) {
            dir.mkdirs();
        }
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        this.jsonlFilePath = persistenceDirectory + File.separator + "cache_" + timestamp + ".jsonl";
        try {
            File jsonlFile = new File(jsonlFilePath);
            if (!jsonlFile.exists()) {
                jsonlFile.createNewFile();
            }
            logger.info("Created JSONL file at: {}", jsonlFilePath);
        } catch (IOException e) {
            logger.error("Failed to create JSONL file", e);
        }
        startWorkerThread();
    }

    public static CachePersistence getInstance() {
        if (instance == null) {
            synchronized (CachePersistence.class) {
                if (instance == null) {
                    instance = new CachePersistence();
                }
            }
        }
        return instance;
    }

    public void addItem(CacheItem item) {
        try {
            queue.put(item);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            logger.error("Failed to add item to queue", e);
        }
    }

    private void startWorkerThread() {
        Thread workerThread = new Thread(() -> {
            while (true) {
                try {
                    processItems();
                } catch (Exception e) {
                    logger.error("Error processing items from queue", e);
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        logger.error("Worker thread interrupted", ie);
                    }
                }
            }
        });
        workerThread.setDaemon(true);
        workerThread.setName("CachePersistence-Worker");
        workerThread.start();
    }

    private void processItems() throws InterruptedException {
        List<CacheItem> batch = new ArrayList<>(BATCH_SIZE);
        while (true) {
            CacheItem item = queue.take();
            batch.add(item);
            if (batch.size() < BATCH_SIZE) {
                queue.drainTo(batch, BATCH_SIZE - batch.size());
            }
            if (batch.size() >= BATCH_SIZE) {
                logger.info("Batch size reached {}, persisting to file", BATCH_SIZE);
                persistBatch(batch);
                batch.clear();
                break;
            }
        }
    }

    private void persistBatch(List<CacheItem> batch) {
        if (batch.isEmpty()) {
            return;
        }
        synchronized (jsonlFilePath) {
            try (FileWriter writer = new FileWriter(jsonlFilePath, true)) {
                for (CacheItem item : batch) {
                    try {
                        String json = objectMapper.writeValueAsString(item);
                        writer.write(json);
                        writer.write("\n");
                    } catch (Exception e) {
                        logger.error("Failed to write item to JSONL file", e);
                        queue.put(item);
                    }
                }
                writer.flush();
                logger.info("Successfully persisted {} items to {}", batch.size(), jsonlFilePath);
            } catch (Exception e) {
                logger.error("Failed to persist batch to JSONL file", e);
                for (CacheItem item : batch) {
                    try {
                        queue.put(item);
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        logger.error("Failed to re-add item to queue", ie);
                    }
                }
            }
        }
    }
}