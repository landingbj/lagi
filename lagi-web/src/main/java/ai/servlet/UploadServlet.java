package ai.servlet;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.Part;

//@WebServlet("/upload")
//@MultipartConfig
public class UploadServlet extends HttpServlet {
	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		req.setCharacterEncoding("UTF-8");

		String url = req.getRequestURI();
		String method = url.substring(url.lastIndexOf("/") + 1);

	
		if (method.equals("uploadVoice")) {
			this.uploadVoice(req, resp);
		}
		
	}
	
	
	private void uploadVoice(HttpServletRequest request, HttpServletResponse response)
	            throws ServletException, IOException {
	        // 获取上传的文件部分
	        Part filePart = request.getPart("audioFile"); // "audioFile" 应与前端上传字段名相匹配

	        // 获取文件名
	        String fileName = getFileName(filePart);

	        // 指定保存文件的目标路径
	        String savePath = "/path/to/save/files/"; // 替换为实际的文件保存路径

	        try (InputStream input = filePart.getInputStream();
	             OutputStream output = new FileOutputStream(savePath + fileName)) {
	            // 将文件保存到指定路径
	            byte[] buffer = new byte[1024];
	            int bytesRead;
	            while ((bytesRead = input.read(buffer)) != -1) {
	                output.write(buffer, 0, bytesRead);
	            }
	        }

	        response.getWriter().println("文件上传成功！");
	    }

	    private String getFileName(Part part) {
	        for (String content : part.getHeader("content-disposition").split(";")) {
	            if (content.trim().startsWith("filename")) {
	                return content.substring(content.indexOf('=') + 1).trim().replace("\"", "");
	            }
	        }
	        return null;
	    }
	
	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		this.doGet(req, resp);
	}
	
	
//	private void uploadVoice(HttpServletRequest req, HttpServletResponse resp) throws IOException {
//		resp.setHeader("Content-Type", "application/json;charset=utf-8");
//		HttpSession session = req.getSession();
//		
//		String jsonString = IOUtils.toString(req.getInputStream(), StandardCharsets.UTF_8);
//		QuestionAnswerRequest qaRequest = gson.fromJson(jsonString, QuestionAnswerRequest.class);
//		String category = qaRequest.getCategory();
//		String questionStr = gson.toJson(qaRequest.getMessages());
//		int channelId = qaRequest.getChannelId();
//		AiServiceCall wsCall = new AiServiceCall();
//
//		Object[] params = { questionStr, category, session.getId(), ""};
//		String returnStr = wsCall.callWS(AiServiceInfo.WSKngUrl, "questionAnswerSessionTag", params)[0];
//		
//		List<ChatResponseWithContext> responseList = null;
//		try {
//			JSONObject xmlJSONObj = XML.toJSONObject(returnStr);
//			responseList = gson.fromJson(xmlJSONObj.getString("QuestionResult"), new TypeToken<List<ChatResponseWithContext>>() {
//			}.getType());
//		} catch (JSONException je) {
//			je.printStackTrace();
//		}
//		
//		Map<String, Object> map = new HashMap<>();
//		
//		if (responseList != null && responseList.size() > 0) {
//			map.put("status", "success");
//			map.put("data", responseList);
//		} else {
//			map.put("status", "failed");
//		}
//
//		PrintWriter out = resp.getWriter();
//		out.print(gson.toJson(map));
//		out.flush();
//		out.close();
//	}
}
