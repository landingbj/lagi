package ai.servlet.api;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import ai.image.service.ImageGenerationService;
import ai.common.pojo.Configuration;
import ai.common.pojo.ImageGenerationRequest;
import ai.common.pojo.ImageGenerationResult;
import ai.servlet.BaseServlet;
import ai.utils.LagiGlobal;

public class ImageApiServlet extends BaseServlet {
    private static final long serialVersionUID = 1L;
    private static Configuration config = LagiGlobal.config;
    private ImageGenerationService imageGenerationService = new ImageGenerationService(config);

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        req.setCharacterEncoding("UTF-8");
        resp.setHeader("Content-Type", "application/json;charset=utf-8");
        String url = req.getRequestURI();
        String method = url.substring(url.lastIndexOf("/") + 1);
        if (method.equals("generations")) {
            this.generations(req, resp);
        }
    }

    private void generations(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        resp.setContentType("application/json;charset=utf-8");
        ImageGenerationRequest request = reqBodyToObj(req, ImageGenerationRequest.class);
        ImageGenerationResult result = imageGenerationService.generations(request);
        responsePrint(resp, toJson(result));
    }
}
