package ai.database.pojo;

import lombok.Data;

import java.util.Map;
@Data
public class MysqlObject {
    private Map<Object, Object> data;
}
