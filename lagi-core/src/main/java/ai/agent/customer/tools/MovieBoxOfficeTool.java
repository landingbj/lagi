package ai.agent.customer.tools;

import ai.agent.customer.pojo.ToolInfo;
import ai.utils.ApiInvokeUtil;
import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;
import lombok.Setter;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Setter
public class MovieBoxOfficeTool extends AbstractTool {

    private static final String API_ADDRESS = "https://api.pearktrue.cn/api/maoyan/";

    public MovieBoxOfficeTool() {
        init();
    }

    private void init() {
        name = "movie_box_office";
        toolInfo = ToolInfo.builder().name("movie_box_office")
                .description("这是一个电影票房工具，提供实时的电影票房榜单")
                .args(new ArrayList<>())
                .build();
        register(this);
    }

    public String getMovieBoxOffice() {
        Map<String, String> headers = new HashMap<>();
        headers.put("Content-Type", "application/json");
        Map<String, String> queryParams = null;
        String response = ApiInvokeUtil.get(API_ADDRESS, queryParams, headers, 15, TimeUnit.SECONDS);
        if (response == null) {
            return "查询失败";
        }
        Gson gson = new Gson();
        Type type = new TypeToken<Map<String, Object>>() {}.getType();
        Map<String, Object> map = gson.fromJson(response, type);

        if (map == null || map.get("code") == null) {
            return "查询失败";
        }

        Object codeObj = map.get("code");
        if (codeObj instanceof Double && ((Double) codeObj).intValue() != 200) {
            return "查询失败";
        }

        List<Map<String, Object>> data = (List<Map<String, Object>>) map.get("data");

        int limit = Math.min(data.size(), 20);

        StringBuilder result = new StringBuilder();
        for (int i = 0; i < limit; i++) {
            Map<String, Object> movie = data.get(i);
            double top = Double.parseDouble(movie.get("top").toString());
            int topInt = (int) top;
            String movieName = (String) movie.get("movieName");
            String releaseInfo = (String) movie.get("releaseInfo");
            String sumBoxDesc = (String) movie.get("sumBoxDesc");
            String boxRate = (String) movie.get("boxRate");
            result.append(String.format("第%d名: %s (%s), 累计票房: %s, 占比: %s\n", topInt, movieName, releaseInfo, sumBoxDesc, boxRate));
        }

        return result.toString();
    }

    @Override
    public String apply(Map<String, Object> args) {
        return getMovieBoxOffice();
    }

    public static void main(String[] args) {
        MovieBoxOfficeTool movieBoxOfficeTool = new MovieBoxOfficeTool();
        String result = movieBoxOfficeTool.getMovieBoxOffice();
        System.out.println(result);
    }
}
