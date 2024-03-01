package ai.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import ai.client.AiServiceCall;
import ai.client.AiServiceInfo;
import ai.migrate.pojo.InstructionsResponse;
import ai.utils.pdf.PdfUtil;
import ai.utils.word.WordUtils;

public class FileAndDocUtil {

	public static List<String> downloadFile(List<String> urls, String tempPath)
			throws IOException {
		ArrayList<String> filePaths = new ArrayList<>();

		File tempDir = new File(tempPath);
		if (!tempDir.exists()) {
			tempDir.mkdirs(); // 创建临时文件夹及其父文件夹（如果不存在）
		}

		for (String url : urls) {
			String fileName = getUrlFileName(url);

			String[] split = url.split("\\.");
			String fileType = split[split.length - 1];
			String outputPath = tempPath + File.separator + fileName + "."
					+ fileType;
			URL urlObj = new URL(url);
			HttpURLConnection conn = (HttpURLConnection) urlObj
					.openConnection();
			conn.setRequestMethod("GET");
			conn.setDoOutput(true);
			conn.connect();

			OutputStream outputStream = new FileOutputStream(outputPath);
			InputStream inputStream = conn.getInputStream();
			byte[] buffer = new byte[1024];
			int bytesRead;
			while ((bytesRead = inputStream.read(buffer)) != -1) {
				outputStream.write(buffer, 0, bytesRead);
			}
			outputStream.close();
			inputStream.close();
			conn.disconnect();
			filePaths.add(outputPath);

		}
		return filePaths;
	}

	public static List<InstructionsResponse> getInstructions(
			List<String> filePathList) throws IOException {
		ArrayList<InstructionsResponse> resposeList = new ArrayList<>();
		ObjectMapper objectMapper = new ObjectMapper();
		for (String filePath : filePathList) {
			File file = new File(filePath);
			// 根据不同的类型调用不同的方法
			String[] split = filePath.split("\\.");
			String fileType = split[split.length - 1];
			String content = "";
			InputStream in = new FileInputStream(file);
			if ("pdf".equals(fileType)) {
				// 执行pdf

				content = PdfUtil.webPdfParse(in).replaceAll("[\r\n?|\n]", "");
			} else if ("txt".equals(fileType)) {
				// 执行txt

				content = FileUtils.readFileToString(file,
						StandardCharsets.UTF_8);

			} else if ("doc".equals(fileType) || "docx".equals(fileType)) {
				// 执行docx
				String extString = file.getName().substring(
						file.getName().lastIndexOf("."));
				content = WordUtils.getContentsByWord(in, extString);
			}
			in.close();

			Object[] params = { content, file.getName(), "ln_unicom" };
			AiServiceCall wsCall = new AiServiceCall();
			String returnStr = wsCall.callWS(AiServiceInfo.WSLrnUrl,
					"getInstructions", params)[0];

			JsonNode jsonNode1 = objectMapper.readTree(returnStr);
			for (JsonNode node : jsonNode1) {
				String instruction = node.get("instruction").asText();
				String input = node.get("input").asText();
				String output = node.get("output").asText();
				InstructionsResponse instructionsResponse = new InstructionsResponse(
						instruction, input, output);
				resposeList.add(instructionsResponse);
			}
		}
		return resposeList;
	}

	public static String getContent(String filePath) throws IOException {
		File file = new File(filePath);
		// 根据不同的类型调用不同的方法
		String[] split = filePath.split("\\.");
		String fileType = split[split.length - 1];
		String content = "";
		InputStream in = new FileInputStream(file);
		if ("pdf".equals(fileType)) {
			// 执行pdf

			content = PdfUtil.webPdfParse(in).replaceAll("[\r\n?|\n]", "");
		} else if ("txt".equals(fileType)) {
			// 执行txt

			content = FileUtils.readFileToString(file, StandardCharsets.UTF_8);

		} else if ("doc".equals(fileType) || "docx".equals(fileType)) {
			// 执行docx
			String extString = file.getName().substring(
					file.getName().lastIndexOf("."));
			content = WordUtils.getContentsByWord(in, extString);
		}
		in.close();
		return content;
	}

	public static String getUrlFileName(String urlString)
			throws MalformedURLException {
		URL url = null;

		url = new URL(urlString);

		String filePath = url.getFile();
		String fileName = filePath.substring(filePath.lastIndexOf("/") + 1);
		return fileName;
	}

	public static ArrayList<InstructionsResponse>  getInstructionsByContent(
			String content) throws IOException {
		Object[] params = { content, "temp", "ln_unicom" };
		AiServiceCall wsCall = new AiServiceCall();
		String returnStr = wsCall.callWS(AiServiceInfo.WSLrnUrl,
				"getInstructions", params)[0];
		ObjectMapper objectMapper = new ObjectMapper();
		JsonNode jsonNode1 = objectMapper.readTree(returnStr);
		ArrayList<InstructionsResponse> resposeList = new ArrayList<>();
		for (JsonNode node : jsonNode1) {
			String instruction = node.get("instruction").asText();
			String input = node.get("input").asText();
			String output = node.get("output").asText();
			InstructionsResponse instructionsResponse = new InstructionsResponse(
					instruction, input, output);
			resposeList.add(instructionsResponse);
		}
		return resposeList;
	}

}
