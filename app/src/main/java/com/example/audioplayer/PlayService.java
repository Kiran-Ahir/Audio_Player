package com.example.audioplayer;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

public class PlayService extends Service {

    GlobalClass gc;

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        gc = (GlobalClass)getApplication();

        gc.mediaPlayer.start();

        return START_NOT_STICKY;
    }
}