package ai.common.db;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.SQLException;

public class HikariDS {
    /** System property key for data directory override (directory containing saas.db). */
    public static final String DATA_DIR_PROPERTY = "ai.data.dir";

    private static volatile HikariConfig saasConfig;
    private static volatile HikariDataSource saasDS;
    private static final String SAAS_CONFIG_PATH = "/hikari-saas.properties";
    private static final String SAAS_DB_FILE = "saas.db";
    private static final String SQLITE_PARAMS = "enable_load_extension=true&busy_timeout=5000&journal_mode=WAL&synchronous=NORMAL";

    static {
        resetDataSource(null);
    }

    private HikariDS() {
    }

    public static Connection getConnection(String conname) throws SQLException {
        HikariConfig config = saasConfig;
        HikariDataSource dataSource = saasDS;
        if (conname.equals(config.getPoolName())) {
            return dataSource.getConnection();
        }
        return null;
    }

    /**
     * Replaces the singleton SQLite connection pool for an isolated test and
     * returns the previous JDBC URL via {@link #getJdbcUrlForTests()}.
     * Production initialization remains fixed for the life of the process.
     */
    public static synchronized void setJdbcUrlForTests(String jdbcUrl) {
        if (jdbcUrl == null || jdbcUrl.trim().isEmpty()) {
            throw new IllegalArgumentException("jdbcUrl is required");
        }
        resetDataSource(jdbcUrl.trim());
    }

    /** Returns the active JDBC URL so a test can restore the original pool. */
    public static synchronized String getJdbcUrlForTests() {
        return saasConfig.getJdbcUrl();
    }

    private static void resetDataSource(String jdbcUrlOverride) {
        HikariDataSource previous = saasDS;
        if (previous != null) {
            previous.close();
        }
        HikariConfig config = new HikariConfig(SAAS_CONFIG_PATH);
        if (jdbcUrlOverride != null) {
            config.setJdbcUrl(jdbcUrlOverride);
        } else {
            String dataDir = System.getProperty(DATA_DIR_PROPERTY);
            if (dataDir != null && !dataDir.isEmpty()) {
                Path dbPath = Paths.get(dataDir).resolve(SAAS_DB_FILE).toAbsolutePath().normalize();
                String jdbcUrl = "jdbc:sqlite:" + dbPath.toString().replace('\\', '/') + "?" + SQLITE_PARAMS;
                config.setJdbcUrl(jdbcUrl);
            }
        }
        saasConfig = config;
        saasDS = new HikariDataSource(config);
    }
}
