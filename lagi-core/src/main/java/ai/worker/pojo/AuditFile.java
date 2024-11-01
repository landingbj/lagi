package ai.worker.pojo;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class AuditFile {
    private String md5;
    private String filename;
}
