package com.wsu.towerdefense.Highscores;

import android.os.AsyncTask;
import android.widget.TextView;

import com.wsu.towerdefense.activity.ScoresActivity;

import org.w3c.dom.Text;

import java.sql.Connection;
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

    @Override
    protected ResultSet doInBackground(String... strings) {
        try {
            DBCon = DBConnection.getDBCon();
            return getResultSet("HIGHSCORES");
        } catch (SQLException throwables) {
            throwables.printStackTrace();
            return null;
        }
    }

    @Override
    protected void onPostExecute(ResultSet rs) {
        listener.onTaskEnd(rs);
    }

    public ResultSet getResultSet(String tableName) throws SQLException {
        if (DBCon == null) {
            DBCon = DBConnection.getDBCon();
        }

        stmt = DBCon.createStatement();
        // SQL query to get all data from DB sorted by score in descending order
        // and limiting the result to 5
        String query = "SELECT * FROM " + tableName + " ORDER BY Score DESC LIMIT 5;";
        rs = stmt.executeQuery(query);
        return rs;
    }


    /**
     * Used for testing. Takes a ResultSet as a parameter and prints the data in
     * that ResultSet.
     * Note: once a ResultSet is printed it is empty - data will not appear in the program elsewhere
     *
     * @param rs ResultSet to print data from
     */
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
