package com.example.whitetiles;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.animation.ObjectAnimator;
import android.content.DialogInterface;
import android.content.Intent;
import android.media.SoundPool;
import android.os.Bundle;
import android.os.Handler;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.Button;
import android.widget.GridLayout;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class MainActivity extends AppCompatActivity {
    GridLayout gridView;
    ViewTreeObserver viewObserver;
    List<ObjectAnimator> objectAnimations;
    TextView tvRoundNumber, tvScore;
    ImageView logo;

    final int gridRows = 3;
    final int gridPages = 8;
    final int initialDelay = 12000;
    final int acceleration = 200;
    final int rounds = 2;

    int globalScore = 0;
    SoundPool soundPool;
    int[] sounds = new int[4];

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        gridView = findViewById(R.id.grid_layout);
        viewObserver = gridView.getViewTreeObserver();
        objectAnimations = new ArrayList<>();

        logo = findViewById(R.id.image_logo);
        tvRoundNumber = findViewById(R.id.text_round);
        tvScore = findViewById(R.id.text_score);

        soundPool = new SoundPool.Builder().setMaxStreams(5).build();
        sounds[0] = soundPool.load(this, R.raw.sound_re, 1);
        sounds[1] = soundPool.load(this, R.raw.sound_mi, 1);
        sounds[2] = soundPool.load(this, R.raw.sound_sol, 1);
        sounds[3] = soundPool.load(this, R.raw.sound_la, 1);

        if (viewObserver.isAlive()) {
            viewObserver.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                @Override
                public void onGlobalLayout() {
                    gridView.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                    int h = gridView.getHeight();

                    int rows = gridRows;
                    int pages = gridPages;
                    int size = h / rows;

                    for (int i = 0; i < pages * rows; i++) {
                        GridLayout row = createRow(gridView, i, size);
                        objectAnimations.add(
                                ObjectAnimator
                                .ofFloat(row, "translationY", (pages - 1) * -h, 0)
                        );
                    }
                }
            });
        }

        final Button btnPlay = findViewById(R.id.button_play);
        btnPlay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                for (final ObjectAnimator oa : objectAnimations) {
                    final int[] separators = generateSeparators();
                    colorizeSeparators(separators);
                    colorizeGrid();
                    btnPlay.setVisibility(View.GONE);
                    logo.setVisibility(View.GONE);
                    setScore(0);
                    setRound(1);
                    oa.setDuration(initialDelay);
                    oa.start();

                    for (int i = 1; i < rounds; i++) {
                        final int j = i;
                        new Handler().postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                colorizeGrid();
                                setRound(j + 1);
                                oa.start();
                                oa.setDuration(initialDelay - (j * acceleration));
                            }
                        }, i * initialDelay);
                    }
                }

                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        btnPlay.setVisibility(View.VISIBLE);
                        logo.setVisibility(View.VISIBLE);
                        setRound(-1);
                        setScore(-1);
                        showResults();
                    }
                }, rounds * initialDelay - (initialDelay / gridPages));
            }
        });
    }

    private GridLayout createRow(GridLayout wrapper, int row, int height) {
        GridLayout rowGrid = new GridLayout(this);

        GridLayout.LayoutParams rowGridParams = new GridLayout.LayoutParams();
        rowGridParams.width = GridLayout.LayoutParams.MATCH_PARENT;
        rowGridParams.height = height;

        rowGrid.setColumnCount(4);
        rowGrid.setLayoutParams(rowGridParams);

        GridLayout.Spec rowSpec = GridLayout.spec(row, 1, GridLayout.FILL, 1f);

        for (int i = 0; i < 4; i++) {
            GridLayout.Spec columnSpec = GridLayout.spec(i, 1, GridLayout.FILL, 1f);

            Button button = new Button(this);

            String buttonText = String.valueOf(row) + '_' + i;
            button.setTag(buttonText);
            button.setBackgroundResource(R.color.whiteTile);
            button.setSoundEffectsEnabled(false);

            GridLayout.LayoutParams param = new GridLayout.LayoutParams();
            param.rowSpec = rowSpec;
            param.columnSpec = columnSpec;
            param.setMargins(2, 2, 2, 2);

            rowGrid.addView(button, param);
        }

        wrapper.addView(rowGrid);
        return rowGrid;
    }

    private int[] generateSeparators() {
        final int[] separators = new int[4];
        for (int i = 0; i < 4; i++)
            separators[i] = new Random().nextInt(4);
        return separators;
    }

    private void colorizeSeparators(int[] separators) {
        for (int i1 = 0; i1 < 4; i1++) {
            int i = 3 - i1;
            int j = objectAnimations.size() - i1 - 1;

            Button btn_i;
            Button btn_j;

            for (int i2 = 0; i2 < 4; i2++) {
                btn_i = gridView.findViewWithTag(i + "_" + i2);
                btn_j = gridView.findViewWithTag(j + "_" + i2);
                btn_i.setBackgroundResource(R.color.whiteTile);
                btn_j.setBackgroundResource(R.color.whiteTile);
                btn_i.setEnabled(false);
                btn_j.setEnabled(false);
            }

            btn_i = gridView.findViewWithTag(i + "_" + separators[i1]);
            btn_j = gridView.findViewWithTag(j + "_" + separators[i1]);
            btn_i.setBackgroundResource(R.color.blackTile);
            btn_j.setBackgroundResource(R.color.blackTile);
            btn_i.setEnabled(true);
            btn_j.setEnabled(true);
            increaseScore(btn_i);
            increaseScore(btn_j);
        }
    }

    private void colorizeGrid() {
        for (int i1 = 4; i1 < objectAnimations.size() - 4; i1++) {
            for (int i2 = 0; i2 < 4; i2++) {
                Button button = gridView.findViewWithTag(i1 + "_" + i2);
                button.setBackgroundResource(R.color.whiteTile);
                button.setEnabled(false);
            }

            int r = new Random().nextInt(4);
            Button button = gridView.findViewWithTag(i1 + "_" + r);
            button.setBackgroundResource(R.color.blackTile);
            button.setEnabled(true);
            increaseScore(button);
        }
    }

    private void setRound(int round) {
        String roundText = getString(R.string.round_text) + " " + round;
        if (round == -1) roundText = "";
        tvRoundNumber.setText(roundText);
    }

    private void increaseScore(final Button button) {
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                globalScore++;
                setScore(globalScore);
                button.setEnabled(false);
                button.setBackgroundResource(R.color.whiteTile);

                String tag = button.getTag().toString();
                String[] result = tag.split("_");
                int soundIndex = Integer.parseInt(result[1]);
                soundPool.play(sounds[soundIndex], 1, 1, 0, 0, 1);
            }
        });
    }

    private void setScore(int score) {
        if (score == 0) globalScore = 0;
        String scoreText = getString(R.string.score_text) + " " + (score * 10);
        if (score == -1) scoreText = "";
        tvScore.setText(scoreText);
    }

    private void showResults() {
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);

        dialogBuilder.setTitle(getString(R.string.alert_result_title));
        dialogBuilder.setIcon(R.drawable.ic_save);

        String roundsString = " round" + (rounds != 1 ? "s" : "");
        int points = globalScore * 10;

        dialogBuilder
            .setMessage("You made " + points + " points in " + rounds + roundsString + "!")
            .setPositiveButton(R.string.alert_result_yes, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                // TODO: save to High Scores
                }
            })
            .setNegativeButton(R.string.alert_result_no, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                dialog.cancel();
                }
            });

        AlertDialog adResults = dialogBuilder.create();
        adResults.show();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.activity_main_menu, menu);
        return true;
    }

    public void menuLeaderboard(MenuItem item) {
        Intent intent = new Intent(this, LeaderboardActivity.class);
        startActivity(intent);
    }
}