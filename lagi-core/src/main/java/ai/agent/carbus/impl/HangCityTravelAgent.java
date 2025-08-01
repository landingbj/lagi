package ai.agent.carbus.impl;

import ai.agent.carbus.HangCityAgent;
import ai.agent.carbus.pojo.*;
import ai.agent.carbus.util.ApiForCarBus;
import ai.common.exception.RRException;
import ai.common.utils.ThreadPoolManager;
import ai.config.pojo.AgentConfig;
import ai.llm.utils.LLMErrorConstants;
import ai.openai.pojo.ChatCompletionResult;
import ai.openai.pojo.ChatMessage;
import ai.utils.ApiInvokeUtil;
import ai.utils.DelayUtil;
import cn.hutool.core.util.StrUtil;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import io.reactivex.Observable;
import lombok.extern.slf4j.Slf4j;

import java.util.*;
import java.util.concurrent.*;
import java.util.stream.Collectors;

@Slf4j
public class HangCityTravelAgent extends HangCityAgent {

    private final String baseApiAddress = "http://20.17.127.24:11105/aicoapi/gateway/v2/chatbot/api_run/";
    private  ExecutorService executor;

    private final String introAppId = "1753064009_dc12560c-1652-4a76-bef9-5b5dbf25610a";
    private final String slotAppId = "1753063684_b2ac4664-cca5-4c7d-b3fa-855526ea4efe";
    private final String intentAppId = "1753063593_aef3fa7b-7d87-4ddd-a783-93c3798e3fb2";
    private final String planeAppId = "1753063641_f1e07ffe-816b-480d-9ee6-027ff413b2ef";


    public HangCityTravelAgent(AgentConfig config) {
        super(config);
        ThreadPoolManager.registerExecutor("hangCityTravelAgent", new ThreadPoolExecutor(1, 1000, 10, TimeUnit.SECONDS,
                new ArrayBlockingQueue<>(1000),
                (r, executor)->{
                    log.error(StrUtil.format("线程池队({})任务过多请求被拒绝", "hangCityTravelAgent"));
                }
        ));
        this.executor = ThreadPoolManager.getExecutor("hangCityTravelAgent");
    }

    @Override
    public Observable<ChatCompletionResult> chat(Request request) {
        Map<String, Object> output = getIntent(request);
        request.setQuery(new Gson().toJson(output));
        return retryChat(request, output);
    }

