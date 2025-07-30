package ai.agent.carbus.impl;

import ai.agent.carbus.HangCityAgent;
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
import ai.utils.DelayUtil;
import cn.hutool.core.util.StrUtil;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;
import io.reactivex.Observable;
import org.apache.hadoop.util.Lists;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

public class HangCitizenEngAgent extends HangCityAgent {

    private final String baseApiUrl = "http://20.17.127.24:11105/aicoapi/gateway/v2/chatbot/api_run/";

    private final String toolInvokeAppId = "1753063751_2e526302-73d7-4d6a-89a0-2943f31b9961";
    private final String answerAppId = "1753063785_720be409-2c96-4b21-8ea8-a1c25c726078";

    public HangCitizenEngAgent(AgentConfig config) {
        super(config);
    }


    public Observable<ChatCompletionResult> chat(Request request) {
        long start = System.currentTimeMillis();
        Map<String, Object> map = toolInvoke(request);
        long end = System.currentTimeMillis();
        System.out.println((end - start) / 1000);
        map.remove("slot_values");
        String intent = (String)map.get("intent");
        if("other".equals(intent)) {
            String answer;
            if(map.containsKey("answer")) {
                answer = (String)map.get("answer");
            } else {
                answer = "抱歉, 我无法为您回答该问题, 请提出更多市民出行相关的问题吧！";
            }
            ObservableList<ChatCompletionResult> observableList = new ObservableList<>();
            ChatCompletionResult chatCompletionResult = new ChatCompletionResult();
            ChatCompletionChoice choice = new ChatCompletionChoice();
            ChatMessage chatMessage = new ChatMessage();
            chatMessage.setRole("assistant");
            chatMessage.setContent(answer);
            choice.setMessage(chatMessage);
            chatCompletionResult.setChoices(Lists.newArrayList(choice));
            observableList.add(chatCompletionResult);
            chatCompletionResult.setExtra(map);
            return observableList.getObservable();
        }
        request.setQuery(new Gson().toJson(map));
        return retryChat(request, map).getObservable();
    }

    private ObservableList<ChatCompletionResult> retryChat(Request request, Map<String, Object> data) {
        int tryTimes = 0;
        RRException exception = null;
        ObservableList<ChatCompletionResult> sse1 = null;
        while (tryTimes < MAX_RETRY_TIME) {
            try {
                return ApiInvokeUtil.sse(baseApiUrl + answerAppId,
                        headers, new Gson().toJson(request), 180, TimeUnit.SECONDS,
                        (a)->{
                            ChatCompletionResult chatCompletionResult = this.convert2StreamResult(a);
                            if(chatCompletionResult != null) {
                                chatCompletionResult.setExtra(data);
                            }
                            return chatCompletionResult;
                        });
            } catch (RRException e) {
                exception = e;
            }
            tryTimes++;
            DelayUtil.delaySeconds(1);
        }
        throw exception;
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
        Map<String, Object> result = retryCitizenToolInvoke(request);
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

    private Map<String, Object> retryCitizenToolInvoke(Request request) {
        int tryTimes = 0;
        while (tryTimes < MAX_RETRY_TIME) {
            try {
                return citizenToolInvoke(request);
            } catch (Exception e) {
            }
            tryTimes++;
            DelayUtil.delaySeconds(1);
        }
        throw new RRException(LLMErrorConstants.OTHER_ERROR, "toolInvoke error!!!!");
    }

    private Map<String, Object> citizenToolInvoke(Request request) {
        try {
            Map<String, Object> data = getOutput(baseApiUrl + toolInvokeAppId, request, "toolInvoke error", 1);
            if(!data.containsKey("output")) {
                throw new RRException(LLMErrorConstants.OTHER_ERROR, "toolInvoke error!");
            }
            Map<String, Object> out1 = (Map<String, Object>) data.get("output");
            if(!out1.containsKey("output")) {
                throw new RRException(LLMErrorConstants.OTHER_ERROR, "toolInvoke error!!");
            }
            String outStr =  (String) out1.get("output");
            Gson gson = new Gson();
            return gson.fromJson(outStr, new TypeToken<Map<String, Object>>() {
            });
        } catch (JsonSyntaxException e) {
            throw new RRException(LLMErrorConstants.OTHER_ERROR, "toolInvoke error!!! \t" +  e.getMessage());
        }
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
                    apiData = ApiForCarBus.getBusRoute(originLocation, dstLocation);
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
        Request request = Request.builder().longitude(120.191227).latitude(30.279547).query("从智普ai导航去钱江世纪城").stream(true).build();
//        Map<String, Object> map = hangCitizenEngAgent.toolInvoke(request);
//        System.out.println( map);

        request.setQuery("导航去钱江世纪城");
        Observable<ChatCompletionResult> chat = hangCitizenEngAgent.chat(request);
        chat.blockingSubscribe((d)->{
            System.out.println(d.getChoices().get(0).getMessage().getContent());
        });
    }
}
