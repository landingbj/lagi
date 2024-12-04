package ai.database.pojo;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class SQLJdbc {
    private String name;
    @JsonProperty("jdbcUrl")
    private String jdbcUrl;
    @JsonProperty("driverClassName")
    private String driverClassName;
    private String username;
    private String password;
}
