package ai.servlet;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.servlet.http.Part;

import org.apache.commons.io.IOUtils;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.XML;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;

import ai.client.AiServiceCall;
import ai.client.AiServiceInfo;
import ai.lagi.service.AudioService;
import ai.lagi.service.CompletionsService;
import ai.learn.questionAnswer.KShingleFilter;
import ai.learning.pojo.IndexSearchData;
import ai.learning.service.IndexSearchService;
import ai.migrate.pojo.AsrResult;
import ai.migrate.pojo.AudioRequestParam;
import ai.migrate.pojo.ChatResponseWithContext;
import ai.migrate.pojo.Configuration;
import ai.migrate.pojo.InstructionsResponse;
import ai.migrate.pojo.QuestionAnswerRequest;
import ai.migrate.service.ApiService;
import ai.migrate.service.IntentService;
import ai.openai.pojo.ChatCompletionRequest;
import ai.openai.pojo.ChatCompletionResult;
import ai.openai.pojo.ChatMessage;
import ai.qa.LLMConfig;
import ai.utils.DownloadUtils;
import ai.utils.FileAndDocUtil;
import ai.utils.LagiGlobal;
import ai.utils.MigrateGlobal;
import ai.utils.ServiceInfoConfig;
import ai.utils.WhisperResponse;
import ai.utils.qa.ChatCompletionUtil;

