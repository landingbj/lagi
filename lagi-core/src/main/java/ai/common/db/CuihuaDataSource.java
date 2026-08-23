package ai.common.db;

import ai.config.pojo.CuihuaConfig;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import java.io.IOException;
import java.nio.file.FileStore;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Locale;
import java.util.regex.Pattern;

/** Dedicated read-only connection pool for the Cuihua-owned SQLite file. */
public final class CuihuaDataSource {
    private static final Pattern VIEW_NAME = Pattern.compile("[A-Za-z_][A-Za-z0-9_]*");
    private static final String POOL_NAME = "cuihua";

    private static volatile HikariDataSource dataSource;
    private static volatile Path databasePath;
    private static volatile String userView;

    private CuihuaDataSource() {
    }

    public static synchronized void initialize(CuihuaConfig config) {
        if (config == null) {
            throw new IllegalStateException("cuihua configuration is required");
        }
        if (!Boolean.TRUE.equals(config.getEnabled())) {
            throw new IllegalStateException("cuihua.enabled must be true");
        }
        String configuredPath = required(config.getDatabasePath(), "cuihua.database_path is required");
        String configuredView = required(config.getUserView(), "cuihua.user_view is required");
        if (!VIEW_NAME.matcher(configuredView).matches()) {
            throw new IllegalStateException("cuihua.user_view must be a simple SQLite identifier");
        }

        Path path = Paths.get(configuredPath);
        if (!path.isAbsolute()) {
            throw new IllegalStateException("cuihua.database_path must be an absolute SQLite file path");
        }
        Path realPath = resolveLocalDatabase(path);

        if (dataSource != null) {
            if (realPath.equals(databasePath) && configuredView.equals(userView)) {
                return;
            }
            throw new IllegalStateException("Cuihua database configuration changes require a LinkMind restart");
        }

        HikariConfig hikariConfig = new HikariConfig();
        hikariConfig.setPoolName(POOL_NAME);
        hikariConfig.setDriverClassName("org.sqlite.JDBC");
        hikariConfig.setJdbcUrl(buildReadOnlyJdbcUrl(realPath));
        hikariConfig.setMaximumPoolSize(4);
        hikariConfig.setMinimumIdle(1);
        hikariConfig.setIdleTimeout(0);
        hikariConfig.setMaxLifetime(287770L);
        hikariConfig.setConnectionTimeout(5000L);
        hikariConfig.setConnectionInitSql("PRAGMA query_only=ON");
        hikariConfig.setConnectionTestQuery("SELECT 1");

        HikariDataSource candidate = new HikariDataSource(hikariConfig);
        try (Connection ignored = candidate.getConnection()) {
            dataSource = candidate;
            databasePath = realPath;
            userView = configuredView;
        } catch (SQLException e) {
            candidate.close();
            throw new IllegalStateException("cannot open Cuihua SQLite file in read-only mode: " + realPath, e);
        }
    }

    public static Connection getConnection() throws SQLException {
        HikariDataSource current = dataSource;
        if (current == null) {
            throw new SQLException("Cuihua data source has not been initialized");
        }
        return current.getConnection();
    }

    public static String getUserView() {
        String current = userView;
        if (current == null) {
            throw new IllegalStateException("Cuihua data source has not been initialized");
        }
        return current;
    }

    public static Path getDatabasePath() {
        return databasePath;
    }

    public static synchronized void resetForTests() {
        HikariDataSource current = dataSource;
        dataSource = null;
        databasePath = null;
        userView = null;
        if (current != null) {
            current.close();
        }
    }

    private static Path resolveLocalDatabase(Path configuredPath) {
        final Path realPath;
        try {
            realPath = configuredPath.toRealPath();
        } catch (IOException e) {
            throw new IllegalStateException("Cuihua SQLite file does not exist or is inaccessible: " + configuredPath, e);
        }
        if (!Files.isRegularFile(realPath) || !Files.isReadable(realPath)) {
            throw new IllegalStateException("Cuihua database_path must be a readable regular file: " + realPath);
        }
        Path parent = realPath.getParent();
        if (parent == null || !Files.isReadable(parent)) {
            throw new IllegalStateException("Cuihua SQLite parent directory is not accessible: " + realPath);
        }
        try {
            FileStore fileStore = Files.getFileStore(realPath);
            if (isNetworkFileSystem(fileStore.type())) {
                throw new IllegalStateException("Cuihua SQLite file must be on a local file system, not " + fileStore.type());
            }
        } catch (IOException e) {
            throw new IllegalStateException("cannot inspect Cuihua SQLite file system: " + realPath, e);
        }
        return realPath;
    }

    private static boolean isNetworkFileSystem(String type) {
        String normalized = type == null ? "" : type.toLowerCase(Locale.ROOT);
        return normalized.contains("nfs")
                || normalized.contains("smb")
                || normalized.contains("cifs")
                || normalized.contains("afp")
                || normalized.contains("sshfs");
    }

    private static String buildReadOnlyJdbcUrl(Path realPath) {
        return "jdbc:sqlite:" + realPath.toUri().toString() + "?mode=ro&busy_timeout=5000";
    }

    private static String required(String value, String message) {
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalStateException(message);
        }
        return value.trim();
    }
}
