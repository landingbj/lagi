package ai.config.pojo;

import lombok.Data;

/** Optional location override for the shared Cuihua-LinkMind SQLite database. */
@Data
public class TokenLinkConfig {
    private String databasePath;
}
