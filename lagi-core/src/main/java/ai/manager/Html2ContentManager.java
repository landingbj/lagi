package ai.manager;

import ai.common.pojo.Backend;
import ai.config.ContextLoader;
import ai.llm.adapter.ILlmAdapter;

import java.util.Objects;

public class Html2ContentManager extends AIManager<ILlmAdapter>{
    private static final Html2ContentManager INSTANCE = new Html2ContentManager();
    public static final  Boolean enable =
        ContextLoader.configuration.getFunctions().getHtml2content() != null &&
                ContextLoader.configuration.getFunctions().getHtml2content().stream()
                .filter(Objects::nonNull)
                .anyMatch(Backend::getEnable);

    private Html2ContentManager() {

    }

    public static Html2ContentManager getInstance(){
        return INSTANCE;
    }
}
