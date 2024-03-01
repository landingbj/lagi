/*
 * This program is commercial software; you can only redistribute it and/or modify
 * it under the WARRANTY of Beijing Landing Technologies Co. Ltd.
 *
 * You should have received a copy license along with this program;
 * If not, write to Beijing Landing Technologies, service@landingbj.com.
 */

/*
 * HikariDS.java
 * Copyright (C) 2020 Beijing Landing Technologies, China
 */

/**
 * 
 */

package ai.migrate.db;

import ai.utils.MigrateGlobal;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

import javax.sql.DataSource;

public class HikariDS {
	private static boolean _DEBUG_1 = false;
	private static boolean _DEBUG_2 = false;

	static {
		if (MigrateGlobal._DEBUG_LEVEL >= 2) {
			_DEBUG_2 = true;
		}
		if (MigrateGlobal._DEBUG_LEVEL >= 1) {
			_DEBUG_1 = true;
		}
	}

	private static HikariConfig landingbjConfig;
	private static HikariDataSource landingbjDS;
	private static final String LANDINGBJ_CONFIG_PATH = "/hikari-saas.properties";

	static {
		landingbjConfig = new HikariConfig(LANDINGBJ_CONFIG_PATH);
		landingbjDS = new HikariDataSource(landingbjConfig);

		try {
            initializeDatabase(getConnection("saas"));
        } catch (SQLException e) {
            e.printStackTrace();
        }
	}
	
    private static void initializeDatabase(Connection conn) throws SQLException {
        // SQL语句用于创建两张表
        String sqlCreateTable1 = "CREATE TABLE IF NOT EXISTS lagi_user(id int auto_increment primary key,"
                + " category varchar(32) not null, category_create_time datetime not null);";

        try (Statement stmt = conn.createStatement()) {
            stmt.execute(sqlCreateTable1);
        }
    }

	private HikariDS() {
	}

	public static Connection getConnection(String conname) throws SQLException {
		Connection conn = null;
		if (conname.equals(landingbjConfig.getPoolName())) {
			conn = landingbjDS.getConnection();
		}
		return conn;
	}

	public static DataSource getDataSource(String conname) throws SQLException {
		DataSource ds = null;
		if (conname.equals(landingbjConfig.getPoolName())) {
			ds = landingbjDS;
		}
		return ds;
	}
}