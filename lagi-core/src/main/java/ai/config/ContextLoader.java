package ai.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.dataformat.yaml.YAMLMapper;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;

public class ContextLoader {

    public static AbstractConfiguration configuration = null;

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
            loadContextByResource("lagi.yml");
        } catch (Exception ignored) {
        }
        if(configuration == null) {
            try {

                loadContextByFilePath("lagi-web/src/main/resources/lagi.yml");
            } catch (Exception ignored) {

            }
        }
        if(configuration == null) {
            try {
                loadContextByFilePath("../lagi-web/src/main/resources/lagi.yml");
            } catch (Exception ignored) {

            }
        }
    }




    public static void main(String[] args) {


        System.out.println(ContextLoader.configuration);;
    }
}
