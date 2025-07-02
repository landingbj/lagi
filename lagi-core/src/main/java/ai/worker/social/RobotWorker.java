package ai.worker.social;

import ai.config.pojo.PnpConfig;
import ai.pnps.SocialPnpFactory;
import ai.pnps.PnpGlobal;
import ai.pnps.pojo.SocialPnpParam;
import ai.pnps.pojo.SocialReceiveData;
import ai.pnps.pojo.SocialSendData;
import ai.pnps.social.SocialPnp;

import java.util.concurrent.atomic.AtomicBoolean;


public class RobotWorker extends SocialWorker {
    private static final long SLEEP_TIME = 1000 * 5;
    private AtomicBoolean running = new AtomicBoolean(false);
    private final String username;

    public RobotWorker(PnpConfig pnpConfig) {
        this.username = pnpConfig.getApiKey();
        SocialPnpParam param = SocialPnpParam.builder()
                .username(username)
                .robotFlag(PnpGlobal.ENABLE_FLAG)
                .timerFlag(PnpGlobal.DISABLE_FLAG)
                .repeaterFlag(PnpGlobal.DISABLE_FLAG)
                .guideFlag(PnpGlobal.DISABLE_FLAG)
                .build();
        this.agent = SocialPnpFactory.getPnp(pnpConfig, param);
    }

    public RobotWorker(SocialPnp agent) {
        SocialPnpParam param = agent.getParam();
        this.username = param.getUsername();
        this.agent = agent;
    }

    @Override
    public Boolean work(Boolean data) {
        agent.connect();
        agent.start();

        while (running.get()) {
            SocialReceiveData receiveData = (SocialReceiveData) agent.receive();
            if (receiveData.getStatus().equals(PnpGlobal.SUCCESS)) {
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
