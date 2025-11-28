package com.khoa.notebooklm.desktop.model;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import javax.sql.DataSource;

public class Database {
    private static HikariDataSource dataSource;

    public static DataSource get() {
        if (dataSource == null) {
            DatabaseConfig cfg = new DatabaseConfig();
            HikariConfig hc = new HikariConfig();
            hc.setJdbcUrl(cfg.url());
            hc.setUsername(cfg.username());
            hc.setPassword(cfg.password());
            hc.setMaximumPoolSize(10);
            hc.setConnectionTimeout(20_000);
            dataSource = new HikariDataSource(hc);
        }
        return dataSource;
    }

    public static void shutdown() {
        if (dataSource != null) dataSource.close();
    }
}
