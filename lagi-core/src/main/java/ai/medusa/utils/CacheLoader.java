package ai.medusa.utils;

import ai.medusa.pojo.CacheItem;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class CacheLoader {
    private static final Logger logger = LoggerFactory.getLogger(CacheLoader.class);
    private final Gson gson = new Gson();
    private static final String persistenceDirectory;

    private static volatile CacheLoader instance;

    private final List<CacheItem> loadedItems;

    static {
        persistenceDirectory = PromptCacheConfig.CACHE_PERSISTENT_PATH;
    }

    private CacheLoader() {
        this.loadedItems = new ArrayList<>();
    }

    public static CacheLoader getInstance() {
        if (instance == null) {
            synchronized (CacheLoader.class) {
                if (instance == null) {
                    instance = new CacheLoader();
                }
            }
        }
        return instance;
    }

    public List<CacheItem> loadFromFile(String jsonlFilePath) {
        List<CacheItem> items = new ArrayList<>();
        File file = new File(jsonlFilePath);

        if (!file.exists() || !file.isFile()) {
            logger.warn("File does not exist or is not a regular file: {}", jsonlFilePath);
            return Collections.emptyList();
        }

        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            int lineNumber = 0;
            while ((line = reader.readLine()) != null) {
                lineNumber++;
                if (line.trim().isEmpty()) {
                    continue;
                }
                try {
                    CacheItem item = gson.fromJson(line, CacheItem.class);
                    items.add(item);
                } catch (Exception e) {
                    logger.warn("Failed to parse line {} in file {}: {}", lineNumber, jsonlFilePath, e.getMessage());
                }
            }
            logger.info("Successfully loaded {} items from {}", items.size(), jsonlFilePath);

        } catch (IOException e) {
            logger.error("Error reading file: {}", jsonlFilePath, e);
        }

        this.loadedItems.addAll(items);
        return items;
    }

    public void loadFromFiles() {
        int totalLoaded = 0;
        File dir = new File(persistenceDirectory);
        if (!dir.exists()) {
            return;
        }
        try (Stream<Path> pathStream = Files.list(Paths.get(persistenceDirectory))) {
            List<Path> files = pathStream
                    .filter(path -> path.toString().endsWith(".jsonl"))
                    .collect(Collectors.toList());
            logger.info("Found {} JSONL files in {}", files.size(), persistenceDirectory);
            for (Path path : files) {
                List<CacheItem> fileItems = loadFromFile(path.toString());
                totalLoaded += fileItems.size();
            }
            logger.info("Total items loaded from all files: {}", totalLoaded);

        } catch (IOException e) {
            logger.error("Error listing files in directory: {}", persistenceDirectory, e);
        }
    }

    public List<CacheItem> getLoadedItems() {
        Set<CacheItem> uniqueItems = loadedItems.stream()
                .filter(item -> item.getChatCompletionResult() != null)
                .collect(Collectors.toSet());
        return Collections.unmodifiableList(new ArrayList<>(uniqueItems));
    }

    public void clearLoadedItems() {
        loadedItems.clear();
        logger.info("Cleared all loaded items from memory");
    }
}