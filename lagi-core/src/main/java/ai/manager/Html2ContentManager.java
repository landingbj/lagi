package ai.manager;

import ai.common.pojo.Backend;
import ai.config.ContextLoader;
import ai.llm.adapter.ILlmAdapter;

import java.util.Objects;
import java.util.Optional;

public class Html2ContentManager extends AIManager<ILlmAdapter>{
    private static final Html2ContentManager INSTANCE = new Html2ContentManager();
public static final Boolean enable = Optional.ofNullable(ContextLoader.configuration)
        .map(config -> config.getStores())
        .map(stores -> stores.getRag().getHtml())
        .orElse(false);

    private Html2ContentManager() {

    }

    public static Html2ContentManager getInstance(){
        return INSTANCE;
    }
}
