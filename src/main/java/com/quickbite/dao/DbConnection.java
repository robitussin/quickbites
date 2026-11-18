package com.quickbite.dao;

import com.quickbite.exception.DataAccessException;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

/** Opens connections using the settings in config/config.properties (never hard-coded). */
public final class DbConnection {
    private static final Path CONFIG = Path.of("config", "config.properties");
    private static Properties settings;

    private DbConnection() {
    }

    private static synchronized Properties load() {
        if (settings == null) {
            Properties p = new Properties();
            try (InputStream in = Files.newInputStream(CONFIG)) {
                p.load(in);
            } catch (IOException e) {
                throw new DataAccessException("Cannot read " + CONFIG, e);
            }
            settings = p;
        }
        return settings;
    }

    public static Connection getConnection() throws SQLException {
        Properties p = load();
        return DriverManager.getConnection(
                p.getProperty("db.url"), p.getProperty("db.user"), p.getProperty("db.password"));
    }
}
