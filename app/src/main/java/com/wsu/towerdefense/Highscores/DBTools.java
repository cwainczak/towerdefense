package com.wsu.towerdefense.Highscores;

import android.os.AsyncTask;

import com.wsu.towerdefense.activity.ScoresActivity;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;

public class DBTools extends AsyncTask<String, Integer, ResultSet> {

    Connection DBCon;
    Statement stmt;
    ResultSet rs;
    public ScoresActivity.OnTaskEnded listener;

    public DBTools(ScoresActivity.OnTaskEnded onTaskEnded) {
        listener = onTaskEnded;
    }

    public DBTools(){

    }

    @Override
    protected ResultSet doInBackground(String... strings) {
        try {
            DBCon = DBConnection.getDBCon();
            if (listener == null){
                addScoreToDB("HIGHSCORES", "Davin", 1000000000);
            }
            rs = getResultSet("HIGHSCORES");
            return rs;
        } catch (SQLException throwables) {
            throwables.printStackTrace();
            return null;
        }
    }

    @Override
    protected void onPostExecute(ResultSet rs) {
        if (listener != null){
            listener.onTaskEnd(rs);
        }
    }

    public ResultSet getResultSet(String tableName) throws SQLException{
        if (DBCon == null) {
            DBCon = DBConnection.getDBCon();
        }

        stmt = DBCon.createStatement();
        String query = "SELECT * FROM " + tableName + ";";
        rs = stmt.executeQuery(query);
        return rs;
    }

    public void addScoreToDB(String tableName, String username, int score) throws SQLException {
        if (DBCon == null) {
            DBCon = DBConnection.getDBCon();
        }

        PreparedStatement query = DBCon.prepareStatement("INSERT INTO " + tableName + " values(?, ?);");
        query.setString(1, username);
        query.setInt(2, score);
        query.execute();
        query.close();
    }

    public void printResultSet(ResultSet rs) {
        try {
            ResultSetMetaData rsmd = rs.getMetaData();
            int columnsNumber = rsmd.getColumnCount();
            while (rs.next()) {
                for (int i = 1; i <= columnsNumber; i++) {
                    if (i > 1) System.out.print(",  ");
                    String columnValue = rs.getString(i);
                    System.out.print(columnValue + " " + rsmd.getColumnName(i));
                }
                System.out.println();
            }
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
    }
}
