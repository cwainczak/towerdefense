package com.wsu.towerdefense.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import androidx.appcompat.app.AppCompatActivity;
import com.wsu.towerdefense.R;

public class ScoresActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scores);
        onWindowFocusChanged(true);
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
