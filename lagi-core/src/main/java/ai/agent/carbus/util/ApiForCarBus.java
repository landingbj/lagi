package ai.agent.carbus.util;

import ai.agent.carbus.pojo.ApiResponse;
import ai.agent.carbus.pojo.LocationData;
import ai.agent.carbus.pojo.ScenicSpotData;
import ai.common.exception.RRException;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ApiForCarBus {
    public static final String BaseUrl = "http://200.200.1.119:9999";

    public static ApiResponse<LocationData> getLocationData(String keywords) throws IOException {
        String url = BaseUrl + "/app/amap/GDApi?keywords=" + keywords + "&apiType=inputTips";
        String get = BaseHttpRequestUtil.get(url, null, null);
        return new Gson().fromJson(get, new TypeToken<ApiResponse<LocationData> >(){});
    }

    public static ApiResponse<ScenicSpotData> getScenicArea(String name) throws IOException {
        String url = BaseUrl + "/app/scenicArea/queryOne?name=" + name;
        String get = BaseHttpRequestUtil.get(url, null, null);
        return new Gson().fromJson(get, new TypeToken<ApiResponse<ScenicSpotData> >(){});
    }

    public static Map<String, Object> getRoute (String type, String origin, String destination) throws IOException {
        Map<String, String> query = new HashMap<>();
        query.put("origin", origin);
        query.put("destination", destination);
        query.put("apiType", type);
        String url = BaseUrl + "/app/amap/GDApi";
        String get = BaseHttpRequestUtil.get(url, query, null);
        ApiResponse<Map<String, Object>> mapApiResponse = new Gson().fromJson(get, new TypeToken<ApiResponse<Map<String, Object>>>() {
        });
        if(mapApiResponse.getCode() != 0) {
            throw new RRException("调用骑行导航失败：" + mapApiResponse.getMsg());
        }
        Map<String, Object> data = mapApiResponse.getData();
        if(data == null) {
            throw new RRException("调用骑行导航失败：数据为空");
        }
        Object o = data.get("route");
        if(o == null) {
            throw new RRException("获取骑行路线为空");
        }
        return ( Map<String, Object>) o;
    }

    public static Map<String, Object> getBicyclingRoute (String origin, String destination) throws IOException {
        return getRoute("bicycling", origin, destination);
    }

    public static Map<String, Object> getWalkingRoute (String origin, String destination) throws IOException {
        return getRoute("walking", origin, destination);
    }

    public static Map<String, Object> getBusRoute (String origin, String destination) throws IOException {
        return getRoute("transit", origin, destination);
    }


    public static Integer  getDistance(String origin, String destination) throws IOException {
        Map<String, String> query = new HashMap<>();
        query.put("origins", origin);
        query.put("destination", destination);
        query.put("apiType", "distance");
        String url = BaseUrl + "/app/amap/GDApi";
        String get = BaseHttpRequestUtil.get(url, query, null);
        ApiResponse<Map<String, Object>> mapApiResponse = new Gson().fromJson(get, new TypeToken<ApiResponse<Map<String, Object>>>() {
        });
        if(mapApiResponse.getCode() != 0) {
            throw new RRException("调用距离计算失败" + mapApiResponse.getMsg());
        }
        Map<String, Object> data = mapApiResponse.getData();
        if(data == null) {
            throw new RRException("获取距离失败：" + mapApiResponse.getMsg());
        }
        List<Map<String, Object>> res = (List<Map<String, Object>>)data.get("results");
        if(res == null || res.isEmpty()) {
            throw new RRException("获取距离失败!!!：" + mapApiResponse.getMsg());
        }
        Map<String, Object> map = res.get(0);
        return Integer.parseInt((String) map.get("distance"));

    }


    public static List<Map<String, Object>>  getNearbyBicycleStation(String origin, Integer radius) throws IOException {
        String[] split = origin.split(",");
        Map<String, Object> body = new HashMap<>();
        body.put("apiType", "STATION_QUERY");
        Map<String, Object> data = new HashMap<>();
        data.put("longitude", Double.parseDouble(split[0]));
        data.put("latitude", Double.parseDouble(split[1]));
        data.put("radius", radius);
        body.put("requestData", data);
        String url = BaseUrl + "/app/JT/JTApi";
        Gson gson = new Gson();
        String post = BaseHttpRequestUtil.post(url, null, null, gson.toJson(body));
        Map<String, Object> res = gson.fromJson(post, new TypeToken<Map<String, Object>>() {
        });
        int code = ((Double) res.get("code")).intValue();
        if(code != 0) {
            throw new RRException("查询你附近站点失败：" + res.get("msg"));
        }
        return (List<Map<String, Object>>) res.get("data");

    }

    public static Map<String, Object> getNearbyStation(String origin, String type, String radius) throws IOException {
        Map<String, String> query = new HashMap<>();
        query.put("origin", origin);
        query.put("radius", radius);
        query.put("searchTypes", type);
        query.put("apiType", "around");
        String url = BaseUrl + "/app/amap/GDApi";
        String get = BaseHttpRequestUtil.get(url, query, null);
        ApiResponse<Map<String, Object>> mapApiResponse = new Gson().fromJson(get, new TypeToken<ApiResponse<Map<String, Object>>>() {
        });
        if(mapApiResponse.getCode() != 0) {
            throw new RRException("调用骑行导航失败：" + mapApiResponse.getMsg());
        }
        Map<String, Object> data = mapApiResponse.getData();
        if(data == null) {
            throw new RRException("调用骑行导航失败：数据为空");
        }
        return  data;
    }

    public static void main(String[] args) throws IOException {
//        Map<String, Object> bicyclingRoute = ApiForCarBus.getBicyclingRoute("120.199524,30.289429", "120.191227,30.279547");
//        System.out.println(bicyclingRoute);
//        Map<String, Object> walkingRoute = ApiForCarBus.getWalkingRoute("120.199524,30.289429", "120.191227,30.279547");
//        System.out.println(walkingRoute);
//        Map<String, Object> busRoute = ApiForCarBus.getBusRoute("120.199524,30.289429", "120.191227,30.279547");
//        System.out.println(busRoute);
//        Integer distance = ApiForCarBus.getDistance("120.199524,30.289429", "120.191227,30.279547");
//        System.out.println(distance);
//        List<Map<String, Object>> nearbyBicycleStation = ApiForCarBus.getNearbyBicycleStation("120.199524,30.289429", 300);
//        System.out.println(nearbyBicycleStation);

        Map<String, Object> station = ApiForCarBus.getNearbyStation("120.199524,30.289429", "公交车站", "3000");
        System.out.println(station);
    }
}
