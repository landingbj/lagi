package ai.agent.carbus.pojo;


import lombok.*;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class ScenicSpotData {
    private String id;
    private String name;
    private String imageUrl;
    private String description;
    private String createBy;
    private String createTime;
    private String updateBy;
    private String updateTime;
    private String delFlag;
}
