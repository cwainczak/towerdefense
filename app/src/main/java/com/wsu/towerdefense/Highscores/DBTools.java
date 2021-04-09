package com.wsu.towerdefense.Highscores;

import android.os.AsyncTask;

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
    public DBListener.OnTaskEnded listener;
    private String testStmt;

    private boolean testMode = false;
    private boolean testWrite = false;
    private int currentScore;
    private String currentUsername;

    //used for reading from DB
    public DBTools(DBListener.OnTaskEnded onTaskEnded) {
        listener = onTaskEnded;
    }

    //used for updating db values
    public DBTools(){}

    @Override
    protected ResultSet doInBackground(String... strings) {
        try {
            DBCon = DBConnection.getDBCon();
            if (listener == null || (testMode && testWrite)){
                addScoreToDB("HIGHSCORES", this.currentUsername, this.currentScore);
            }

            if(testMode && testStmt != null) {

                executeStatement(testStmt);
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

    public void initUsernameAndScore(String username, int score){
        this.currentUsername = username;
        this.currentScore = score;
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

    //only used during testing
    public void setDBCon(Connection DBCon){
        this.DBCon = DBCon;
        testMode = true;
    }

    /**
     * Used in testing to execute statements onto the test table
     *
     * @param statement - statement to be executed
     */
    public void executeStatement(String statement) throws SQLException {
        if (DBCon == null) {
            DBCon = DBConnection.getDBCon();
        }

        PreparedStatement query = DBCon.prepareStatement(statement);
        query.execute();
        query.close();
    }

    public void setTestStmt(String stmt){
        this.testStmt = stmt;
    }

    public boolean isTestMode(){
        return testMode;
    }

    public void setTestWrite(boolean write){
        this.testWrite = write;
    }

}
