package ai.config;

import ai.common.pojo.Configuration;

public abstract class AbstractConfiguration {

    public void init() {

    }

    public abstract  Configuration transformToConfiguration();
}
