package java.com.example.adamf.spotonapp;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.os.Handler;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.example.adamf.spotonapp.GameScreen;
import com.example.adamf.spotonapp.Highscores;
import com.example.adamf.spotonapp.R;

import java.util.Objects;

/**
 * An example full-screen activity that shows and hides the system UI (i.e.
 * status bar and navigation/system bar) with user interaction.
 */
public class Home extends AppCompatActivity {

    Button play;
    TextView title;
    TextView highestScore;
    int highest;

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

        setContentView(R.layout.home);

        SharedPreferences prefs = getSharedPreferences("HIGHSCORES",
                MODE_PRIVATE);

        highest = prefs.getInt("highestScore", 0);

        play = (Button) findViewById(R.id.button);
        title = (TextView) findViewById(R.id.title);
        highestScore = (TextView) findViewById(R.id.highscoreTitle);

        DisplayMetrics displayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        int height = displayMetrics.heightPixels;
        int width = displayMetrics.widthPixels;

        title.setTextSize(height / 15);

        play.setHeight(height / 8);

        highestScore.setTextSize(height / 30);
        highestScore.setWidth(width);
        highestScore.setGravity(Gravity.CENTER);

        highestScore.setText("Highest Score Ever\n" + highest);

        play.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                goPlay();
            }
        });

    }

    public void goPlay() {
        Intent game = new Intent(this, GameScreen.class);
        startActivity(game);
    }
}
