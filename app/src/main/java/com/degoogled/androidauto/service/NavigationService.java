package com.degoogled.androidauto.service;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

public class NavigationService extends Service {
    private static final String TAG = "NavigationService";

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "NavigationService created");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, "NavigationService started");
        return START_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "NavigationService destroyed");
    }
}