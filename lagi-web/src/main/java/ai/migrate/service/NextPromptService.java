package ai.migrate.service;

import ai.pnps.pojo.ChannelUser;
import ai.pnps.pojo.ChannelUserResponse;
import ai.pnps.pojo.GetAppListResponse;
import ai.pnps.service.RpaService;
import ai.servlet.dto.NextPromptRequest;
import ai.servlet.dto.NextPromptResponse;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class NextPromptService {
    private static final String GET_QR_CODE = "GET_QR_CODE";
    private static final RpaService rpaService = new RpaService();
    private static final Map<String, Integer> appMap;

    static {
        appMap = new ConcurrentHashMap<>();
        GetAppListResponse response = rpaService.getAppTypeList();
        if (response.getStatus().equals("success")) {
            response.getData().forEach(app -> appMap.put(app.getAppName(), app.getAppId()));
        }
    }

    public NextPromptResponse nextPrompt(NextPromptRequest nextPromptRequest) {
        NextPromptResponse response = null;
        if (GET_QR_CODE.equals(nextPromptRequest.getAction())) {
            response = detectGetQrCode(nextPromptRequest);
        }
        return response;
    }

    private NextPromptResponse detectGetQrCode(NextPromptRequest nextPromptRequest) {
        NextPromptResponse response = new NextPromptResponse();
        ChannelUserResponse channelUserResponse = rpaService.getChannelUser();
        if (channelUserResponse.getStatus().equals("failed")) {
            response.setStatus("failed");
            return response;
        } else {
            ChannelUser channelUser = channelUserResponse.getData();
            response.setChannelId(channelUser.getChannelId());
            response.setUsername(channelUser.getNickname());
        }
        String prompt = nextPromptRequest.getPrompt();
        StringBuilder appIdStr = new StringBuilder();
        for (String appName : appMap.keySet()) {
            if (prompt.contains(appName)) {
                appIdStr.append(appMap.get(appName)).append(",");
            }
        }
        if (appIdStr.length() == 0) {
            response.setStatus("failed");
            return response;
        }
        response.setAppId(appIdStr.substring(0, appIdStr.length() - 1));
        response.setStatus("success");
        return response;
    }
}
