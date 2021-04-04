package com.wsu.towerdefense.Highscores;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;

public class DBTools {

    Connection DBCon;
    Statement stmt;
    ResultSet rs;

    public DBTools() throws SQLException, ClassNotFoundException {
        Connection conn = DBConnection.getDBCon();
    }

    public ResultSet getResultSet(String tableName) throws SQLException, ClassNotFoundException {
        if (DBCon == null) {
            DBCon = DBConnection.getDBCon();
        }

        stmt = DBCon.createStatement();
        String query = "SELECT * FROM " + tableName + ";";
        rs = stmt.executeQuery(query);

        printResultSet(rs);
        return rs;
    }

    private void printResultSet(ResultSet rs) {
        try {
            ResultSetMetaData rsmd = rs.getMetaData();
            int columnsNumber = rsmd.getColumnCount();
            while (rs.next()) {
                for (int i = 1; i <= columnsNumber; i++) {
                    if (i > 1) System.out.print(",  ");
                    String columnValue = rs.getString(i);
                    System.out.print(columnValue + " " + rsmd.getColumnName(i));
                }
                System.out.println("");
            }
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
    }
}
