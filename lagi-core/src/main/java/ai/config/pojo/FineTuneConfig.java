package ai.config.pojo;

import lombok.*;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@ToString
public class FineTuneConfig {
    private Integer isLocal;
    private String env;
    private String envPath;
    private String datasetDir;
    private String saveAdapterDir;
    private String saveModelDir;
    private String saveFineTuneDir;
    private String llamaFactoryDir;
    private String trainDir;
    private String saveDir;
    private List<String> ports;
}
