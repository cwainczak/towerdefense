package com.wsu.towerdefense.view.activity;

import android.content.Intent;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import com.wsu.towerdefense.audio.AdvancedSoundPlayer;
import com.wsu.towerdefense.map.AbstractMap;
import com.wsu.towerdefense.Model.Game;
import com.wsu.towerdefense.Model.Game.Difficulty;
import com.wsu.towerdefense.MapReader;
import com.wsu.towerdefense.Model.save.Serializer;
import com.wsu.towerdefense.R;
import com.wsu.towerdefense.Settings;
import com.wsu.towerdefense.Util;
import java.util.ArrayList;
import java.util.List;

public class MapSelectionActivity extends AppCompatActivity {

    private AdvancedSoundPlayer audioButtonPress;

    private List<ImageView> mapList;
    private TextView txt_mapName;
    private Button btn_play;
    private Button btn_easy;
    private Button btn_medium;
    private Button btn_hard;
    private Button selected_difficulty;
    private LinearLayout imageContainer;

    private AbstractMap selectedMap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map_selection);
        onWindowFocusChanged(true);

        audioButtonPress = new AdvancedSoundPlayer(R.raw.ui_button_press);

        txt_mapName = findViewById(R.id.txt_selectedMapName);
        imageContainer = findViewById(R.id.imageContainer);
        btn_play = findViewById(R.id.btn_playMapSelection);
        btn_easy = findViewById(R.id.btn_easyMapSelection);
        btn_medium = findViewById(R.id.btn_mediumMapSelection);
        btn_hard = findViewById(R.id.btn_hardMapSelection);

        addImageViews();

        btn_play.setEnabled(false);
        selected_difficulty = btn_easy;
        btn_medium.setTextColor(getResources().getColor(R.color.not_selected_text, null));
        btn_hard.setTextColor(getResources().getColor(R.color.not_selected_text, null));
        btn_medium.setTextColor(getColor(R.color.not_selected_text));
        btn_hard.setTextColor(getColor(R.color.not_selected_text));
    }

    private void addImageViews() {
        final int imageWidth = Util.dpToPixels(getResources(), 275);
        final int imageHeight = Util.dpToPixels(getResources(), 173);
        final int marginStart = Util.dpToPixels(getResources(), 10);
        final int marginEnd = Util.dpToPixels(getResources(), 20);

        mapList = new ArrayList<>();

        for (AbstractMap map : MapReader.getMaps()) {
            ImageView image = new ImageView(this);
            image.setImageResource(map.getImageID());
            image.setOnClickListener(this::mapSelected);
            image.setTag(map.getName());

            LayoutParams layout = new LayoutParams(imageWidth, imageHeight);
            layout.setMarginStart(marginStart);
            layout.setMarginEnd(marginEnd);
            image.setLayoutParams(layout);

            mapList.add(image);
            imageContainer.addView(image);
        }
    }

    private int dpToPixels(int dp) {
        return (int) TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP,
            dp,
            getResources().getDisplayMetrics()
        );
    }

    private void showText(String str) {
        if (str != null) {
            txt_mapName.setText(str);
            txt_mapName.setVisibility(View.VISIBLE);
        } else {
            txt_mapName.setText("");
            txt_mapName.setVisibility(View.INVISIBLE);
        }
    }

    /**
     * This method is for when the play button is clicked. If the play button is clicked before a
     * map is selected, the txt_mapName displays an error message. Otherwise it goes to the
     * GameActivity
     *
     * @param view view
     */
    public void btnPlayClicked(View view) {
        audioButtonPress.play(view.getContext(), Settings.getSFXVolume(view.getContext()));

        if (selectedMap != null) {
            // delete save file when new game is started
            Serializer.delete(this, Serializer.SAVEFILE);

            // open game
            Intent intent = new Intent(this, GameActivity.class);
            intent.putExtra("map", selectedMap.getName());
            intent.putExtra("difficulty", getDifficulty());
            startActivity(intent);
        }
    }

    /**
     * This method is for when the back button is clicked. When the back button is clicked, it goes
     * to the GameSelectionActivity.
     *
     * @param view view
     */
    public void btnBackClicked(View view) {
        audioButtonPress.play(view.getContext(), Settings.getSFXVolume(view.getContext()));

        finish();
    }

    public void btnEasyClicked(View view) {
        audioButtonPress.play(view.getContext(), Settings.getSFXVolume(view.getContext()));

        selected_difficulty.setTextColor(getColor(R.color.not_selected_text));
        selected_difficulty = btn_easy;
        selected_difficulty.setTextColor(getColor(android.R.color.white));
    }

    public void btnMediumClicked(View view) {
        audioButtonPress.play(view.getContext(), Settings.getSFXVolume(view.getContext()));

        selected_difficulty.setTextColor(getColor(R.color.not_selected_text));
        selected_difficulty = btn_medium;
        selected_difficulty.setTextColor(getColor(android.R.color.white));
    }

    public void btnHardClicked(View view) {
        audioButtonPress.play(view.getContext(), Settings.getSFXVolume(view.getContext()));

        selected_difficulty.setTextColor(getColor(R.color.not_selected_text));
        selected_difficulty = btn_hard;
        selected_difficulty.setTextColor(getColor(android.R.color.white));
    }

    /**
     * This method is for when one of the ImageView objects representing game maps is clicked. It
     * sets the text of the txt_mapName to the map name and sets its visibility to visible.
     *
     * @param view view
     */
    public void mapSelected(View view) {
        audioButtonPress.play(view.getContext(), Settings.getSFXVolume(view.getContext()));

        ImageView imageView = (ImageView) view;
        for (int i = 0; i < mapList.size(); i++) {

            if (mapList.get(i).isPressed()) {
                // select map
                selectedMap = MapReader.get((String) imageView.getTag());
                // update ui
                btn_play.setEnabled(true);
                showText(selectedMap.getDisplayName());
                return;
            }
        }
    }

    public Difficulty getDifficulty() {
        if (selected_difficulty.getId() == btn_easy.getId()) {
            return Game.Difficulty.EASY;
        } else if (selected_difficulty.getId() == btn_medium.getId()) {
            return Game.Difficulty.MEDIUM;
        } else {
            return Game.Difficulty.HARD;
        }
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
            Util.hideNavigator(getWindow());
        }
    }
}
