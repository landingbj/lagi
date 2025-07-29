package ai.agent.carbus.util;

import ai.agent.carbus.pojo.ApiResponse;
import ai.agent.carbus.pojo.LocationData;
import ai.agent.carbus.pojo.ScenicSpotData;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.IOException;

public class ApiForCarBus {
    public static final String BaseUrl = "http://200.200.1.119:9999";

    public static ApiResponse<LocationData> getLocationData(String keywords) throws IOException {
        String url = BaseUrl + "/app/amap/GDApi?keywords=" + keywords + "&apiType=inputTips";
        String get = BaseHttpRequestUtil.get(url, null, null);
        System.out.println(get);
        return new Gson().fromJson(get, new TypeToken<ApiResponse<LocationData> >(){});
    }

    public static ApiResponse<ScenicSpotData> getScenicArea(String name) throws IOException {
        String url = BaseUrl + "/app/scenicArea/queryOne?name=" + name;
        String get = BaseHttpRequestUtil.get(url, null, null);
        return new Gson().fromJson(get, new TypeToken<ApiResponse<ScenicSpotData> >(){});
    }

}
