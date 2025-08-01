package ai.servlet.api;

import ai.agent.carbus.HangCityAgent;
import ai.agent.carbus.impl.HangCitizenEngAgent;
import ai.agent.carbus.impl.HangCityTravelAgent;
import ai.agent.carbus.pojo.*;
import ai.agent.carbus.util.ApiForCarBus;
import ai.common.exception.RRException;
import ai.config.ContextLoader;
import ai.config.pojo.AgentConfig;
import ai.config.pojo.NacosGateWayConfig;
import ai.nacos.NacosRegistration;
import ai.openai.pojo.ChatCompletionResult;
import ai.servlet.RestfulServlet;
import ai.servlet.annotation.Body;
import ai.servlet.annotation.Post;
import ai.utils.SensitiveWordUtil;
import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.StrUtil;
import io.reactivex.Observable;
import lombok.extern.slf4j.Slf4j;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
public class ZhipuModelServlet extends RestfulServlet {


//    HDT
//    首页基础配置-场景类型-杭城深度游场景：Hangzhou Deep Tour-保留城市名首字母 + 核心词组合
//
//    CM
//    首页基础配置-场景类型-市民通勤场景：Commuting 取核心词前两位字母
//
//    WT
//    首页基础配置-场景类型-周末去哪玩场景：Weekend Trip 取核心词首字母组合
//
//    BF
//    首页基础配置-场景类型-公交特色场景：Bus Feature  取核心词首字母组合

    private static final String HDT_TYPE = "HDT";
    private static final String CM_TYPE = "CM";
    private static final String WT_TYPE = "WT";
    private static final String BF_TYPE = "BF";

    private static HangCityTravelAgent hangCityTravelAgent = null;
    private static HangCitizenEngAgent hangCitizenEngAgent = null;
    private static Map<String, HangCityAgent> chatAgentMap = new HashMap<>();

    static {
        try {
            NacosGateWayConfig gateway = ContextLoader.configuration.getGateway();
            NacosRegistration nacosRegistration = new NacosRegistration(gateway);
            nacosRegistration.registerService();
            nacosRegistration.startHealthCheckServer();
            nacosRegistration.startMainServer();
        } catch (Exception e) {
            e.printStackTrace();
        }
        try {
            List<AgentConfig> zhipuAgents = ContextLoader.configuration.getZhipuAgents();
            if(zhipuAgents != null) {
                for (AgentConfig agentConfig : zhipuAgents) {
                    if(HDT_TYPE.equals(agentConfig.getName())) {
                        hangCityTravelAgent = new HangCityTravelAgent(agentConfig);
                        chatAgentMap.put(HDT_TYPE, hangCityTravelAgent);
                    } else if(CM_TYPE.equals(agentConfig.getName())) {
                        hangCitizenEngAgent = new HangCitizenEngAgent(agentConfig);
                        chatAgentMap.put(CM_TYPE, hangCitizenEngAgent);
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        try {
            List<Map<String, String>> apis = ContextLoader.configuration.getApis();
            if(apis != null) {
                for (Map<String, String> api : apis) {
                    String name = api.get("name");
                    if("carbus".equals(name)) {
                        String url = api.get("base_url");
                        ApiForCarBus.setBaseUrl(url);
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }


    @Post("travelChat")
    public void chat(@Body Request request, HttpServletResponse resp) throws IOException {
        String type = request.getType();
        HangCityAgent hangCityAgent = chatAgentMap.get(type);
        if(hangCityAgent == null) {
            throw new RRException("未配置该智能体");
        }
        request.setStream(true);
        resp.flushBuffer();
        PrintWriter out = resp.getWriter();
        Observable<ChatCompletionResult> chat = hangCityAgent.chat(request);
        resp.setHeader("Content-Type", "text/event-stream;charset=utf-8");
        streamOutPrint(chat, out);
    }

    @Post("travelSlot")
    public Travels travelSlot(@Body Request request)  {
        if(hangCityTravelAgent == null) {
            throw new RRException("未配置该智能体");
        }
        request.setStream(true);
        return hangCityTravelAgent.getSlotAndFillingWithApi(request);
    }

    @Post("travelIntro")
    public String  travelIntro(@Body Request request) {
        if(hangCityTravelAgent == null) {
            throw new RRException("未配置该智能体");
        }
        request.setStream(true);
        ApiResponse<ScenicSpotData> scenicArea = ApiForCarBus.getScenicArea(request.getQuery());
        if(scenicArea != null && scenicArea.getCode() == 0) {
            String details = scenicArea.getData().getDetails();
            if(!StrUtil.isBlank( details)) {
                return details;
            }
        }
        String introduce = hangCityTravelAgent.introduce(request);
        try {
            ApiForCarBus.saveScenicArea(ScenicSpotData.builder().name(request.getQuery()).details(introduce).build());
        } catch (Exception ignored) {
        }
        return introduce;
    }


    private void streamOutPrint(Observable<ChatCompletionResult> observable, PrintWriter out) {
        try {
            final ChatCompletionResult[] fullResult = {null};
            observable
                    .doOnNext(data -> {
                        ChatCompletionResult filter = SensitiveWordUtil.filter(data);
                        try {
                            if(fullResult[0] == null) {
                                ChatCompletionResult chatCompletionResult = new ChatCompletionResult();
                                BeanUtil.copyProperties(filter, chatCompletionResult);
                                fullResult[0] = chatCompletionResult;
                            } else {
                                ChatCompletionResult chatCompletionResult = fullResult[0];
                                String reasonContent = nullToEmpty(chatCompletionResult.getChoices().get(0).getMessage().getReasoning_content());
                                String contentNew = nullToEmpty(filter.getChoices().get(0).getMessage().getContent());
                                String reasonContentNew = nullToEmpty(filter.getChoices().get(0).getMessage().getReasoning_content());
                                String content = nullToEmpty(chatCompletionResult.getChoices().get(0).getMessage().getContent());
                                chatCompletionResult.getChoices().get(0).getMessage().setContent(content + contentNew);
                                chatCompletionResult.getChoices().get(0).getMessage().setReasoning_content(reasonContent+reasonContentNew);
                            }
                        } catch (Exception ignored) {
                        }

                        String msg = gson.toJson(filter);
                        out.print("data: " + msg + "\n\n");
                        out.flush();
                    })
                    .doOnError(e -> log.error("", e))
                    .doOnComplete(() -> {
                        if (out != null) {
                            out.flush();
                            out.close();
                        }
                    })
                    .blockingSubscribe(); // 阻塞直到完成
            log.info("请求最终结果： {}", fullResult[0]);
        } catch (Exception e) {
            log.error("流处理异常", e);
        }
    }

    public static String nullToEmpty(String str) {
        return str == null ? "" : str;
    }

    @Post("citizenChat")
    public void citizenChat(@Body Request request, HttpServletResponse resp) throws IOException {
        if(hangCitizenEngAgent == null) {
            throw new RRException("未配置该智能体");
        }
        request.setStream(true);
        PrintWriter out = resp.getWriter();
        Observable<ChatCompletionResult> chat = hangCitizenEngAgent.chat(request);
        resp.setHeader("Content-Type", "text/event-stream;charset=utf-8");
        streamOutPrint(chat, out);
    }

}
