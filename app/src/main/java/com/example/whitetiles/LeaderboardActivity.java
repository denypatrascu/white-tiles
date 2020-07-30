package com.example.whitetiles;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class LeaderboardActivity extends AppCompatActivity {
    ListView leaderboardListView;
    Map<String, Integer> leaderboardMap = new HashMap<>();
    List<String> formattedLeaderboard = new ArrayList<>();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_leaderboard);

        leaderboardListView = findViewById(R.id.list_leaderboard);

        leaderboardMap.put("Dan", 300);
        leaderboardMap.put("John", 500);
        leaderboardMap.put("Alex", 470);
        leaderboardMap.put("RandomGuy", 330);
        leaderboardMap.put("George", 410);
        leaderboardMap.put("Anonymous", 375);

        sortAndFill();
    }

    void sortAndFill() {
        Comparator<Map.Entry<String, Integer>> comparator = new Comparator<Map.Entry<String, Integer>>() {
            @Override
            public int compare(Map.Entry<String, Integer> e1, Map.Entry<String, Integer> e2) {
                return -1 * e1.getValue().compareTo(e2.getValue());
            }
        };

        List<Map.Entry<String, Integer>> leaderboardArray = new ArrayList<>(leaderboardMap.entrySet());
        Collections.sort(leaderboardArray, comparator);

        int position = 1;
        for (Map.Entry<String, Integer> i: leaderboardArray) {
            formattedLeaderboard.add(position + ". " + i.getValue() + " points - " + i.getKey());
            position++;
        }

        ArrayAdapter<String> leaderboardAdapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_list_item_1,
                formattedLeaderboard
        );
        leaderboardListView.setAdapter(leaderboardAdapter);
    }
}
