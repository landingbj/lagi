package ai.servlet.api;

import ai.agent.carbus.impl.HangCityTravelAgent;
import ai.agent.carbus.pojo.Request;
import ai.agent.carbus.pojo.Travel;
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
import io.reactivex.Observable;
import lombok.extern.slf4j.Slf4j;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;

@Slf4j
public class ZhipuModelServlet extends RestfulServlet {

    private static HangCityTravelAgent hangCityTravelAgent = null;

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
                    if("travel".equals(agentConfig.getName())) {
                        hangCityTravelAgent = new HangCityTravelAgent(agentConfig);
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    @Post("travelChat")
    public void chat(@Body Request request, HttpServletResponse resp) throws IOException {
        if(hangCityTravelAgent == null) {
            throw new RRException("未配置该智能体");
        }
        PrintWriter out = resp.getWriter();
        Observable<ChatCompletionResult> chat = hangCityTravelAgent.chat(request);
        resp.setHeader("Content-Type", "text/event-stream;charset=utf-8");
        streamOutPrint(chat, out);
    }

    @Post("travelSlot")
    public List<Travel> travelSlot(@Body Request request)  {
        if(hangCityTravelAgent == null) {
            throw new RRException("未配置该智能体");
        }
        return hangCityTravelAgent.getSlotAndFillingWithApi(request);
    }

    @Post("travelIntro")
    public String  travelIntro(@Body Request request) {
        if(hangCityTravelAgent == null) {
            throw new RRException("未配置该智能体");
        }
        return hangCityTravelAgent.introduce(request);
    }


    private void streamOutPrint(Observable<ChatCompletionResult> observable, PrintWriter out) {
        try {
            observable
                    .doOnNext(data -> {
                        ChatCompletionResult filter = SensitiveWordUtil.filter(data);
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
        } catch (Exception e) {
            log.error("流处理异常", e);
        }
    }

}
