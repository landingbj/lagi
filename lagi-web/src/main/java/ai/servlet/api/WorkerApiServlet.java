package ai.servlet.api;

import ai.audio.pojo.AsrResponse;
import ai.audio.service.AudioService;
import ai.common.pojo.AsrResult;
import ai.common.pojo.AudioRequestParam;
import ai.openai.pojo.ChatCompletionRequest;
import ai.openai.pojo.ChatCompletionResult;
import ai.servlet.BaseServlet;
import ai.utils.FileUploadUtil;
import ai.utils.SensitiveWordUtil;
import ai.worker.DefaultWorker;
import ai.worker.audio.Asr4FlightsWorker;
import ai.worker.chengtouyun.SignUtil;
import ai.worker.pojo.Asr4FlightData;
import ai.worker.pojo.BatteryMonthlyReport;
import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.sun.xml.bind.v2.TODO;
import lombok.extern.slf4j.Slf4j;

import javax.servlet.ServletException;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.Part;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@MultipartConfig(fileSizeThreshold = 1024 * 1024, maxFileSize = 1024 * 1024 * 5, maxRequestSize = 1024 * 1024 * 5)
public class WorkerApiServlet extends BaseServlet {
    private final Asr4FlightsWorker asr4FlightsWorker = new Asr4FlightsWorker();
    private final DefaultWorker defaultWorker = new DefaultWorker();
    private AudioService audioService = new AudioService();
    private static final Gson gson = new Gson();
    private final static String HTTP_URL = "http://10.110.108.182:20088/";
//    private final static String HTTP_URL = "http://192.168.254.182:20088/";
    private final static String HTTP_RECOGNIZE = "http://20.17.127.16:8500/";
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        req.setCharacterEncoding("UTF-8");
        resp.setHeader("Content-Type", "application/json;charset=utf-8");
        String url = req.getRequestURI();
        String method = url.substring(url.lastIndexOf("/") + 1);
        if (method.equals("tiredetail")) {
            //轮胎详情
            this.tiredetail(req, resp);
        }else if (method.equals("vehicledetailsv")) {
            //车辆详情
            this.vehicledetailsv(req, resp);
        }else if (method.equals("careInformation")) {
            //保养信息分页接口
            //maintenance/v1/zhiPu/busMaintainRecord/page
            this.careInformation(req, resp);
        }else if (method.equals("maintenance")) {
            //维修信息列表接口
//            http://localhost:20091/maintenance/v1/zhiPu/warrantyInfo/page GET
            this.maintenance(req, resp);
        }else if (method.equals("vehiclepagedetailsv")) {
            //车辆分页查询
            this.vehiclepagedetailsv(req, resp);
        }

    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        req.setCharacterEncoding("UTF-8");
        resp.setHeader("Content-Type", "application/json;charset=utf-8");
        String url = req.getRequestURI();
        String method = url.substring(url.lastIndexOf("/") + 1);
        if (method.equals("uploadVoice") || method.equals("asr4flights")) {
            this.asr4flights(req, resp);
        } else if (method.equals("completions")) {
            this.completions(req, resp);
        } else if (method.equals("setBatteryMonthlyReport")) {
            //车辆月报表
            this.setBatteryMonthlyReport(req, resp);
        } else if (method.equals("faultRecognize")) {
            //车辆月报表
            this.faultRecognize(req, resp);
        }
    }

    private void faultRecognize(HttpServletRequest req, HttpServletResponse resp)  throws ServletException, IOException {
        resp.setContentType("application/json;charset=utf-8");
        AudioRequestParam audioRequestParam = postQueryToObj(req, AudioRequestParam.class);
        String uploadDir = getServletContext().getRealPath("/upload");
        if (!new File(uploadDir).isDirectory()) {
            new File(uploadDir).mkdirs();
        }
        String extension = "wav";
        if (audioRequestParam.getFormat() != null) {
            extension = audioRequestParam.getFormat();
        }
        String audioFilePath = uploadDir + "/" + FileUploadUtil.generateRandomFileName(extension);
        try (InputStream inputStream = req.getInputStream();
             OutputStream outputStream = new FileOutputStream(audioFilePath)) {
            byte[] buffer = new byte[4096];
            int bytesRead;
            while ((bytesRead = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, bytesRead);
            }
        } catch (Exception e) {
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
        AsrResult result = audioService.asr(audioFilePath, audioRequestParam);
        if (result.getStatus() == 20000000){
            String jsonInputString = "{ \"text\": \""+result.getResult()+"\" }";
            HttpResponse response = HttpRequest.post(HTTP_RECOGNIZE+"recognize")
                    .header("Content-Type", "application/json")
                    .body(jsonInputString)
                    .execute();
            responsePrint(resp, response.body());
        }else {
            responsePrint(resp, toJson(result));
        }

    }

    private void maintenance(HttpServletRequest req, HttpServletResponse resp)  throws ServletException, IOException {
        resp.setContentType("application/json;charset=utf-8");
        String VEHICLE_PAGE_URL = HTTP_URL+"maintenance/v1/zhiPu/warrantyInfo/page";
        String currentPage = req.getParameter("currentPage");
        String pageSize = req.getParameter("pageSize");
        String busId = req.getParameter("busId");
        Map<String, String> params = new HashMap<>();
        params.put("busId", busId);
        params.put("currentPage", currentPage);
        params.put("pageSize", pageSize);
        long timestamp = System.currentTimeMillis() / 1000;
        Map<String, String> signature = SignUtil.generateSign(params, timestamp);
        params.put("signature", signature.get("signature"));
        params.put("timestamp", signature.get("timestamp"));
        Map<String, Object> responseMap = new HashMap<>();

        try {
            String jsonInputString = "{\"busId\": "+busId+", \"currentPage\": "+currentPage+",\"pageSize\": "+pageSize+", \"signature\": \""+signature.get("signature")+"\", \"timestamp\": "+timestamp+" }";
            HttpResponse response = HttpRequest.get(VEHICLE_PAGE_URL)
                    .header("Content-Type", "application/json")
                    .body(jsonInputString)
                    .execute();
            resp.setHeader("Content-Type", "application/json;charset=utf-8");
            PrintWriter out = resp.getWriter();
            out.print(response.body());
            out.flush();
            out.close();
        } catch (Exception e) {
            log.error("处理电池月报数据时发生错误", e);
            responseMap.put("code", 1);
            responseMap.put("message", "错误: " + e.getMessage());
            responseMap.put("status", HttpServletResponse.SC_BAD_REQUEST);
            responseMap.put("data", null);
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            resp.setHeader("Content-Type", "application/json;charset=utf-8");
            PrintWriter out = resp.getWriter();
            out.print(gson.toJson(responseMap));
            out.flush();
            out.close();
        }
        //todo 处理逻辑12345

    }

    private void careInformation(HttpServletRequest req, HttpServletResponse resp)  throws ServletException, IOException {
        resp.setContentType("application/json;charset=utf-8");
        String VEHICLE_PAGE_URL = HTTP_URL+"maintenance/v1/zhiPu/busMaintainRecord/page";
        String currentPage = req.getParameter("currentPage");
        String pageSize = req.getParameter("pageSize");
        String busId = req.getParameter("busId");
        Map<String, String> params = new HashMap<>();
        params.put("busId", busId);
        params.put("currentPage", currentPage);
        params.put("pageSize", pageSize);
        long timestamp = System.currentTimeMillis() / 1000;
        Map<String, String> signature = SignUtil.generateSign(params, timestamp);
        params.put("signature", signature.get("signature"));
        params.put("timestamp", signature.get("timestamp"));
        Map<String, Object> responseMap = new HashMap<>();

        try {
            String jsonInputString = "{\"busId\": "+busId+", \"currentPage\": "+currentPage+",\"pageSize\": "+pageSize+", \"signature\": \""+signature.get("signature")+"\", \"timestamp\": "+timestamp+" }";
            HttpResponse response = HttpRequest.get(VEHICLE_PAGE_URL)
                    .header("Content-Type", "application/json")
                    .body(jsonInputString)
                    .execute();
            responseMap.put("code", 0);
            responseMap.put("message", "success");
            responseMap.put("status", HttpServletResponse.SC_OK);
            responseMap.put("data", response.body());
            resp.setHeader("Content-Type", "application/json;charset=utf-8");
            PrintWriter out = resp.getWriter();
            out.print(response.body());
            out.flush();
            out.close();
        } catch (Exception e) {
            responseMap.put("code", 1);
            responseMap.put("message", "错误: " + e.getMessage());
            responseMap.put("status", HttpServletResponse.SC_BAD_REQUEST);
            responseMap.put("data", null);
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            System.out.println("处理逻辑12345");
            resp.setHeader("Content-Type", "application/json;charset=utf-8");
            PrintWriter out = resp.getWriter();
            out.print(gson.toJson(responseMap));
            out.flush();
            out.close();
        }
        //todo 处理逻辑12345

    }
    private void setBatteryMonthlyReport(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        req.setCharacterEncoding("UTF-8");
        resp.setContentType("application/json;charset=utf-8");
        Map<String, Object> responseMap = new HashMap<>();
        try {
            // 从请求中读取JSON数据
            StringBuilder sb = new StringBuilder();
            String line;
            BufferedReader reader = req.getReader();
            while ((line = reader.readLine()) != null) {
                sb.append(line);
            }

            // 将JSON数据转换为BatteryMonthlyReport对象
            BatteryMonthlyReport report = gson.fromJson(sb.toString(), BatteryMonthlyReport.class);


            //todo 这里可以添加业务逻辑处理
            responseMap.put("code", 0);
            responseMap.put("message", "success");
            responseMap.put("status", HttpServletResponse.SC_OK);
            responseMap.put("data", report);
        } catch (Exception e) {
            log.error("处理电池月报数据时发生错误", e);
            responseMap.put("code", 1);
            responseMap.put("message", "处理电池月报数据时发生错误: " + e.getMessage());
            responseMap.put("status", HttpServletResponse.SC_BAD_REQUEST);
            responseMap.put("data", null);
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);

        }
        // 返回处理后的对象
        PrintWriter out = resp.getWriter();
        out.print(gson.toJson(responseMap));
        out.flush();
        out.close();
    }


    private void vehiclepagedetailsv(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException{
        resp.setContentType("application/json;charset=utf-8");
        String VEHICLE_PAGE_URL = HTTP_URL+"maintenance/v1/zhiPu/busInfo/page";
        String currentPage = req.getParameter("currentPage");
        String pageSize = req.getParameter("pageSize");
        Map<String, String> params = new HashMap<>();
        params.put("currentPage", currentPage);
        params.put("pageSize", pageSize);

        long timestamp = System.currentTimeMillis() / 1000;
        Map<String, String> signature = SignUtil.generateSign(params, timestamp);
        params.put("signature", signature.get("signature"));
        params.put("timestamp", signature.get("timestamp"));
        Map<String, Object> responseMap = new HashMap<>();

        try {
            String jsonInputString = "{ \"currentPage\": "+currentPage+",\"pageSize\": "+pageSize+", \"signature\": \""+signature.get("signature")+"\", \"timestamp\": "+timestamp+" }";
            HttpResponse response = HttpRequest.get(VEHICLE_PAGE_URL)
                    .header("Content-Type", "application/json")
                    .body(jsonInputString)
                    .execute();
            System.out.println("处理逻辑12345");
            resp.setHeader("Content-Type", "application/json;charset=utf-8");
            PrintWriter out = resp.getWriter();
            out.print(response.body());
            out.flush();
            out.close();
        } catch (Exception e) {
            log.error("处理电池月报数据时发生错误", e);
            responseMap.put("code", 1);
            responseMap.put("message", "错误: " + e.getMessage());
            responseMap.put("status", HttpServletResponse.SC_BAD_REQUEST);
            responseMap.put("data", null);
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            System.out.println("处理逻辑12345");
            resp.setHeader("Content-Type", "application/json;charset=utf-8");
            PrintWriter out = resp.getWriter();
            out.print(responseMap);
            out.flush();
            out.close();
        }
        //todo 处理逻辑12345

    }

    private void vehicledetailsv(HttpServletRequest req, HttpServletResponse resp)  throws ServletException, IOException{
        resp.setContentType("application/json;charset=utf-8");
        String VEHICLE_PAGE_URL = HTTP_URL+"maintenance/v1/zhiPu/busInfo/detail";
        String busId = req.getParameter("busId");
        Map<String, String> params = new HashMap<>();
        params.put("busId", busId);
        long timestamp = System.currentTimeMillis() / 1000;
        Map<String, String> signature = SignUtil.generateSign(params, timestamp);
        params.put("signature", signature.get("signature"));
        params.put("timestamp", signature.get("timestamp"));
        try {

            String jsonInputString = "{ \"busId\": "+busId+", \"signature\": \""+signature.get("signature")+"\", \"timestamp\": "+timestamp+" }";
            HttpResponse response = HttpRequest.get(VEHICLE_PAGE_URL)
                    .header("Content-Type", "application/json")
                    .body(jsonInputString)
                    .execute();
            resp.setHeader("Content-Type", "application/json;charset=utf-8");
            PrintWriter out = resp.getWriter();
            out.print(response.body());
            out.flush();
            out.close();
        } catch (Exception e) {
            Map<String, Object> responseMap = new HashMap<>();
            log.error("处理电池月报数据时发生错误", e);
            responseMap.put("code", 1);
            responseMap.put("message", "错误: " + e.getMessage());
            responseMap.put("status", HttpServletResponse.SC_BAD_REQUEST);
            responseMap.put("data", null);
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            resp.setHeader("Content-Type", "application/json;charset=utf-8");
            PrintWriter out = resp.getWriter();
            out.print(gson.toJson(responseMap));
            out.flush();
            out.close();
        }
        //todo 调用报告的接口

    }

    private void tiredetail(HttpServletRequest req, HttpServletResponse resp)  throws IOException {
        resp.setContentType("application/json;charset=utf-8");
        String VEHICLE_PAGE_URL = HTTP_URL+"maintenance/v1/zhiPu/tyre/detail";
        String busId = req.getParameter("busId");
        Map<String, String> params = new HashMap<>();
        params.put("busId", busId);
        long timestamp = System.currentTimeMillis() / 1000;
        Map<String, String> signature = SignUtil.generateSign(params, timestamp);
        params.put("signature", signature.get("signature"));
        params.put("timestamp", signature.get("timestamp"));

        Map<String, Object> responseMap = new HashMap<>();

        try {
            String jsonInputString = "{ \"busId\": "+busId+", \"signature\": \""+signature.get("signature")+"\", \"timestamp\": "+timestamp+" }";
            HttpResponse response = HttpRequest.get(VEHICLE_PAGE_URL)
                    .header("Content-Type", "application/json")
                    .body(jsonInputString)
                    .execute();
            resp.setHeader("Content-Type", "application/json;charset=utf-8");
            PrintWriter out = resp.getWriter();
            out.print(response.body());
            out.flush();
            out.close();
        } catch (Exception e) {
            log.error("处理电池月报数据时发生错误", e);
            responseMap.put("code", 1);
            responseMap.put("message", "错误: " + e.getMessage());
            responseMap.put("status", HttpServletResponse.SC_BAD_REQUEST);
            responseMap.put("data", null);
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
        }
        //todo 调用报告的接口
    }

    public void completions(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        resp.setContentType("application/json;charset=utf-8");
        ChatCompletionRequest chatCompletionRequest = reqBodyToObj(req, ChatCompletionRequest.class);
        ChatCompletionResult chatCompletionResult = defaultWorker.work("best", chatCompletionRequest);
        chatCompletionResult = SensitiveWordUtil.filter(chatCompletionResult);
        responsePrint(resp, gson.toJson(chatCompletionResult));
    }

    public void asr4flights(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        Part filePart = request.getPart("audioFile");
        String fileName = getFileName(filePart);
        String os = System.getProperty("os.name").toLowerCase();

        String tempFolder;
        if (os.contains("win")) {
            tempFolder = "C:/temp/";
        } else if (os.contains("nix") || os.contains("nux") || os.contains("mac")) {
            tempFolder = "/tmp/";
        } else {
            tempFolder = "/var/tmp/";
        }

        File tempDir = new File(tempFolder);
        if (!tempDir.exists()) {
            tempDir.mkdirs();
        }

        String savePath = tempFolder;
        String resPath = savePath + fileName;
        AsrResponse result;
        try (InputStream input = filePart.getInputStream();
             OutputStream output = Files.newOutputStream(Paths.get(savePath + fileName))) {
            byte[] buffer = new byte[1024];
            int bytesRead;
            while ((bytesRead = input.read(buffer)) != -1) {
                output.write(buffer, 0, bytesRead);
            }
            Asr4FlightData build = Asr4FlightData.builder().resPath(resPath).build();
            result = asr4FlightsWorker.call(build);
        } catch (IOException e) {
            result = new AsrResponse(1, "返回错误");
            e.printStackTrace();
        }
        response.setHeader("Content-Type", "application/json;charset=utf-8");
        PrintWriter out = response.getWriter();
        out.print(gson.toJson(result));
        out.flush();
        out.close();
    }

    private String getFileName(Part part) {
        for (String content : part.getHeader("content-disposition").split(";")) {
            if (content.trim().startsWith("filename")) {
                return content.substring(content.indexOf('=') + 1).trim()
                        .replace("\"", "");
            }
        }
        return null;
    }
}
