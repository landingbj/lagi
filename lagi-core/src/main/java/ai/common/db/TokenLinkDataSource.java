package ai.common.db;

import ai.config.pojo.TokenLinkConfig;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.SQLException;

/**
 * Read-write data source for the small SQLite database shared by Cuihua and
 * LinkMind. Both processes may read concurrently; SQLite serializes writes.
 */
public final class TokenLinkDataSource {
    public static final String DATABASE_FILE = "tokenlink.db";
    private static final String POOL_NAME = "tokenlink";
    private static final String SQLITE_PARAMS = "busy_timeout=5000&journal_mode=WAL&synchronous=NORMAL";

    private static volatile HikariDataSource dataSource;
    private static volatile Path databasePath;

    private TokenLinkDataSource() {
    }

    public static synchronized void initialize(TokenLinkConfig config) {
        Path resolvedPath = resolveDatabasePath(config);
        HikariDataSource current = dataSource;
        if (current != null) {
            if (resolvedPath.equals(databasePath)) {
                return;
            }
            throw new IllegalStateException("tokenlink.database_path changes require a LinkMind restart");
        }

        HikariConfig hikariConfig = new HikariConfig();
        hikariConfig.setPoolName(POOL_NAME);
        hikariConfig.setDriverClassName("org.sqlite.JDBC");
        hikariConfig.setJdbcUrl("jdbc:sqlite:" + resolvedPath.toString().replace('\\', '/') + "?" + SQLITE_PARAMS);
        hikariConfig.setMaximumPoolSize(4);
        hikariConfig.setMinimumIdle(1);
        hikariConfig.setIdleTimeout(0);
        hikariConfig.setMaxLifetime(287770L);
        hikariConfig.setConnectionTimeout(5000L);
        hikariConfig.setConnectionInitSql("PRAGMA foreign_keys=ON");
        hikariConfig.setConnectionTestQuery("SELECT 1");

        HikariDataSource candidate = new HikariDataSource(hikariConfig);
        try (Connection ignored = candidate.getConnection()) {
            dataSource = candidate;
            databasePath = resolvedPath;
        } catch (SQLException e) {
            candidate.close();
            throw new IllegalStateException("cannot open tokenlink SQLite database: " + resolvedPath, e);
        }
    }

    public static Connection getConnection() throws SQLException {
        HikariDataSource current = dataSource;
        if (current == null) {
            initialize(null);
            current = dataSource;
        }
        return current.getConnection();
    }

    public static Path getDatabasePath() {
        if (databasePath == null) {
            initialize(null);
        }
        return databasePath;
    }

    public static synchronized void resetForTests() {
        HikariDataSource current = dataSource;
        dataSource = null;
        databasePath = null;
        if (current != null) {
            current.close();
        }
    }

    private static Path resolveDatabasePath(TokenLinkConfig config) {
        String configuredPath = config == null ? null : config.getDatabasePath();
        if (configuredPath != null && !configuredPath.trim().isEmpty()) {
            Path configured = Paths.get(configuredPath.trim());
            if (!configured.isAbsolute()) {
                throw new IllegalStateException("tokenlink.database_path must be an absolute SQLite file path");
            }
            return prepareParentDirectory(configured.toAbsolutePath().normalize());
        }

        String dataDir = System.getProperty(HikariDS.DATA_DIR_PROPERTY);
        Path defaultPath = dataDir == null || dataDir.trim().isEmpty()
                ? Paths.get(System.getProperty("user.dir"), DATABASE_FILE)
                : Paths.get(dataDir.trim(), DATABASE_FILE);
        return prepareParentDirectory(defaultPath.toAbsolutePath().normalize());
    }

    private static Path prepareParentDirectory(Path path) {
        Path parent = path.getParent();
        if (parent == null) {
            throw new IllegalStateException("tokenlink database path must have a parent directory");
        }
        try {
            Files.createDirectories(parent);
        } catch (IOException e) {
            throw new IllegalStateException("cannot create tokenlink database directory: " + parent, e);
        }
        if (!Files.isDirectory(parent) || !Files.isWritable(parent)) {
            throw new IllegalStateException("tokenlink database directory is not writable: " + parent);
        }
        return path;
    }
}
