
package com.bank.util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DBConnection {

    private static final String URL =
        System.getenv("DB_URL");

    private static final String USERNAME =
        System.getenv("DB_USERNAME");

    private static final String PASSWORD =
        System.getenv("DB_PASSWORD");

    public static Connection getConnection() throws SQLException {

        try {
            Class.forName("com.mysql.cj.jdbc.Driver");

            if (URL == null || USERNAME == null || PASSWORD == null) {
                throw new SQLException(
                    "Database environment variables are missing"
                );
            }

            return DriverManager.getConnection(
                URL, USERNAME, PASSWORD
            );

        } catch (ClassNotFoundException e) {
            throw new SQLException("MySQL JDBC Driver not found", e);
        }
    }
}
