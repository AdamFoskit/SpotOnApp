package java.com.example.adamf.spotonapp;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.RequiresApi;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.adamf.spotonapp.Highscores;
import com.example.adamf.spotonapp.Home;
import com.example.adamf.spotonapp.R;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;

import static com.example.adamf.spotonapp.R.layout.game_screen;

/**
 * Created by adamf on 5/3/2017.
 */

public class GameScreen extends AppCompatActivity implements SensorEventListener {

    private static final long UPDATE_PERIOD = 300;
    private static final int SHAKE_THRESHOLD = 600;
    static int lifeCounter;
    static String formattedDate;
    private static int screenHeight;
    private static int screenWidth;
    private static int BUGSIZE;
    Context myContext;
    ArrayList<Integer> myScores = new ArrayList();
    ArrayList<String> myDates = new ArrayList<>();
    boolean shakeable = false;
    AlertDialog myDialog;
    //Layout variables
    LinearLayout life_container;
    RelativeLayout bugLayout;
    TextView highestScore;
    TextView currentHighscore;
    TextView myLevel;
    int highest;
    int highScoreDay;
    MediaPlayer hit;
    MediaPlayer miss;
    MediaPlayer disappear;
    int todayHigh;
    String myDaily;
    View myLifeView;
    int bugCounter = 0;
    int BASE_RED = 150;
    int BASE_GREEN = 100;
    int currentLevel = 1;
    int bugsKilled = 0;
    int bugsMissed = 0;
    long POINTS_GREEN = 35;
    long POINTS_RED = 75;
    int GREEN_COUNTER = 0;
    int CURRENT_POINTS = 0;
    //Shake detector variables
    private Sensor accelerometer;
    private SensorManager sm;
    private long curTime, lastUpdate;
    private float x, y, z, last_x, last_y, last_z;
    //Variables for animating
    private Handler handler = new Handler();
    private Timer timer = new Timer();

