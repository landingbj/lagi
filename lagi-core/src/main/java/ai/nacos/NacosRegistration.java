package ai.nacos;

import ai.config.pojo.NacosGateWayConfig;
import com.alibaba.nacos.api.NacosFactory;
import com.alibaba.nacos.api.naming.NamingService;
import com.alibaba.nacos.api.naming.pojo.Instance;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;


import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;


public class NacosRegistration {


    private NacosGateWayConfig nacosGateWayConfig;

    public NacosRegistration(NacosGateWayConfig nacosGateWayConfig) {
        this.nacosGateWayConfig = nacosGateWayConfig;
    }


    public void registerService() {
        try {
            NamingService namingService = NacosFactory.createNamingService(this.nacosGateWayConfig.getGatewayIp());
            Instance instance = new Instance();
            instance.setIp(this.nacosGateWayConfig.getLocalIp());
            instance.setPort(this.nacosGateWayConfig.getLocalPort());
            instance.setServiceName(this.nacosGateWayConfig.getServiceName());
            instance.setHealthy(true);
            instance.setWeight(1.0);

            namingService.registerInstance(this.nacosGateWayConfig.getServiceName(), instance);
            System.out.println("服务注册成功: " + this.nacosGateWayConfig.getServiceName());
        } catch (Exception e) {
            System.err.println("服务注册失败: " + e.getMessage());
        }
    }

    public void startHealthCheckServer() {
        try {
            HttpServer server = HttpServer.create(new InetSocketAddress(this.nacosGateWayConfig.getGatewayPort()), 0);
            server.createContext("/actuator/health", new HealthCheckHandler());
            server.setExecutor(null);
            server.start();
            System.out.println("健康检查服务启动，端口: " + this.nacosGateWayConfig.getGatewayPort());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void startMainServer() {
        // 这里实现你的主应用逻辑
        // 例如，启动Servlet容器或其他HTTP服务器
        System.out.println("主应用服务启动，端口: " + this.nacosGateWayConfig.getLocalPort());
    }

    static class HealthCheckHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            String response = "UP";
            exchange.sendResponseHeaders(200, response.length());
            OutputStream os = exchange.getResponseBody();
            os.write(response.getBytes());
            os.close();
        }
    }
}