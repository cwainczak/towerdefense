package com.wsu.towerdefense.Highscores;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DBConnection {

    public static Connection getDBCon() throws SQLException {
        try {
            Class.forName("com.mysql.jdbc.Driver");
            String userName = "amir";
            String port = "3306";
            String password = ""; // password removed
            String dbName = "high_scores";
            String hostname = "cais-310-mysql.cctdhx8tknhb.us-east-1.rds.amazonaws.com";

            String jdbcUrl = "jdbc:mysql://" + hostname + ":" + port + "/" + dbName + "?user=" + userName + "&password=" + password;

            return DriverManager.getConnection(jdbcUrl);
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
            return null;
        }
    }
}
