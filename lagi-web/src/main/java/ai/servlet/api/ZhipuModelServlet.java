package ai.servlet.api;

import ai.config.ContextLoader;
import ai.config.pojo.NacosGateWayConfig;
import ai.nacos.NacosRegistration;
import ai.servlet.RestfulServlet;
import ai.servlet.annotation.Post;

public class ZhipuModelServlet extends RestfulServlet {

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
    }

    @Post("test")
    public String test() {
        System.out.println("test");
        return "hello !";
    }
}
