package ai.database.pojo;

import lombok.*;

@Data
@ToString
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class TableColumnInfo {
    private String tableName;
    private String tableType;
    private String columnName;
    private String columnType;
    private int columnSize;
    private String columnRemark;
}
