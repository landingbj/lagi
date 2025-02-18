package ai.worker.pojo;

import lombok.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@ToString
public class UserRagVector {
    private int id;                    // 主键
    private String userId;             // 用户ID
    private String defaultCategory;    // 默认类别
    private int similarityTopK;        // 相似度Top K
    private double similarityCutoff;   // 相似度阈值
    private int parentDepth;           // 父节点深度
    private int childDepth;            // 子节点深度
}
