package ai.config;

import ai.config.pojo.GeneralConfig;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ConfigUtilCascadeProxyTest {

    private final GlobalConfigurations previousConfiguration = ContextLoader.configuration;

    @AfterEach
    void restoreConfiguration() {
        ContextLoader.configuration = previousConfiguration;
    }

    @Test
    void cascadeProxyRequiresExplicitGeneralConfiguration() {
        GlobalConfigurations configurations = new GlobalConfigurations();
        GeneralConfig general = new GeneralConfig();
        configurations.setGeneral(general);
        ContextLoader.configuration = configurations;

        assertFalse(ConfigUtil.isCascadeProxyEnabled());
        assertEquals("", ConfigUtil.getConfiguredCascadeApiAddress());

        general.setCascadeApiAddress("https://cascade.example.com/v1/chat/completions");

        assertTrue(ConfigUtil.isCascadeProxyEnabled());
        assertEquals("https://cascade.example.com", ConfigUtil.getConfiguredCascadeApiAddress());
    }
}
