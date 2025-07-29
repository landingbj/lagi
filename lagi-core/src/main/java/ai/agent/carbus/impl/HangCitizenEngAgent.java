package ai.agent.carbus.impl;

import ai.agent.carbus.pojo.*;
import ai.agent.carbus.util.ApiForCarBus;
import ai.common.exception.RRException;
import ai.common.utils.ObservableList;
import ai.config.pojo.AgentConfig;
import ai.llm.utils.LLMErrorConstants;
import ai.openai.pojo.ChatCompletionChoice;
import ai.openai.pojo.ChatCompletionResult;
import ai.openai.pojo.ChatMessage;
import ai.utils.ApiInvokeUtil;
import cn.hutool.core.util.StrUtil;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import io.reactivex.Observable;
import org.apache.hadoop.util.Lists;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

public class HangCitizenEngAgent extends HangCityTravelAgent{

    protected Map<String,  String> headers;
    private final String baseApiUrl = "http://20.17.127.24:11105/aicoapi/gateway/v2/chatbot/api_run/";

    private final String toolInvokeAppId = "1753063751_2e526302-73d7-4d6a-89a0-2943f31b9961";
    private final String answerAppId = "1753063785_720be409-2c96-4b21-8ea8-a1c25c726078";

    public HangCitizenEngAgent(AgentConfig config) {
        super(config);
    }


    public Observable<ChatCompletionResult> chat(Request request) {
        Map<String, Object> map = toolInvoke(request);
        String intent = (String)map.get("intent");
        if("other".equals(intent)) {
            String answer = (String)map.get("answer");
            ObservableList<ChatCompletionResult> observableList = new ObservableList<>();
            ChatCompletionResult chatCompletionResult = new ChatCompletionResult();
            ChatCompletionChoice choice = new ChatCompletionChoice();
            ChatMessage chatMessage = new ChatMessage();
            chatMessage.setRole("assistant");
            chatMessage.setContent(answer);
            choice.setMessage(chatMessage);
            chatCompletionResult.setChoices(Lists.newArrayList(choice));
            observableList.add(chatCompletionResult);
            return observableList.getObservable();
        }
        request.setQuery(new Gson().toJson(map));
        ObservableList<ChatCompletionResult> sse1 = ApiInvokeUtil.sse(baseApiUrl + answerAppId,
                headers, new Gson().toJson(request), 180, TimeUnit.SECONDS,
                this::convert2StreamResult);
        return sse1.getObservable();
    }

    private ChatCompletionResult convert2StreamResult(String response) {
        Map<String, Object> out = new Gson().fromJson(response, new TypeToken<Map<String, Object>>() {
        }.getType());
        if("node_chunk".endsWith((String) out.get("event"))) {
            Result<ChatCompletionResult> chatCompletionResultResult = new Gson().fromJson(response, new TypeToken<Result<ChatCompletionResult>>() {
            });
            Object o = out.get("session_id");
            if(o != null) {
                chatCompletionResultResult.getData().setSession_id((String) o);
            }
            chatCompletionResultResult.getData().getChoices().forEach(choice->{
                ChatMessage delta = choice.getDelta();
                choice.setMessage(delta);
                choice.setDelta(null);
            });
            return chatCompletionResultResult.getData();
        }
        // 非流式 设为 null 被过滤
        return null;
    }

    public Map<String, Object> toolInvoke(Request request) {
        Map<String, Object> data = getOutput(baseApiUrl + toolInvokeAppId, request, "toolInvoke error");
        if(!data.containsKey("output")) {
            throw new RRException(LLMErrorConstants.OTHER_ERROR, "toolInvoke error!");
        }
        Map<String, Object> out1 = (Map<String, Object>) data.get("output");
        if(!out1.containsKey("output")) {
            throw new RRException(LLMErrorConstants.OTHER_ERROR, "toolInvoke error!!");
        }
        String outStr =  (String) out1.get("output");
        Gson gson = new Gson();
        Map<String, Object> result = gson.fromJson(outStr, new TypeToken<Map<String, Object>>() {
        });
        String intent = (String) result.get("intent");
        Map<String, String> slotValues = Collections.emptyMap();
        if(result.get("slot_values") != null) {
            slotValues = (Map<String, String>)result.get("slot_values");
        }
        if("other".equals(intent)) {
            return result;
        } else if("navigate".equals(intent)) { // 导航
            doNavigate(request, slotValues, result);
        } else if("bus_station".equals(intent) || "subway_station".equals(intent)) { // 公交车站查询
            doStations(request, slotValues, result);
        } else if("redcycle_station".equals(intent)) {
            doBicycleStation(request, slotValues, result);
        } else if("bus_arrival".equals(intent)) {
            doArrival(request, slotValues, "公交车站", result);
        } else if("subway_arrival".equals(intent)) {
            doArrival(request, slotValues, "地铁站", result);
        } else if("bus_line".equals( intent)) {

        } else if("subway_line".equals(intent)) {

        }
        return result;
    }

