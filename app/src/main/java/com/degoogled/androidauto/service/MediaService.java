package com.degoogled.androidauto.service;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

public class MediaService extends Service {
    private static final String TAG = "MediaService";

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "MediaService created");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, "MediaService started");
        return START_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "MediaService destroyed");
    }
}