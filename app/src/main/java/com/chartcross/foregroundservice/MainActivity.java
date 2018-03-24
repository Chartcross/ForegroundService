package com.chartcross.foregroundservice;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import java.util.Locale;

public class MainActivity extends AppCompatActivity implements TestResultsReceiver.Receiver, View.OnClickListener {

    private String TAG = getClass().getSimpleName();
    private TestResultsReceiver mReceiver;
    private TextView mTimeText;
    private long mCurrentTime;

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putLong("current_time", mCurrentTime);
        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        mTimeText = findViewById(R.id.system_time_text);
        mReceiver = new TestResultsReceiver(new Handler());
        mReceiver.setReceiver(this);

        Button startButton = findViewById(R.id.start_timer_button);
        Button stopButton = findViewById(R.id.stop_timer_button);
        startButton.setOnClickListener(this);
        stopButton.setOnClickListener(this);

        if (savedInstanceState == null || !savedInstanceState.containsKey("current_time")) {
            mCurrentTime = 0;
        } else {
            mCurrentTime = savedInstanceState.getLong("current_time");
        }
        mTimeText.setText(String.format(Locale.UK, "%013d", mCurrentTime));
        Log.d(TAG, "Activity created");
    }

    @Override
    protected void onResume() {
        mReceiver = new TestResultsReceiver(new Handler());
        mReceiver.setReceiver(this);
        Intent receiverIntent = new Intent(this, TestService.class);
        receiverIntent.putExtra(TestService.RESULTS_RECEIVER, mReceiver);
        receiverIntent.setAction(TestService.SET_RESULTS_RECEIVER_ACTION);
        startService(receiverIntent);
        Log.d(TAG, "Activity resumed");
        super.onResume();
    }

    @Override
    protected void onPause() {
        if (mReceiver != null) {
            Log.d(TAG, "Setting ResultsReciever to null");
            mReceiver.setReceiver(null);
        }
        Log.d(TAG, "Activity paused");
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        Log.d(TAG, "Activity destroyed");
        super.onDestroy();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.start_timer_button:
                Intent startIntent = new Intent(MainActivity.this, TestService.class);
                startIntent.putExtra(TestService.RESULTS_RECEIVER, mReceiver);
                startIntent.setAction(TestService.START_FOREGROUND_ACTION);
                startService(startIntent);
                break;
            case R.id.stop_timer_button:
                Intent stopIntent = new Intent(MainActivity.this, TestService.class);
                stopIntent.setAction(TestService.STOP_FOREGROUND_ACTION);
                startService(stopIntent);
                break;
            default:
                break;
        }
    }

    @Override
    public void onReceiveResult(int resultCode, Bundle resultData) {
        switch(resultCode) {
            case TestService.TIMER_MESSAGE:
                Log.d("MainActivity", resultData.getString(TestService.MESSAGE));
                break;

            case TestService.TIMER_TIME:
                mCurrentTime = resultData.getLong(TestService.CURRENT_TIME);
                mTimeText.setText(String.format(Locale.UK, "%013d", mCurrentTime));
                break;
        }
    }
}
