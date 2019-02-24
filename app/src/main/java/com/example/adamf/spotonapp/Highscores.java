package com.example.adamf.spotonapp;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.text.method.ScrollingMovementMethod;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

/**
 * Created by adamf on 5/3/2017.
 */

public class Highscores extends AppCompatActivity {


    ArrayList<Integer> myScores = new ArrayList<>();
    ArrayList<String> myDates = new ArrayList<>();
    int todayHigh;
    int highest;


    TextView highestTV;
    TextView topTen;
    TextView dailyHighTV;

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            if (Objects.equals(item.getTitle().toString(), "Home")) {
                Intent home = new Intent(this, Home.class);
                startActivity(home);
            } else if (Objects.equals(item.getTitle().toString(), "My Highscores")) {
                Intent high = new Intent(this, Highscores.class);
                startActivity(high);
            }
        }
        return true;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.highscore_screen);

        Calendar today = Calendar.getInstance();
        SimpleDateFormat df = new SimpleDateFormat("MMM-dd-yyyy");
        String myDaily = df.format(today.getTime());

        highestTV = (TextView) findViewById(R.id.highest);
        topTen = (TextView) findViewById(R.id.topTen);
        dailyHighTV = (TextView) findViewById(R.id.dailyHigh);

        SharedPreferences prefs = getSharedPreferences("HIGHSCORES",
                MODE_PRIVATE);
        highest = prefs.getInt("highestScore", 0);
        todayHigh = prefs.getInt("dailyHigh" + myDaily, 0);

        for (int i = 0; i < 10; i++) {
            int temp = prefs.getInt("highScore" + i, 0);
            myScores.add(temp);
            String string = prefs.getString("date" + i, "");
            myDates.add(string);
        }

        DisplayMetrics displayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        int height = displayMetrics.heightPixels;
        int width = displayMetrics.widthPixels;

        highestTV.setTextSize(height / 30);

        dailyHighTV.setTextSize(height / 30);
        dailyHighTV.setGravity(Gravity.CENTER);

        topTen.setTextSize(height / 30);
        topTen.setHeight(height);
        topTen.setGravity(Gravity.CENTER);
        topTen.setMovementMethod(new ScrollingMovementMethod());

        highestTV.setText("Highest Score Ever\n" + highest);
        dailyHighTV.setText("Today's Highscore:\n" + todayHigh);
        topTen.setText("Top 10 scores:");


        for (int i = 0; i < 10; i++)
            if (myScores.get(i) != 0) {
                topTen.setText(topTen.getText() + "\n" + myScores.get(i) + " -\t\t" + myDates.get(i));
            }

    }

}
