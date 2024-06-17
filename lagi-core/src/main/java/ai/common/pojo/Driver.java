package ai.common.pojo;

import ai.config.pojo.OSSConfig;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Data
public class Driver {
    private String model;
    private String driver;
    private OSSConfig oss;
}
