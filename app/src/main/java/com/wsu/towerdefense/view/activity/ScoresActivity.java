package com.wsu.towerdefense.view.activity;

import android.os.Bundle;
import android.view.View;
import androidx.appcompat.app.AppCompatActivity;
import com.wsu.towerdefense.R;
import com.wsu.towerdefense.audio.AdvancedSoundPlayer;

public class ScoresActivity extends AppCompatActivity {

    private AdvancedSoundPlayer audioButtonPress;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scores);
        onWindowFocusChanged(true);

        audioButtonPress = new AdvancedSoundPlayer(R.raw.ui_button_press);
    }

    /**
     * This method is for when the back button is clicked. When the back button is clicked, it goes
     * to the MainActivity.
     *
     * @param view view
     */
    public void btnBackClicked(View view) {
        audioButtonPress.play(view.getContext());

        finish();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        audioButtonPress.release();
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (hasFocus) {
            ActivityUtil.hideNavigator(getWindow());
        }
    }
}
