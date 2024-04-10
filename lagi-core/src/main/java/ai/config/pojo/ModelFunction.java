package ai.config.pojo;

import ai.common.pojo.Backend;
import lombok.Data;

import java.util.List;

@Data
public class ModelFunction {
    private List<Backend> backends;
}
