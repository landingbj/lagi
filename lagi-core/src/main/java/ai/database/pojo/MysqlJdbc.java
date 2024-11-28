package ai.database.pojo;
import lombok.Data;

@Data
public class MysqlJdbc {
    private String name;
    private String jdbcUrl;
    private String driverClassName;
    private String username;
    private String password;
}
