package com.wsu.towerdefense;

import static com.wsu.towerdefense.Model.Highscores.DBListener.*;
import static org.junit.Assert.assertEquals;
import com.wsu.towerdefense.Model.Highscores.DBConnection;
import com.wsu.towerdefense.Model.Highscores.DBTools;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

@RunWith(JUnit4.class)
public class DBToolsTest {
    List<HighScore> highScores = new ArrayList<>();
    boolean operationComplete = false;
    OnTaskEnded listener = new OnTaskEnded() {
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
                operationComplete = true;

            } catch (SQLException throwables) {
                throwables.printStackTrace();
            }
        }
    };

    private void resetDB() throws InterruptedException, SQLException {
        DBTools dbt = new DBTools(listener);
        dbt.setDBCon(DBConnection.getDBCon(true));
        System.out.println("connected");

        highScores = new ArrayList<>();
        dbt.setTestStmt("DELETE FROM EASY");
        dbt.execute();

        while (!operationComplete){
            Thread.sleep(500);
        }
        operationComplete = false;
        dbt.setTestStmt(null);
    }

    private void addHighscore(String name, int score) throws SQLException, InterruptedException {
        DBTools dbt = new DBTools(listener);
        dbt.setDBCon(DBConnection.getDBCon(true));

        highScores = new ArrayList<>();
        dbt.setTestWrite(true);
        dbt.initUsernameAndScore(name, score);
        dbt.execute();
        while(!operationComplete){
            Thread.sleep(500);
        }
        operationComplete = false;
        dbt.setTestWrite(false);
    }

    @Test
    public void testEmptyDB() throws InterruptedException, SQLException {
        resetDB();

        assertEquals(true, highScores.isEmpty());
    }

    @Test
    public void testOneRow() throws InterruptedException, SQLException {
        HighScore expected = new HighScore("John", 500);
        resetDB();
        addHighscore("John", 500);

        assertEquals(1, highScores.size());
        assertEquals(expected.getName(), highScores.get(0).getName());
        assertEquals(expected.getScore(), highScores.get(0).getScore());
    }

    @Test
    public void testTwoRows() throws SQLException, InterruptedException {
        List<HighScore> expected = new ArrayList<>();
        highScores = new ArrayList<>();
        expected.add(new HighScore("Cena", 9001));
        expected.add(new HighScore("John", 500));

        resetDB();
        addHighscore("John", 500);
        addHighscore("Cena", 9001);

        assertEquals(2, highScores.size());
        for(int i = 0; i < highScores.size(); i++){
            assertEquals(expected.get(i).getName(), highScores.get(i).getName());
            assertEquals(expected.get(i).getScore(), highScores.get(i).getScore());
        }
    }

    //Query Limits results to 5, so max rows that can be tested is 5
    @Test
    public void testFiveRows() throws SQLException, InterruptedException {
        List<HighScore> expected = new ArrayList<>();
        highScores = new ArrayList<>();
        expected.add(new HighScore("Tyler", 191919));
        expected.add(new HighScore("Amir", 9001));
        expected.add(new HighScore("Davin", 8080));
        expected.add(new HighScore("Edward", 6000));
        expected.add(new HighScore("Cooper", 1711));

        resetDB();
        addHighscore("Edward", 6000);
        addHighscore("Cooper", 1711);
        addHighscore("Amir", 9001);
        addHighscore("Davin", 8080);
        addHighscore("Tyler", 191919);

        assertEquals(5, highScores.size());
        for(int i = 0; i < highScores.size(); i++){
            assertEquals(expected.get(i).getName(), highScores.get(i).getName());
            assertEquals(expected.get(i).getScore(), highScores.get(i).getScore());
        }
    }
}