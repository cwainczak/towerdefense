package com.wsu.towerdefense.Highscores;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DBConnection {

    public static Connection getDBCon() throws SQLException{
        try{
            Class.forName("com.mysql.cj.jdbc.Driver");
            String userName = "tlafleurWSU";
            String port = "3306";
            String password = "";
            String dbName = "qacs_ch02";
            String hostname = "test.cznsvkc1cyzq.us-east-1.rds.amazonaws.com";

            String jdbcUrl = "jdbc:mysql://" + hostname + ":" + port + "/" + dbName + "?user=" + userName + "&password=" + password;

            return DriverManager.getConnection(jdbcUrl);
        }catch (ClassNotFoundException e){
            e.printStackTrace();
            return null;
        }
    }
}
