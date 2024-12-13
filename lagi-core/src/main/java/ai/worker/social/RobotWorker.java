package ai.worker.social;

import ai.agent.AgentFactory;
import ai.agent.AgentGlobal;
import ai.agent.pojo.SocialAgentParam;
import ai.agent.pojo.SocialReceiveData;
import ai.agent.pojo.SocialSendData;
import ai.agent.social.SocialAgent;
import ai.config.pojo.AgentConfig;

import java.util.concurrent.atomic.AtomicBoolean;


public class RobotWorker extends SocialWorker {
    private static final long SLEEP_TIME = 1000 * 5;
    private AtomicBoolean running = new AtomicBoolean(false);
    private final String username;

    public RobotWorker(AgentConfig agentConfig) {
        this.username = agentConfig.getApiKey();
        SocialAgentParam param = SocialAgentParam.builder()
                .username(username)
                .robotFlag(AgentGlobal.ENABLE_FLAG)
                .timerFlag(AgentGlobal.DISABLE_FLAG)
                .repeaterFlag(AgentGlobal.DISABLE_FLAG)
                .guideFlag(AgentGlobal.DISABLE_FLAG)
                .build();
        this.agent = AgentFactory.getAgent(agentConfig, param);
    }

    public RobotWorker(SocialAgent agent) {
        SocialAgentParam param = agent.getParam();
        this.username = param.getUsername();
        this.agent = agent;
    }

    @Override
    public Boolean work(Boolean data) {
        agent.connect();
        agent.start();

        while (running.get()) {
            SocialReceiveData receiveData = (SocialReceiveData) agent.receive();
            if (receiveData.getStatus().equals(AgentGlobal.SUCCESS)) {
                String text = getCompletionResult(receiveData.getData());
                SocialSendData sendData = new SocialSendData();
                sendData.setChannelUser(username);
                sendData.setText(text);
                agent.send(sendData);
            }
            sleep(SLEEP_TIME);
        }
        agent.stop();
        return null;
    }


    @Override
    public Boolean call(Boolean data) {
        return null;
    }

    @Override
    public void notify(Boolean data) {
        running.set(data);
        if(running.get()) {
            Thread workerThread = new Thread(()->work(null));
            workerThread.start();
        }
    }
}
