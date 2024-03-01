package ai.utils;

import java.io.IOException;

import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

import ai.client.AiServiceInfo;

public class HttpServiceCall {

	public static String SERVICE_AI = AiServiceInfo.AI_SERVER + "/";

	public static String Call(String url, String path) {
		return doGet(url + path);
	}

	public static String doGet(String url) {
		String result = "";
		CloseableHttpClient httpClient = HttpClients.createDefault();
		HttpGet get = new HttpGet(url);
		try {
			CloseableHttpResponse response = httpClient.execute(get);
			HttpEntity entity = response.getEntity();
			result = EntityUtils.toString(entity, "utf-8");
		} catch (IOException e) {
			e.printStackTrace();
		}
		return result;
	}
	
	public static String httpPost(String url, String json) throws IOException {
		if (url == null) {
			return null;
		}
		HttpClientBuilder httpClientBuilder = HttpClientBuilder.create();
		CloseableHttpClient httpClient = httpClientBuilder.build();
		HttpPost httpPost = new HttpPost(url);
		httpPost.setEntity(new StringEntity(json, ContentType.APPLICATION_JSON));
		String responseMessage = null;
		CloseableHttpResponse response = httpClient.execute(httpPost);
		int statusCode = response.getStatusLine().getStatusCode();
		if (statusCode == 200) {
			responseMessage = EntityUtils.toString(response.getEntity(), "UTF-8");
		}

		return responseMessage;
	}
}