    @Override
    public void onPause() {
        super.onPause();
        timer.cancel();
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(game_screen);

        //Get the width/height of screen
        DisplayMetrics displayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        screenHeight = displayMetrics.heightPixels;
        screenWidth = displayMetrics.widthPixels;

        //Sounds
        hit = MediaPlayer.create(this, R.raw.hit);
        miss = MediaPlayer.create(this, R.raw.miss);
        disappear = MediaPlayer.create(this, R.raw.disappear);

        //Declare static variables
        life_container = (LinearLayout) findViewById(R.id.lives_container);
        highestScore = (TextView) findViewById(R.id.highscoreTitle);
        currentHighscore = (TextView) findViewById(R.id.currentScore);
        bugLayout = (RelativeLayout) findViewById(R.id.bugLayout);
        myLevel = (TextView) findViewById(R.id.myLevel);

        Calendar today = Calendar.getInstance();
        SimpleDateFormat df = new SimpleDateFormat("MMM-dd-yyyy h:mm:ss a");
        formattedDate = df.format(today.getTime());

        SimpleDateFormat dailyFormat = new SimpleDateFormat("MMM-dd-yyyy");
        myDaily = dailyFormat.format(today.getTime());


        //Shared Preferences
        SharedPreferences prefs = getSharedPreferences("HIGHSCORES", MODE_PRIVATE);
        highest = prefs.getInt("highestScore", 0);
        todayHigh = prefs.getInt("dailyHigh" + myDaily, 0);

        for (int i = 0; i < 10; i++) {
            highScoreDay = prefs.getInt("highScore" + i, 0);
            myScores.add(highScoreDay);
        }

        //Sort the highscroes then order them most to least
        Collections.sort(myScores);
        Collections.reverse(myScores);

        bugLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                miss.start();
                if (CURRENT_POINTS > 0) {
                    CURRENT_POINTS -= 100 * currentLevel;
                    if (CURRENT_POINTS < 0)
                        CURRENT_POINTS = 0;
                }
            }
        });

        BUGSIZE = screenWidth / 11;

        //Set sizes
        highestScore.setTextSize(screenWidth / 45);
        currentHighscore.setTextSize(screenWidth / 45);
        myLevel.setTextSize(screenWidth / 45);

        if (highest > 0)
            highestScore.setText(highestScore.getText() + " " + highest);


        //Declare sensor variables
        myContext = GameScreen.this;

        sm = (SensorManager) getSystemService(SENSOR_SERVICE);
        accelerometer = sm.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        sm.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_NORMAL);
        curTime = lastUpdate = (long) 0.0;
        x = y = z = last_x = last_y = last_z = (float) 0.0;


        lifeCounter = 0;

        myContext = GameScreen.this;
        myLevel.setText("Level " + currentLevel);


        //Give the player lives
        for (int i = 0; i < 3; i++)
            addLife();

        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                handler.post(new Runnable() {
                    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR1)
                    @Override
                    public void run() {
                        if (bugCounter < 4)
                            addBug();
                        if (bugsKilled % 11 == 10) {
                            currentLevel++;
                            myLevel.setText("Level " + currentLevel);
                            bugsKilled = 1;
                        }
                        if (bugsMissed % 8 == 7) {
                            removeLife();
                            bugsMissed = 1;
                        }
                        if (CURRENT_POINTS > highest)
                            highestScore.setText("High Score: " + CURRENT_POINTS);
                        currentHighscore.setText("Current Score: " + CURRENT_POINTS);
                        if (GREEN_COUNTER % 8 == 7) {
                            GREEN_COUNTER = 1;
                            addLife();
                        }
                    }
                });
            }
        }, 10, 50);
    }

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR1)
    public void addBug() {
        Random rand = new Random();

        if (bugCounter < 4) {
            if (rand.nextBoolean())
                addRedBug();
            else
                addGreenBug();
            bugCounter++;
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR1)
    public void addRedBug() {
        final Timer redTimer = new Timer();

        DisplayMetrics displayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        screenHeight = displayMetrics.heightPixels;
        screenWidth = displayMetrics.widthPixels;

        LayoutInflater layoutInflater =
                (LayoutInflater) getBaseContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        final View addView = layoutInflater.inflate(R.layout.red_bug, null);

        final ImageView redBug = (ImageView) addView.findViewById(R.id.redBug);
        redBug.setImageResource(R.drawable.red_spot);

        //Set the height/width of the bug
        final LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) redBug.getLayoutParams();
        params.height = BUGSIZE;
        params.width = BUGSIZE;

        Random rand = new Random();
        final int myX = Math.abs(rand.nextInt(screenWidth - 300));
        int myY = Math.abs(rand.nextInt(screenHeight - screenHeight / 2));
        params.leftMargin = myX + 200;
        params.topMargin = myY;

        redBug.setLayoutParams(params);

        redBug.setOnTouchListener(new View.OnTouchListener() {
            private GestureDetector gestureDetector = new GestureDetector(myContext, new GestureDetector.SimpleOnGestureListener() {
                @Override
                public boolean onDoubleTap(MotionEvent e) {
                    hit.start();
                    ((RelativeLayout) addView.getParent()).removeView(addView);
                    redTimer.cancel();
                    bugCounter--;
                    bugsKilled++;
                    CURRENT_POINTS += (POINTS_RED * currentLevel);
                    return super.onDoubleTap(e);
                }
            });

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                gestureDetector.onTouchEvent(event);
                return true;
            }
        });


        final Runnable redRun = new Runnable() {
            @Override
            public void run() {
                moveBug(this, redTimer, redBug, params.leftMargin, params.topMargin, params.width, params.height);
            }
        };


        bugLayout.addView(addView);

        redTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                handler.post(redRun);
            }
        }, 0, (int) Math.round(BASE_RED / (1 + (currentLevel * .5))));

    }

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR1)
    public void addGreenBug() {

        final Timer greenTimer = new Timer();

        DisplayMetrics displayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        screenHeight = displayMetrics.heightPixels;
        screenWidth = displayMetrics.widthPixels;

        LayoutInflater layoutInflater =
                (LayoutInflater) getBaseContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        final View addView = layoutInflater.inflate(R.layout.green_bug, null);

        final ImageView greenBug = (ImageView) addView.findViewById(R.id.greenBug);
        greenBug.setImageResource(R.drawable.green_spot);

        //Set the height/width of the bug
        final LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) greenBug.getLayoutParams();
        params.height = BUGSIZE;
        params.width = BUGSIZE;

        Random rand = new Random();
        int myX = Math.abs(rand.nextInt(screenWidth - 300));
        int myY = Math.abs(rand.nextInt(screenHeight - screenHeight / 2));
        params.leftMargin = myX + 200;
        params.topMargin = myY;

        greenBug.setLayoutParams(params);

        final Runnable greenRun = new Runnable() {
            @Override
            public void run() {
                moveBug(this, greenTimer, greenBug, params.leftMargin, params.topMargin, params.width, params.height);
            }
        };

        greenBug.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                hit.start();
                ((RelativeLayout) addView.getParent()).removeView(addView);
                greenTimer.cancel();
                bugCounter--;
                bugsKilled++;
                GREEN_COUNTER++;
                CURRENT_POINTS += (POINTS_GREEN * currentLevel);
            }
        });

        bugLayout.addView(addView);

        greenTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                handler.post(greenRun);
            }
        }, 0, (int) Math.round(BASE_GREEN / (1 + (currentLevel * .5))));

    }

    public void moveBug(Runnable greenRun, Timer greenTimer, ImageView myBug, float pLeft, float pTop, int pWidth, int pHieght) {
        LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) myBug.getLayoutParams();

        if (params.height > 3) {
            params.height = pWidth - 1;

        }
        if (params.width > 3) {
            params.width = pHieght - 1;

        }
        if (params.height <= 3) {
            greenTimer.cancel();
            myBug.removeCallbacks(greenRun);
            myBug.setImageResource(android.R.color.transparent);
            myBug.destroyDrawingCache();
            bugCounter--;
            bugsMissed++;
            disappear.start();

            if (CURRENT_POINTS > 0) {
                CURRENT_POINTS -= 100;
                if (CURRENT_POINTS < 0)
                    CURRENT_POINTS = 0;
            }

        }

        if (pLeft < screenWidth - 150) {
            params.leftMargin = (int) pLeft + 3;
            myBug.setLayoutParams(params);
        }
        if (pTop < screenHeight - screenHeight / 2) {
            params.topMargin = (int) pTop + 1;
            myBug.setLayoutParams(params);
        }
        if (pTop >= screenHeight - screenHeight / 3) {
            myBug.setImageResource(android.R.color.transparent);
            myBug.destroyDrawingCache();
            greenTimer.cancel();
            bugCounter--;
        }
        if (pLeft >= screenWidth - 100) {
            myBug.setImageResource(android.R.color.transparent);
            myBug.destroyDrawingCache();
            greenTimer.cancel();
            bugCounter--;
        }
    }

    public void addLife() {
        LayoutInflater layoutInflater =
                (LayoutInflater) getBaseContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        myLifeView = layoutInflater.inflate(R.layout.add_lives, null);

        final ImageView mylife = (ImageView) myLifeView.findViewById(R.id.myLife);
        mylife.setImageResource(R.drawable.life);

        //Set the height/width of the lives to adjust to screen size
        LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) mylife.getLayoutParams();
        params.width = screenWidth / 25;
        params.height = screenHeight / 15;
        mylife.setLayoutParams(params);

        lifeCounter++;
        life_container.addView(myLifeView);
    }


    public void removeLife() {
        if (lifeCounter > 1) {
            life_container.removeViewAt(0);
            lifeCounter--;
        } else {
            life_container.removeViewAt(0);
            lifeCounter--;
            shakeable = true;
            timer.cancel();

            SharedPreferences.Editor editor = getSharedPreferences("HIGHSCORES",
                    MODE_PRIVATE).edit();

            //Highest score
            if (CURRENT_POINTS > highest) {
                editor.putInt("highestScore", CURRENT_POINTS);
                Toast.makeText(getApplicationContext(), "New highest score!",
                        Toast.LENGTH_SHORT).show();
            }

            //Today's highest score
            if (CURRENT_POINTS > todayHigh) {
                editor.putInt("dailyHigh" + myDaily, CURRENT_POINTS);
            }
            SharedPreferences prefs = getSharedPreferences("HIGHSCORES",
                    MODE_PRIVATE);

            for (int i = 0; i < 10; i++) {
                String string = prefs.getString("date" + i, "");
                myDates.add(string);
            }

            //Top 10 scores
            for (int i = 0; i < 10; i++)
                if (CURRENT_POINTS > myScores.get(i)) {
                    editor.putInt("highScore" + i, CURRENT_POINTS);
                    editor.putString("date" + i, formattedDate);
                    for (int c = 9; c > i; c--) {
                        editor.putInt("highScore" + c, myScores.get(c - 1));
                        editor.putString("date" + c, myDates.get(c - 1));
                    }
                    break;
                }
            editor.apply();


            myDialog = new AlertDialog.Builder(myContext)
                    .setTitle("Game Over!")
                    .setMessage("You scored " + CURRENT_POINTS + " points!\nShake the screen to go to your highscores.")
                    .setPositiveButton("Play Again!", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                            finish();
                            Intent play = new Intent(myContext, GameScreen.class);
                            startActivity(play);
                        }
                    })
                    .setNegativeButton("Go Home", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                            finish();
                            Intent home = new Intent(myContext, Home.class);
                            startActivity(home);
                        }
                    })
                    .setIcon(android.R.drawable.star_big_on).setOnCancelListener(new DialogInterface.OnCancelListener() {
                        @Override
                        public void onCancel(DialogInterface dialog) {
                            shakeable = false;
                        }
                    })
                    .setCancelable(false)
                    .show();

        }
    }

    @Override
    public void onSensorChanged(SensorEvent event) {

        if (!shakeable)
            return;

        curTime = System.currentTimeMillis();

        if ((curTime - lastUpdate) > UPDATE_PERIOD) {
            long diffTime = (curTime - lastUpdate);
            lastUpdate = curTime;

            x = event.values[0];
            y = event.values[1];
            z = event.values[2];

            float speed = Math.abs(x + y + z - last_x - last_y - last_z) / diffTime * 10000;

            //SHAKE EVENT HERE
            if (speed > SHAKE_THRESHOLD) {
                myDialog.dismiss();
                finish();
                Intent highscore = new Intent(this, Highscores.class);
                startActivity(highscore);
                shakeable = false;
            }

            last_x = x;
            last_y = y;
            last_z = z;
        }
    }
    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy){}

}
