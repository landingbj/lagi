package ai.config.pojo;

import lombok.Data;

@Data
public class BigdataConfig {
    private String name;
    private String driver;
    private String host;
    private Integer port;
    private String username;
    private String password;
    private Boolean enable;
}
