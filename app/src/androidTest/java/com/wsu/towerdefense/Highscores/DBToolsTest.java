package com.wsu.towerdefense.Highscores;

import android.os.AsyncTask;

import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.wsu.towerdefense.view.activity.ScoresActivity;

import org.junit.Test;
import org.junit.runner.RunWith;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;

@RunWith(AndroidJUnit4.class)
public class DBToolsTest {
    List<HighScore> highScores = new ArrayList<>();

    DBTools dbtWrite = new DBTools();
    DBTools dbtRead = new DBTools(new ScoresActivity.OnTaskEnded() {
        @Override
        public void onTaskEnd(ResultSet rs) {
            try {
                while (rs.next()) {
                    String name = rs.getString(1);
                    int score = rs.getInt(2);

                    // create new HighScore object and add it to an arrayList
                    HighScore hs = new HighScore(name, score);
                    highScores.add(hs);
                }
            } catch (SQLException throwables) {
                throwables.printStackTrace();
            }
        }
    });

    public void DBToolsTest() throws SQLException {
        dbtRead.setDBCon(DBConnection.getDBCon("HighscoresTest"));
        dbtRead.setTestStmt("DELETE FROM HIGHSCORES");
        dbtRead.execute();
        dbtRead.setTestStmt(null);
    }

    @Test
    public void testEmptyDB(){
        highScores = new ArrayList<>();

    }

    public void testOneRow(){

    }

}