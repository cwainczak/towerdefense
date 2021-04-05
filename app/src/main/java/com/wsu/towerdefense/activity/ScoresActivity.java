package com.wsu.towerdefense.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.wsu.towerdefense.Highscores.DBTools;
import com.wsu.towerdefense.R;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;

public class ScoresActivity extends AppCompatActivity{

    TextView test;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scores);
        onWindowFocusChanged(true);

        test = findViewById(R.id.test);

        final ResultSet results = null;
        DBTools dbt = new DBTools(new OnTaskEnded(){

            @Override
            public void onTaskEnd(ResultSet rs) {
                try {
                    ResultSetMetaData rsmd = rs.getMetaData();
                    int columnsNumber = rsmd.getColumnCount();
                    while (rs.next()) {
                        //test.setText(rs.getString(1));
                        //break;
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
        });

        dbt.execute();
    }

    public interface OnTaskEnded {

        /**
         * This method is called once the AsyncTask has completed
         */
        void onTaskEnd(ResultSet rs);
    }

    /**
     * This method is for when the back button is clicked. When the back button is clicked, it goes
     * to the MainActivity.
     *
     * @param view view
     */
    public void btnBackClicked(View view) {
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (hasFocus) {
            ActivityUtil.hideNavigator(getWindow());
        }
    }
}
