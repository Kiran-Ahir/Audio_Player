package com.example.audioplayer;

import android.app.Activity;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.widget.ImageButton;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.annotation.RequiresApi;
import androidx.core.app.NotificationCompat;

public class MainActivity extends Activity {

    ImageButton open, play, pause, stop;
    TextView tv;
    SeekBar sb;
    //  MediaPlayer mediaPlayer = null;
    GlobalClass gc;
    int duration = 0;
    boolean finish = false;
    boolean pauseFinish = false;
    int current = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        gc = (GlobalClass)getApplication();
        //  It doesn't create object but returns object created by android at the time application starts.

        open = findViewById(R.id.open);
        play = findViewById(R.id.play);
        pause = findViewById(R.id.pause);
        stop = findViewById(R.id.sto);
        tv = findViewById(R.id.tv);
        sb = findViewById(R.id.sb);

        sb.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {

                if (fromUser) {
                    gc.mediaPlayer.seekTo(progress);
                }

            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

                current = seekBar.getProgress();
                gc.mediaPlayer.seekTo(current * 1000);
                tv.setText("" + current + "/" + duration);

            }
        });

        play.setOnClickListener(v -> {
            if (gc.mediaPlayer != null) {
                pauseFinish = false;
                gc.mediaPlayer.start();
            }
        });

        pause.setOnClickListener(v -> {
            if (gc.mediaPlayer != null) {
                gc.mediaPlayer.pause();
                pauseFinish = true;
            }
        });

        stop.setOnClickListener(v -> {
            if (gc.mediaPlayer != null) {
                gc.mediaPlayer.stop();
                finish = true;
                pauseFinish = true;
                gc.mediaPlayer = null;
                sb.setProgress(0);
                duration = 0;
                current = 0;
                tv.setText("0/0");
            }
        });

        open.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_GET_CONTENT);  //  USED TO FETCH ANY TYPE OF CONTENT FILE FROM CLIENTS DEVICE
            intent.setType("audio/*");  //  intent.setType("video/*");  // intent.setType("images/*");
            startActivityForResult(Intent.createChooser(intent, "SELECT YOUR SONG"), 151);
        });
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        if (requestCode == 151 && resultCode == RESULT_OK) {

            Uri uri = data.getData();   // URI is used to store location for given content

            gc.mediaPlayer = MediaPlayer.create(getApplicationContext(), uri);
            gc.mediaPlayer.start();

            duration = gc.mediaPlayer.getDuration();
            duration = duration / 1000;   // Convert millisec to sec
            sb.setMax(duration);

            tv.setText("0/" + duration);

            finish = false;
            pauseFinish = false;

            notifyMe(uri);

            new Thread(new Runnable() {
                @Override
                public void run() {
                    while (!finish) {
                        try {
                            Thread.sleep(1000);
                        } catch (Exception e) {
                        }

                        if (!pauseFinish) {

                            current = gc.mediaPlayer.getCurrentPosition();
                            current = current / 1000;
                            sb.setProgress(current);

                            tv.post(new Runnable() {
                                @Override
                                public void run() {
                                    tv.setText("" + current + "/" + duration);
                                }
                            });

                            if (current >= duration) {
                                pauseFinish = true;
                                finish = true;
                                duration = 0;
                                current = 0;
                                sb.setProgress(0);
                                gc.mediaPlayer = null;

                                tv.post(new Runnable() {
                                    @Override
                                    public void run() {
                                        tv.setText("0/0");
                                    }
                                });
                            }
                        }

                    }
                }
            }).start();
        }

    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    void notifyMe(Uri uri){
        String path = uri.getPath();
        //  Songs/By_Movies/Kal_ho_na_ho/Song_name
        int p = path.lastIndexOf("/");
        String song = path.substring(p+1);

        //  Used to create channel for notification with unique id
        NotificationChannel channel = new NotificationChannel("1001","My Music",
                NotificationManager.IMPORTANCE_HIGH);

        //  Used to create intent for activity but to call later so keep it pending
        Intent intent = new Intent(getApplicationContext(), MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, 0);

        Intent intent1 = new Intent(getApplicationContext(), PauseService.class);
        PendingIntent pendingIntent1 = PendingIntent.getService(this, 0, intent1, 0);



        // Used to build view for notification
        NotificationCompat.Builder not = new NotificationCompat.Builder(getApplicationContext(), "1001")
                .setContentTitle(song)
                .setContentText("Favorite Song")
                .setSmallIcon(R.drawable.ic_play)
                .setContentIntent(pendingIntent)
                .addAction(android.R.drawable.ic_media_pause,"Pause", pendingIntent1);

        NotificationManager manager = getSystemService(NotificationManager.class);

        manager.createNotificationChannel(channel);

        manager.notify(123, not.build());
    }
}