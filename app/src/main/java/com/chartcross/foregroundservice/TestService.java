package com.chartcross.foregroundservice;

import android.app.Service;
import android.content.Intent;
import android.os.Bundle;
import android.os.IBinder;
import android.os.ResultReceiver;
import android.support.annotation.Nullable;
import android.util.Log;
import android.widget.Toast;

/**
 * Copyright (C) 2017 Chartcross Limited
 * Created by Mark on 17/03/2018.
 */

public class TestService extends Service {

    public static final String TEST_DATA = "test_data";
    public static final String RESULTS_RECEIVER = "results_receiver";
    public static final int RESULT_OK = 1;

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
        //
        // Get the results receiver from the activity
        // in order to pass data back to the activity
        //
        ResultReceiver receiver = intent.getParcelableExtra(RESULTS_RECEIVER);
        //
        // Send data back to activity
        //
        Bundle bundle = new Bundle();
        bundle.putString(TEST_DATA, "Data From Service");
        if (receiver != null) {
            receiver.send(RESULT_OK, bundle);
        }

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
