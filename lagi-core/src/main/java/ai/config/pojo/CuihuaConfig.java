package ai.config.pojo;

import lombok.Data;

/** Required configuration for the read-only Cuihua account database. */
@Data
public class CuihuaConfig {
    private Boolean enabled;
    private String databasePath;
    private String userView;
}
