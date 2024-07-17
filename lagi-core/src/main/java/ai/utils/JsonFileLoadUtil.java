package ai.utils;

import cn.hutool.json.JSONUtil;
import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class JsonFileLoadUtil {

    public static List<String> readWordListJson(String resPath) {
        String content = "{}";
        List<String> result = new ArrayList<>();
        try (InputStream in = JsonFileLoadUtil.class.getResourceAsStream(resPath);) {
            if (in == null) {
                return result;
            }
            content = new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8)).lines().collect(Collectors.joining("\n"));
        } catch (IOException e) {
            e.printStackTrace();
        }
        Type listType = new TypeToken<List<String>>() {
        }.getType();
        List<String> tempResult = new Gson().fromJson(content, listType);
        for (String word : tempResult) {
            result.add(word.toLowerCase());
        }
        return result;
    }


    public static<T> T readWordLRulesList(String resPath, Class<T> tClass) {
        String content = "{}";
        try (InputStream in = JsonFileLoadUtil.class.getResourceAsStream(resPath);) {
            if (in == null) {
                return null;
            }
            content = new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8))
                    .lines()
                    .collect(Collectors.joining("\n"));
            return JSONUtil.toBean(content, tClass);
        } catch (Exception ignored) {

        }
        return null;
    }

}
