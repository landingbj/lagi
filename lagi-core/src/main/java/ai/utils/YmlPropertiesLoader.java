package ai.utils;

import ai.config.ContextLoader;
import cn.hutool.core.convert.Convert;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.dataformat.yaml.YAMLMapper;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.io.InputStream;
import java.util.Map;

@Slf4j
public class YmlPropertiesLoader {
    public static   <T> T loaderProperties(String yamlName, String fieldName, Class<T> clazz) {
        ObjectMapper mapper = new YAMLMapper();
        mapper.setPropertyNamingStrategy(PropertyNamingStrategies.SNAKE_CASE);
        try (InputStream inputStream = ContextLoader.class.getResourceAsStream("/" + yamlName);){
            TypeReference<Map<String, Object>> mapType = new TypeReference<Map<String, Object>>() {
            };
            Map<String, Object> map = mapper.readValue(inputStream, mapType);
            return  Convert.convert(clazz, map.get(fieldName));
        } catch (IOException e) {
            log.error("加载配置文件失败：{}", e.getMessage(), e);
        }
        return null;
    }

    public static  <T> T loaderProperties(String yamlName, String fieldName, cn.hutool.core.lang.TypeReference<T> typeReference) {
        ObjectMapper mapper = new YAMLMapper();
        mapper.setPropertyNamingStrategy(PropertyNamingStrategies.SNAKE_CASE);
        try (InputStream inputStream = ContextLoader.class.getResourceAsStream("/" + yamlName);){
            TypeReference<Map<String, Object>> mapType = new TypeReference<Map<String, Object>>() {
            };
            Map<String, Object> map = mapper.readValue(inputStream, mapType);
            return  Convert.convert(typeReference, map.get(fieldName));
        } catch (IOException e) {
            log.error("加载配置文件失败：{}", e.getMessage(), e);
        }
        return null;
    }

}