    private Observable<ChatCompletionResult> retryChat(Request request, Map<String, Object> output) {
        int tryTime = 0;
        while (tryTime < MAX_RETRY_TIME) {
            try {
                return ApiInvokeUtil.sse(baseApiAddress + planeAppId,
                        headers, new Gson().toJson(request), 180, TimeUnit.SECONDS,
                        (a)->{
                            ChatCompletionResult chatCompletionResult = this.convert2StreamResult(a);
                            if(chatCompletionResult != null) {
                                chatCompletionResult.setExtra(output);
                            }
                            return chatCompletionResult;
                        }).getObservable();
            } catch (Exception e) {
                log.error("chat error!", e);
            }
            tryTime++;
            DelayUtil.delaySeconds(1);
        }
        throw new RRException(LLMErrorConstants.OTHER_ERROR, "get intent error!");
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


    public List<Travel> getSlotAndFillingWithApi(Request request) {
        int tryTime = 0;
        String output = null;
        while (tryTime < MAX_RETRY_TIME) {
            try {
                Map<String, Object> data = getSlot(request);
                Map<String, Object> out1 = (Map<String, Object>) data.get("output");
                output =  (String)out1.get("output");
                break;
            } catch (Exception e) {
                log.error("get slot error!", e);
            }
            tryTime++;
            DelayUtil.delaySeconds(1);
        }
        if(output == null) {
            throw new RRException(LLMErrorConstants.OTHER_ERROR, "get slot error!");
        }
        return fillingWithApi(output);
    }

    private List<Travel> fillingWithApi(String outputStr) {
        Gson gson = new Gson();
        Map<String, Object> output = gson.fromJson( outputStr, new TypeToken<Map<String, Object>>() {});
        Set<String> days = output.keySet();
        List<Travel> travels = new ArrayList<>();
        Map<String, AttractionInfo> attractionInfoMap = new ConcurrentHashMap<>();
        for (String day : days) {
            Map<String, List<String>> dayPlanes = (Map<String, List<String>>) output.get(day);
            List<String> morning = dayPlanes.get( "上午");
            List<String> afternoon = dayPlanes.get("中午");
            List<String> evening = dayPlanes.get("下午");
            List<String> night = dayPlanes.get("晚上");
            List<AttractionInfo> monrings = morning.stream().map(name -> {
                AttractionInfo info = AttractionInfo.builder().name(name).build();
                attractionInfoMap.put(name, info);
                return info;
            }).collect(Collectors.toList());
            List<AttractionInfo> afternoons = afternoon.stream().map(name -> {
                AttractionInfo info = AttractionInfo.builder().name(name).build();
                attractionInfoMap.put(name, info);
                return info;
            }).collect(Collectors.toList());
            List<AttractionInfo> evenings = evening.stream().map(name -> {
                AttractionInfo info = AttractionInfo.builder().name(name).build();
                attractionInfoMap.put(name, info);
                return info;
            }).collect(Collectors.toList());
            List<AttractionInfo> nights = night.stream().map(name -> {
                AttractionInfo info = AttractionInfo.builder().name(name).build();
                attractionInfoMap.put(name, info);
                return info;
            }).collect(Collectors.toList());
            Travel build = Travel.builder().witchDay(day).morning(monrings).afternoon(afternoons).evening(evenings).night(nights).build();
            travels.add(build);
        }
        List<Future<AttractionInfo>> locationFutures = new ArrayList<>();
        List<Future<AttractionInfo>> scenicFutures = new ArrayList<>();
        for (String address : attractionInfoMap.keySet()) {
            Future<AttractionInfo> locationFuture = executor.submit(() -> {
                try {
                    ApiResponse<LocationData> locationData = ApiForCarBus.getLocationData(address);
                    LocationData data = locationData.getData();
                    if(data != null && data.getTips() != null &&  !data.getTips().isEmpty()) {
                        LocationTip tip = data.getTips().get(0);
                        Object location = tip.getLocation();
                        if(location instanceof String) {
                            String[] split = ((String) location).split(",");
                            AttractionInfo attractionInfo = attractionInfoMap.get(address);
                            attractionInfo.setLongitude(Double.parseDouble(split[0]));
                            attractionInfo.setLatitude(Double.parseDouble(split[1]));
                            return attractionInfo;
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                return null;
            });
            locationFutures.add(locationFuture);
            Future<AttractionInfo> scenicFuture = executor.submit(() -> {
                try {
                    ApiResponse<ScenicSpotData> scenicArea = ApiForCarBus.getScenicArea(address);
                    ScenicSpotData data = scenicArea.getData();
                    if(scenicArea.getCode() == 0) {
                        AttractionInfo attractionInfo = attractionInfoMap.get(address);
                        attractionInfo.setDescription(data.getDescription());
                        attractionInfo.setImageUrl(data.getImageUrl());
                        attractionInfo.setDetails(data.getDetails());
                        return attractionInfo;
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                return null;
            });
            scenicFutures.add(scenicFuture);
        }

        locationFutures.forEach(future->{
            try {
                AttractionInfo attractionInfo = future.get();
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
        scenicFutures.forEach(future->{
            try {
                AttractionInfo attractionInfo = future.get();
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
        return travels;
    }


    public String introduce(Request request) {
        String url = baseApiAddress +  introAppId;
        int tryTime = 0;
        while (tryTime < MAX_RETRY_TIME) {
            Map<String, Object> res = getOutput(url, request, "获取介绍失败", 1);
            try {
                return (String)((Map<String, Object>)res.get("output")).get("output");
            } catch (Exception e) {
            }
            tryTime++;
            DelayUtil.delay(500);
        }
        throw new RRException("转化介绍结果失败");
    }

    public Map<String, Object> getIntent(Request query) {
        String url = baseApiAddress +  intentAppId;
        int retryTime = 0;
        Gson gson = new Gson();
        // 重试  处理大模型输出不稳定问题
        while (retryTime < MAX_RETRY_TIME){
            try {
                Map<String, Object> data = getOutput(url, query, "获取意图失败", 1);
                String out = (String)((Map<String, Object>) data.get("output")).get("output");
                return gson.fromJson(out, new TypeToken<Map<String, Object>>(){});
            } catch (RRException e) {
            }
            retryTime++;
            DelayUtil.delaySeconds(1);
        }
        throw new RRException(LLMErrorConstants.OTHER_ERROR, "获取意图失败");
    }


    public Map<String, Object> getSlot(Request request) {
        String url = baseApiAddress +  slotAppId;
        return getOutput(url, request, "获取提示槽失败", 1);
    }



    public static void main(String[] args) throws InterruptedException {
        HangCityTravelAgent hangCityTravelAgent = new HangCityTravelAgent(AgentConfig.builder().appId("").build());
        Request request = Request.builder().doc_list(Collections.emptyList()).image_url("").query("三日游行程攻略").stream(true).build();

//        Observable<ChatCompletionResult> observable = hangCityTravelAgent.chat(request);
//        observable.blockingForEach(System.out::println);

//        request.setQuery("**DAY 1：西湖环湖**  \\n上午：断桥 → 白堤（步行 | 1.5h | 打车到断桥 | 拍雪景/桃柳）  \\n中午：楼外楼（西湖醋鱼 | 1h | 步行10分钟 | 预约靠窗位）  \\n下午：雷峰塔（登塔俯瞰 | 1h | 打车5分钟 | 带身份证）  \\n晚上：苏堤夜游（步行/骑行 | 2h | 打车到北山街 | 捕捉三潭灯光）  \\n\\n**DAY 2：灵隐禅意**  \\n上午：灵隐寺（烧香祈福 | 2h | 打车15分钟 | 穿素色衣服）  \\n中午：法云安缦茶餐厅（龙井虾仁 | 1h | 步行5分钟 | 预订靠山窗）  \\n下午：龙井村（采茶体验 | 2h | 打车20分钟 | 带防晒帽）  \\n晚上：河坊街（南宋小吃 | 2h | 打车10分钟 | 必吃葱包桧）  \\n\\n**DAY 3：湿地秘境**  \\n上午：西溪湿地（摇橹船 | 2h | 打车25分钟 | 穿防水鞋）  \\n中午：西溪湿地餐厅（有机鱼头 | 1h | 船上接驳 | 点藕粉甜品）  \\n下午：西溪国家湿地公园（芦苇迷宫 | 3h | 电瓶车接驳 | 带驱蚊水）  \\n晚上：钱江新城灯光秀（城市天际线 | 1h | 打车15分钟 | 19:30准时到奥体中心）");
//        List<Travel> slot = hangCityTravelAgent.getSlotAndFillingWithApi(request);
//        System.out.println(slot);

        request.setQuery("白堤");
        String intro = hangCityTravelAgent.introduce(request);
        System.out.println(intro);

        System.out.println("Done");
    }

}
