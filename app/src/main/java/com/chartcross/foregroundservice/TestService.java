package com.chartcross.foregroundservice;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;
import android.widget.Toast;

/**
 * Copyright (C) 2017 Chartcross Limited
 * Created by Mark on 17/03/2018.
 */

public class TestService extends Service {

    public static final String TEST_DATA = "test_data";
    private String TAG = getClass().getSimpleName();

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        Log.d(TAG, "Service created");
        super.onCreate();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, "Service starting");
        String testData = intent.getStringExtra(TEST_DATA);
        Toast.makeText(this, testData, Toast.LENGTH_SHORT).show();
        Log.d(TAG, testData);
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "Service destroyed");
    }
}
