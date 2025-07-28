package com.degoogled.androidauto.service;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

public class ProtocolHandlerService extends Service {
    private static final String TAG = "ProtocolHandlerService";

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "ProtocolHandlerService created");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, "ProtocolHandlerService started");
        return START_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "ProtocolHandlerService destroyed");
    }
}