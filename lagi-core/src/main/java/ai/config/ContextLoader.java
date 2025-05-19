package ai.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.dataformat.yaml.YAMLMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;

public class ContextLoader {

    private static final Logger log = LoggerFactory.getLogger(ContextLoader.class);

    public static GlobalConfigurations configuration = null;

    private static void loadContextByInputStream(InputStream inputStream) {
        ObjectMapper mapper = new YAMLMapper();
        mapper.setPropertyNamingStrategy(PropertyNamingStrategies.SNAKE_CASE);
        try {
            configuration = mapper.readValue(inputStream, GlobalConfigurations.class);
            configuration.init();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static void loadContextByResource(String yamlName) {
        InputStream resourceAsStream = ContextLoader.class.getResourceAsStream("/" + yamlName);
        loadContextByInputStream(resourceAsStream);
    }

    public static void loadContextByFilePath(String filePath) {
        try {
            InputStream resourceAsStream = Files.newInputStream(Paths.get(filePath));
            loadContextByInputStream(resourceAsStream);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static void loadContext() {
        try {
            if(configuration == null) {
                loadContextByResource("lagi.yml");
            }
        } catch (Exception e) {
            log.warn(e.getMessage());
        }
        if(configuration == null) {
            try {

                loadContextByFilePath("lagi-web/src/main/resources/lagi.yml");
            } catch (Exception e) {
                log.warn(e.getMessage());
            }
        }
        if(configuration == null) {
            try {
                loadContextByFilePath("../lagi-web/src/main/resources/lagi.yml");
            } catch (Exception e) {
                log.warn(e.getMessage());
            }
        }
    }




    public static void main(String[] args) {

        ContextLoader.loadContext();
        System.out.println(ContextLoader.configuration);;
    }
}
