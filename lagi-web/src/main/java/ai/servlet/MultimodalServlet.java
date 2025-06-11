package ai.servlet;

import ai.llm.pojo.InstructionEntity;
import ai.llm.service.InstructionService;
import ai.migrate.service.ApiService;
import ai.openai.pojo.ChatMessage;
import ai.router.pojo.LLmRequest;
import ai.servlet.annotation.Post;
import ai.utils.FileAndDocUtil;
import ai.utils.MigrateGlobal;
import com.google.gson.Gson;
import org.apache.commons.io.IOUtils;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MultimodalServlet extends RestfulServlet {
    private static final long serialVersionUID = 1L;
    private final Gson gson = new Gson();
    private final ApiService apiService = new ApiService();
    private final InstructionService instructionService = new InstructionService();

    @Post("process")
    public void process(HttpServletRequest req, HttpServletResponse resp)
            throws IOException {
        resp.setHeader("Content-Type", "application/json;charset=utf-8");
        PrintWriter out = resp.getWriter();

        String jsonString = IOUtils.toString(req.getInputStream(), StandardCharsets.UTF_8);
        LLmRequest lLmRequest = gson.fromJson(jsonString, LLmRequest.class);
        List<ChatMessage> messages = lLmRequest.getMessages();

        HttpSession session = req.getSession();
        String lastVideoPath = (String) session.getAttribute("last_video_file");
        String lastImagePath = (String) session.getAttribute("last_image_file");
        String lastFilePath = (String) session.getAttribute("lastFilePath");

        String content = messages.get(messages.size() - 1).getContent().trim();
        String modal = lLmRequest.getIntent().getModal();

        String result;
        if (modal.equals(MigrateGlobal.INSTRUCTION_TYPE_INSTRUCTION)) {
            Map<String, Object> map = new HashMap<>();
            if (lastFilePath != null && !lastFilePath.isEmpty()) {
                String fileContent = FileAndDocUtil.getContent(lastFilePath);
                List<InstructionEntity> instructionsResponseList = instructionService.getInstructionList(fileContent);
                map.put("status", "success");
                map.put("instructions", instructionsResponseList);
            } else {
                map.put("status", "failed");
                map.put("message", "No file uploaded or file content is empty.");
            }
            result = gson.toJson(map);
        } else if (modal.equals(MigrateGlobal.INSTRUCTION_TYPE_IMAGE)) {
            result = apiService.generateImage(content, req);
        } else if (modal.equals(MigrateGlobal.INSTRUCTION_TYPE_IMAGE_TO_TEXT) && lastImagePath != null) {
            result = apiService.imageToText(lastImagePath, req);
        } else if (modal.equals(MigrateGlobal.INSTRUCTION_TYPE_ESRGAN) && lastImagePath != null) {
            result = apiService.enhanceImage(lastImagePath, req);
        } else if (modal.equals(MigrateGlobal.INSTRUCTION_TYPE_SVD) && lastImagePath != null) {
            result = apiService.generateVideo(lastImagePath, req);
        } else if (modal.equals(MigrateGlobal.INSTRUCTION_TYPE_SVD_BY_TEXT)) {
            result = apiService.generateVideoByTextV2(content, req);
        } else if (modal.equals(MigrateGlobal.INSTRUCTION_TYPE_MMTRACKING) && lastVideoPath != null) {
            result = apiService.motInference(lastVideoPath, req);
        } else if (modal.equals(MigrateGlobal.INSTRUCTION_TYPE_MMEDITING) && lastVideoPath != null) {
            result = apiService.mmeditingInference(lastVideoPath, req);
        } else {
            returnError(resp, out);
            return;
        }
        out.print(result);
        out.flush();
        out.close();
    }

    private void returnError(HttpServletResponse resp, PrintWriter out) {
        Map<String, Object> body = new HashMap<>();
        body.put("message", "No model detected or processing failed, please try again later.");
        body.put("code", 500);
        resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        out.print(gson.toJson(body));
        out.flush();
        out.close();
    }
}