    private void doArrival(Request request, Map<String, String> slotValues, String type, Map<String, Object> result) {
        String lineName = slotValues.get("line_name");
        String stationName = slotValues.get("station_name");
        if(StrUtil.isBlank(stationName)) {
            String location = request.getLongitude() + "," + request.getLatitude();
            try {
                Map<String, Object> nearbyStation = ApiForCarBus.getNearbyStation(location, type, "2000");
                List<Map<String, Object>> pois = (List<Map<String, Object>>)nearbyStation.get("pois");
                for (Map<String, Object> poi : pois) {
                    stationName = (String) poi.get("name");
                    break;
                }
            } catch (Exception e) {

            }
        }
        if(StrUtil.isBlank(stationName)) {
            result.put("data", "为查询到附近站点, 请指定附近站点");
        } else {
            result.put("data", "暂无站点到站信息");
        }
    }

    private void doBicycleStation(Request request, Map<String, String> slotValues, Map<String, Object> result) {
        String location = slotValues.get("location");
        String radius = slotValues.get("radius");
        CompletableFuture<String> locationFuture = CompletableFuture.supplyAsync(() -> {
            if(StrUtil.isNotBlank(location)) {
                return this.getLocation(location);
            }
            return request.getLongitude() + "," + request.getLatitude();
        });
        try {
            String position = locationFuture.get();
            List<Map<String, Object>> nearbyBicycleStation = ApiForCarBus.getNearbyBicycleStation(position, Integer.parseInt(radius));
            result.put("data", nearbyBicycleStation);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void doStations(Request request, Map<String, String> slotValues, Map<String, Object> result) {
        String location = slotValues.get("location");
        String radius = slotValues.get("radius");
        String type = slotValues.get("type");
        CompletableFuture<String> locationFuture = CompletableFuture.supplyAsync(() -> {
            if(StrUtil.isNotBlank(location)) {
                return this.getLocation(location);
            }
            return request.getLongitude() + "," + request.getLatitude();
        });
        try {
            String position = locationFuture.get();
            Map<String, Object> station = ApiForCarBus.getNearbyStation(position, type, radius);
            result.put("data", station);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void doNavigate(Request request, Map<String, String> slotValues, Map<String, Object> result) {
        String origin = slotValues.get("origin");
        String destination = slotValues.get("destination");
        String type = slotValues.get("type");
        CompletableFuture<String> originFuture = CompletableFuture.supplyAsync(() -> {
            if(StrUtil.isNotBlank(origin)) {
                return this.getLocation(origin);
            }
            return request.getLongitude() + "," + request.getLatitude();
        });
        CompletableFuture<String> dstFuture = CompletableFuture.supplyAsync(() -> this.getLocation(destination));
        try {
            String originLocation = originFuture.get();
            String dstLocation = dstFuture.get();
            Map<String, Object> apiData =null;
            if("cycling".equals(type)) {
                apiData = ApiForCarBus.getBicyclingRoute(originLocation, dstLocation);
            } else if("public_trans".equals(type)) {
                apiData = ApiForCarBus.getBusRoute(originLocation, dstLocation);
            } else if("walking".equals(type)) {
                apiData = ApiForCarBus.getWalkingRoute(originLocation, dstLocation);
            } else {
                // 判断距离
                // > 1000 公交导航
                int distance = ApiForCarBus.getDistance(originLocation, dstLocation);
                if(distance > 1000) {
                    apiData = ApiForCarBus.getBicyclingRoute(originLocation, dstLocation);
                } else if(distance < 300) {
                    apiData = ApiForCarBus.getWalkingRoute(originLocation, dstLocation);
                } else {
                    // 300 - 1000
                    List<Map<String, Object>> nearbyBicycleStation = ApiForCarBus.getNearbyBicycleStation(originLocation, 300);
                    if(nearbyBicycleStation.isEmpty()) {
                        apiData = ApiForCarBus.getWalkingRoute(originLocation, dstLocation);
                    } else {
                        apiData = ApiForCarBus.getBicyclingRoute(originLocation, dstLocation);
                    }
                }
            }
            result.put("data", apiData);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 获取经纬度信息
     * @return 122.00,32.00
     */
    public String getLocation(String address) {
        try {
            ApiResponse<LocationData> locationData = ApiForCarBus.getLocationData(address);
            LocationData data = locationData.getData();
            if(data != null && data.getTips() != null &&  !data.getTips().isEmpty()) {
                LocationTip tip = data.getTips().get(0);
                Object location = tip.getLocation();
                if(location instanceof String) {
                    return (String) location;
                }
            }
        } catch (Exception ignored) {

        }
        return null;
    }

    public static void main(String[] args) {
        AgentConfig config = AgentConfig.builder().apiKey("").build();

        HangCitizenEngAgent hangCitizenEngAgent = new HangCitizenEngAgent(config);
        Request request = Request.builder().longitude(120.191227).latitude(30.279547).query("导航去钱江世纪城").stream(true).build();
        Map<String, Object> map = hangCitizenEngAgent.toolInvoke(request);
        System.out.println( map);

        request.setQuery("你好");
        Observable<ChatCompletionResult> chat = hangCitizenEngAgent.chat(request);
        chat.subscribe(System.out::println);
    }
}