@MultipartConfig(fileSizeThreshold = 1024 * 1024, maxFileSize = 1024 * 1024 * 5, maxRequestSize = 1024 * 1024 * 5)
public class SearchServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private ApiService apiService = new ApiService();
	private IntentService intentService = new IntentService();
	private Gson gson = new Gson();

	private static Configuration config = LagiGlobal.config;
	private AudioService audioService = new AudioService(config);
    private CompletionsService completionsService = new CompletionsService(config);
	
	static {
		ServiceInfoConfig.setAiServer(MigrateGlobal.AI_SERVICE_URL);
	}

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		req.setCharacterEncoding("UTF-8");

		String url = req.getRequestURI();
		String method = url.substring(url.lastIndexOf("/") + 1);

		if (method.equals("questionAnswer")) {
			this.questionAnswer(req, resp);
		}

		else if (method.equals("uploadVoice")) {
			this.uploadVoice(req, resp);
		} else if (method.equals("uploadFile")) {
			this.uploadFile(req, resp);
		}
		// else if (method.equals("generateImage")) {
		// this.generateImage(req, resp);
		// }
		else if (method.equals("generateExam")) {
			this.generateExam(req, resp);
		}
	}

	private void generateExam(HttpServletRequest req, HttpServletResponse resp)
			throws IOException {
		resp.setHeader("Content-Type", "application/json;charset=utf-8");
		String jsonString = IOUtils.toString(req.getInputStream(),
				StandardCharsets.UTF_8);
		ObjectMapper objectMapper = new ObjectMapper();
		String result = "[]";
		try {
			JsonNode jsonNode = objectMapper.readTree(jsonString);
			String content = jsonNode.get("content").asText();
			Object[] params = { content, "temp", "ln_unicom" };
			AiServiceCall wsCall = new AiServiceCall();
			String returnStr = wsCall.callWS(AiServiceInfo.WSLrnUrl,
					"getInstructions", params)[0];
			JsonNode jsonNode1 = objectMapper.readTree(returnStr);
			ArrayList<ArrayList<String>> list = new ArrayList<>();
			for (JsonNode node : jsonNode1) {
				String instruction = node.get("instruction").asText();
				String input = node.get("input").asText();
				String output = node.get("output").asText();
				ArrayList<String> inter = new ArrayList<>();
				inter.add(instruction);
				inter.add(output);
				list.add(inter);
			}
			result = objectMapper.writeValueAsString(list);
		} catch (JsonProcessingException e) {
			e.printStackTrace();
		} finally {
			PrintWriter out = resp.getWriter();
			out.print(result);
			out.flush();
			out.close();
		}

		// String jsonString = IOUtils.toString(req.getInputStream(),

	}

	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		this.doGet(req, resp);
	}

	private void uploadVoice(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		String allowedOrigin = "https://localhost";
		response.setHeader("Access-Control-Allow-Origin", allowedOrigin);
		response.setHeader("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS");
		response.setHeader("Access-Control-Allow-Headers", "Content-Type");
		response.setHeader("Access-Control-Allow-Credentials", "true");
		Part filePart = request.getPart("audioFile");

		String fileName = getFileName(filePart);

		String os = System.getProperty("os.name").toLowerCase();

		String tempFolder;
		if (os.contains("win")) {
			tempFolder = "C:\\temp\\";
		} else if (os.contains("nix") || os.contains("nux") || os.contains("mac")) {
			tempFolder = "/tmp/";
		} else {
			tempFolder = "/var/tmp/";
		}

		File tempDir = new File(tempFolder);
		if (!tempDir.exists()) {
			tempDir.mkdirs(); // 创建临时文件夹及其父文件夹（如果不存在）
		}

		String savePath = tempFolder;
		String resPath = savePath + fileName;
		String result = null;
		try (InputStream input = filePart.getInputStream();
				OutputStream output = new FileOutputStream(savePath + fileName)) {
			byte[] buffer = new byte[1024];
			int bytesRead;
			while ((bytesRead = input.read(buffer)) != -1) {
				output.write(buffer, 0, bytesRead);
			}
			result = getVoiceResult(resPath);
		} catch (IOException e) {
		    result = gson.toJson(new WhisperResponse(1, "识别失败"));
			e.printStackTrace();
		}

		response.setHeader("Content-Type", "application/json;charset=utf-8");
		PrintWriter out = response.getWriter();
		out.print(result);
		out.flush();
		out.close();
	}


	// 保留接口
	private String getVoiceResult(String resPath) throws IOException {
	    AudioRequestParam audioRequestParam = new AudioRequestParam();
	    AsrResult result = audioService.asr(resPath, audioRequestParam);
	    
	    if (result.getStatus() == LagiGlobal.ASR_STATUS_SUCCESS) {
	        return gson.toJson(new WhisperResponse(0, result.getResult()));
	    }
		return gson.toJson(new WhisperResponse(1, "识别失败"));
	}

	private void uploadFile(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {
		String allowedOrigin = "https://localhost";
		response.setCharacterEncoding("UTF-8");
		Part filePart = request.getPart("file"); // 与前端发送的FormData中的字段名对应
		String fileName = getFileName1(filePart);
		String tempFolder;
		String os = System.getProperty("os.name").toLowerCase();
		if (os.contains("win")) {
			tempFolder = "C:\\temp\\"; // Windows系统下的临时文件夹
		} else if (os.contains("nix") || os.contains("nux")
				|| os.contains("mac")) {
			tempFolder = "/tmp/"; // Unix或Mac系统下的临时文件夹
		} else {
			tempFolder = "/var/tmp/"; // 默认的临时文件夹
		}
		File tempDir = new File(tempFolder);
		if (!tempDir.exists()) {
			tempDir.mkdirs(); // 创建临时文件夹及其父文件夹（如果不存在）
		}

		String UPLOAD_DIRECTORY = tempFolder;
		// 检查文件名是否为空
		if (fileName != null && !fileName.isEmpty()) {
			try {
				// 将文件保存到指定目录
				String filePath = UPLOAD_DIRECTORY + File.separator + fileName;
				File file = new File(filePath);
				try (InputStream input = filePart.getInputStream();
						OutputStream output = new FileOutputStream(file)) {
					byte[] buffer = new byte[1024];
					int length;
					while ((length = input.read(buffer)) > 0) {
						output.write(buffer, 0, length);
					}
				}

				// 成功上传的响应
				JSONObject jsonResponse = new JSONObject();
				jsonResponse.put("message", "文件上传成功");
				response.getWriter().write(jsonResponse.toString());
				// 请接口，调用接口
				uploadFileToAPI(filePath, fileName);

			} catch (Exception e) {
				// 上传失败的响应
				JSONObject jsonResponse = new JSONObject();
				jsonResponse.put("error", "文件上传失败");
				response.getWriter().write(jsonResponse.toString());
			}
		} else {
			// 文件名为空的响应
			JSONObject jsonResponse = new JSONObject();
			jsonResponse.put("error", "没有选择文件");
			response.getWriter().write(jsonResponse.toString());
		}
	}

	private void uploadFileToAPI(String filePath, String fileName) {
	}

	private String getFileName1(Part part) {
		String contentDisposition = part.getHeader("content-disposition");
		String[] tokens = contentDisposition.split(";");
		for (String token : tokens) {
			if (token.trim().startsWith("filename")) {
				return token.substring(token.indexOf('=') + 1).trim()
						.replace("\"", "");
			}
		}
		return null;
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

	private void questionAnswer(HttpServletRequest req, HttpServletResponse resp)
			throws IOException {
		resp.setHeader("Content-Type", "application/json;charset=utf-8");
		HttpSession session = req.getSession();

		String jsonString = IOUtils.toString(req.getInputStream(),
				StandardCharsets.UTF_8);
		QuestionAnswerRequest qaRequest = gson.fromJson(jsonString,
				QuestionAnswerRequest.class);
		String category = qaRequest.getCategory();
		List<ChatMessage> messages = qaRequest.getMessages();

		ObjectMapper objectMapper = new ObjectMapper();
		String content = messages.get(messages.size() - 1).getContent().trim();
		String intent = intentService.detectIntent(content);
		String uri = req.getScheme() + "://" + req.getServerName() + ":" + req.getServerPort(); 
        PrintWriter out = resp.getWriter();
		
        String result =  "{\"status\":\"failed\"}";
        
        String lastImagePath = (String) session.getAttribute("last_image_file");
        String lastVideoPath = (String) session.getAttribute("last_video_file");
        
		// 普通对话
        if (intent.equals(MigrateGlobal.INSTRUCTION_TYPE_INSTRUCTION)) {
            // 通过路径获取文件的内容
            String lastFilePath = (String) session.getAttribute("lastFilePath");
            // 是否之前有上传的文件
            if (lastFilePath != null && !lastFilePath.equals("")) {
                // 获取内容
                String fileContent = FileAndDocUtil.getContent(lastFilePath);
                // 获取指令集
                ArrayList<InstructionsResponse> instructionsResponseList = FileAndDocUtil
                        .getInstructionsByContent(fileContent);
                Map<String, Object> map = new HashMap<String, Object>();
                map.put("status", "success");
                map.put("instructions", instructionsResponseList);
                result = objectMapper.writeValueAsString(map);
            }
		} else if (intent.equals(MigrateGlobal.INSTRUCTION_TYPE_IMAGE)) {
			result = apiService.generateImage(content, req);
		} else if (intent.equals(MigrateGlobal.INSTRUCTION_TYPE_IMAGE_TO_TEXT) && lastImagePath != null) {
            result = apiService.imageToText(lastImagePath, req);
        } else if (intent.equals(MigrateGlobal.INSTRUCTION_TYPE_ESRGAN) && lastImagePath != null) {
            result = apiService.enhanceImage(lastImagePath, req);
        } else if (intent.equals(MigrateGlobal.INSTRUCTION_TYPE_SVD) && lastImagePath != null) {
            result = apiService.generateVideo(lastImagePath, req);
        } else if (intent.equals(MigrateGlobal.INSTRUCTION_TYPE_SVD_BY_TEXT)) {
            result = apiService.generateVideoByText(content, req);
        } else if (intent.equals(MigrateGlobal.INSTRUCTION_TYPE_MMTRACKING) && lastVideoPath != null) {
            result = apiService.motInference(lastVideoPath, req);
        } else if (intent.equals(MigrateGlobal.INSTRUCTION_TYPE_MMEDITING) && lastVideoPath != null) {
            result = apiService.mmeditingInference(lastVideoPath, req);
        } else {
            Map<String, Object> map = new HashMap<>();
            Map<String, Object> qaResult = callQuestionAnswer(qaRequest,category, session);
            if(qaResult.get("status").equals("failed")){
                map.put("status", "failed");
            } else {
                map.put("status", "success");
                List<ChatResponseWithContext> responseList = (List<ChatResponseWithContext>)qaResult.get("data");
                for (ChatResponseWithContext responseWithContext: responseList) {
                    List<String> imageList = responseWithContext.getImageList();
                    if (imageList == null) {
                        continue;
                    }
                    ArrayList<String> list = new ArrayList<>();
                    for (String url: imageList) {
                        ServletContext context = req.getServletContext();
                        String rootPath = context.getRealPath("");
                        String filePath = rootPath + "static/img/imgList/";
                        File tempDir = new File( filePath);
                        if (!tempDir.exists()) {
                            tempDir.mkdirs();
                        }
                        WhisperResponse whisperResponse1= DownloadUtils.downloadFile(url, "png", filePath);
                        String urlResult = uri + "/static/img/imgList/" + whisperResponse1.getMsg();
                        list.add(urlResult);
                    }
                    responseWithContext.setImageList(list);
                }
                map.put("data", responseList);
            }
            
            result = gson.toJson(map);
        }
		out.print(result);
        out.flush();
        out.close();
	}
	
    private List<ChatResponseWithContext> getResponseList(List<ChatMessage> messages, String category, HttpSession session){ 
        String questionStr = gson.toJson(messages);
        
        ChatCompletionRequest request = new ChatCompletionRequest();
        request.setCategory(category);
        request.setMessages(messages);
        request.setTemperature(LLMConfig.TEMPERATURE);
        request.setMax_tokens(LLMConfig.LLM_MAX_TOKENS);
        String lastMessage = ChatCompletionUtil.getLastMessage(request);
        List<IndexSearchData> indexSearchDataList = IndexSearchService.search(lastMessage, request.getCategory(), null, null);
        if (indexSearchDataList != null) {
            String contextText = indexSearchDataList.get(0).getText();
            String prompt = ChatCompletionUtil.getPrompt(contextText, lastMessage);
            ChatCompletionUtil.setLastMessage(request, prompt);
        }
        ChatCompletionResult result = completionsService.completions(request);
        List<ChatResponseWithContext> responseList = new ArrayList<>();
        ChatResponseWithContext chatResponseWithContext = new ChatResponseWithContext();
        if (indexSearchDataList != null && indexSearchDataList.size() > 0) {
            IndexSearchData indexSearchData = indexSearchDataList.get(0);
            chatResponseWithContext.setContext(indexSearchData.getText());
            chatResponseWithContext.setCategory(category);
            chatResponseWithContext.setDistance(indexSearchData.getDistance());
            chatResponseWithContext.setImage(indexSearchData.getImage());
            chatResponseWithContext.setFilename(indexSearchData.getFilename());
            chatResponseWithContext.setFilepath(indexSearchData.getFilepath());
            chatResponseWithContext.setIndexId(indexSearchData.getId());
        }
        chatResponseWithContext.setText(result.getChoices().get(0).getMessage().getContent());
        responseList.add(chatResponseWithContext);
        return responseList;
    }
	
	private Map<String, Object> callQuestionAnswer(QuestionAnswerRequest qaRequest, String category, HttpSession session) throws IOException {
        List<ChatMessage> messages = qaRequest.getMessages();
        List<ChatResponseWithContext> responseList = getResponseList(messages, category, session);
        
        Map<String, Object> map = new HashMap<>();
        map.put("status", "failed");

        if (responseList == null || responseList.size() == 0) {
            return map;
        }
        String question = messages.get(messages.size() - 1).getContent();
        
        for (ChatResponseWithContext respone: responseList) {
            double threshold = 0.33d;
            double frequencyThreshold = 0.5;
            if (question.length() <= 8) {
                threshold = 0.4d;
                frequencyThreshold = 0.6;
            }
            KShingleFilter kShingleFilter = new KShingleFilter(question.length() - 1, threshold, frequencyThreshold);
            String context = respone.getContext();
            if (context != null && !kShingleFilter.isSimilar(question, context)) {
                respone.setIndexId(null);
                respone.setFilename(null);
                respone.setFilepath(null);
                respone.setAuthor(null);
                respone.setDistance(null);
                respone.setContext(null);
            }
        }
        
        if (responseList != null && responseList.size() > 0) {
           for (ChatResponseWithContext chatResp: responseList) {
                if (chatResp.getImage() == null || chatResp.getImage().equals("")) {
                    continue;
                }
                List<JsonObject> imageObjectList = gson.fromJson(chatResp.getImage(), new TypeToken<List<JsonObject>>(){}.getType());
                List<String> imageList = new ArrayList<>();
                for (JsonObject image: imageObjectList) {
                    String url = MigrateGlobal.FILE_PROCESS_URL + "/static/" + image.get("path").getAsString();
                    imageList.add(url);
                }
                chatResp.setImageList(imageList);
            }
            map.put("status", "success");
            map.put("data", responseList);
            return map;
        }
        return map;
    }
}
