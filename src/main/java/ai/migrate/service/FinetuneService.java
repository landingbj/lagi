package ai.migrate.service;

import java.util.List;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import ai.client.AiServiceCall;
import ai.client.AiServiceInfo;
import ai.migrate.pojo.Instruction;

public class FinetuneService {
    private AiServiceCall wsCall = new AiServiceCall();
    private Gson gson = new Gson();

    public void addFinetuneInstructions(List<Instruction> instructionList) {

    }

    public void addInstructions(String content, String filename, String category) {
        Object[] params = { content, filename, "smartqa" };
        String returnStr = wsCall.callWS(AiServiceInfo.WSLrnUrl, "getInstructions", params)[0];
        List<Instruction> instructionList = gson.fromJson(returnStr, new TypeToken<List<Instruction>>(){}.getType());
        
    }
}
