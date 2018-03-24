package com.chartcross.foregroundservice;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.os.ResultReceiver;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Copyright (C) 2017 Chartcross Limited
 * Created by Mark on 17/03/2018.
 */

public class TestService extends Service {

    public static final String NOTIFICATION_CHANNEL = "com.chartcross.foregroundservice.channel";
    public static final String START_FOREGROUND_ACTION = "com.chartcross.foregroundservice.action.startforeground";
    public static final String STOP_FOREGROUND_ACTION = "com.chartcross.foregroundservice.action.stopforeground";
    public static final String SHARE_ACTION = "com.chartcross.foregroundservice.action.share";
    public static final String SET_RESULTS_RECEIVER_ACTION = "com.chartcross.foregroundservice.action.setresultsreceiver";
    public static final int NOTIFICATION_ID = 1234;


    public static final String RESULTS_RECEIVER = "results_receiver";
    public static final String CURRENT_TIME = "current_time";
    public static final String MESSAGE = "message";
    public static final int TIMER_MESSAGE = 1;
    public static final int TIMER_TIME = 2;

    private String TAG = getClass().getSimpleName();

    NotificationCompat.Builder mNotificationBuilder;
    ResultReceiver mReceiver;
    Timer mTimer = new Timer();
    ServiceTimerTask mTimerTask;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        Log.d(TAG, "Service created");
        super.onCreate();
        mNotificationBuilder = buildNotification();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent.getAction() != null) {
            Log.d(TAG, intent.getAction());
            if (START_FOREGROUND_ACTION.equals(intent.getAction())) {
                if (mNotificationBuilder != null) {
                    startForeground(NOTIFICATION_ID, mNotificationBuilder.build());

                    //
                    // Get the results receiver from the activity
                    // in order to pass data back to the activity
                    //
                    mReceiver = intent.getParcelableExtra(RESULTS_RECEIVER);

                    //
                    // Start a simple one second timer
                    //
                    if (mTimerTask == null) {
                        mTimerTask = new ServiceTimerTask();
                        mTimer.scheduleAtFixedRate(mTimerTask, 1000, 1000);
                    }
                }
            } else if (STOP_FOREGROUND_ACTION.equals(intent.getAction())) {
                stopForeground(true);
                if (mTimer != null) {
                    mTimer.cancel();
                }
                if (mReceiver != null) {
                    Bundle bundle = new Bundle();
                    bundle.putString(MESSAGE, "Timer Stopped");
                    mReceiver.send(TIMER_MESSAGE, bundle);
                }
                stopSelf();
                Intent it = new Intent(Intent.ACTION_CLOSE_SYSTEM_DIALOGS);
                sendBroadcast(it);
            } else if (SET_RESULTS_RECEIVER_ACTION.equals(intent.getAction())) {
                mReceiver = intent.getParcelableExtra(RESULTS_RECEIVER);
            } else if (SHARE_ACTION.equals(intent.getAction())) {
                final Intent shareIntent = new Intent(Intent.ACTION_SEND);
                shareIntent.setType("text/plain");
                shareIntent.putExtra(Intent.EXTRA_SUBJECT, getResources().getString(R.string.app_name));
                shareIntent.putExtra(Intent.EXTRA_TEXT, String.format(Locale.UK, "System Time = %d", System.currentTimeMillis()));
                shareIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                Intent chooserIntent = Intent.createChooser(shareIntent, getResources().getString(R.string.title_share));
                chooserIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                this.startActivity(chooserIntent);
                Intent it = new Intent(Intent.ACTION_CLOSE_SYSTEM_DIALOGS);
                sendBroadcast(it);
            }
        }
        Log.d(TAG, "Service start called");
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mTimer != null) {
            mTimer.cancel();
        }
        if (mReceiver != null) {
            Bundle bundle = new Bundle();
            bundle.putString(MESSAGE, "Timer Stopped");
            mReceiver.send(TIMER_MESSAGE, bundle);
        }
        Log.d(TAG, "Service destroyed");
    }

    class ServiceTimerTask extends TimerTask {
        ServiceTimerTask() {
            Bundle bundle = new Bundle();
            bundle.putString(MESSAGE, "Timer Started");
            mReceiver.send(TIMER_MESSAGE, bundle);
        }

        @Override
        public void run() {
            Bundle bundle = new Bundle();
            bundle.putLong(CURRENT_TIME, System.currentTimeMillis());
            mReceiver.send(TIMER_TIME, bundle);
            mNotificationBuilder.setContentText(String.format(Locale.UK, "time = %d", System.currentTimeMillis()));
            startForeground(NOTIFICATION_ID, mNotificationBuilder.build());
        }
    }

    private NotificationCompat.Builder buildNotification() {
        try {
            //
            // Notification channels are new to Android O
            //
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                NotificationChannel channel = new NotificationChannel(NOTIFICATION_CHANNEL,
                        getString(R.string.notification_channel_title), NotificationManager.IMPORTANCE_DEFAULT);
                channel.setLightColor(Color.GREEN);
                channel.setSound(null, null);
                channel.setVibrationPattern(null);
                channel.setLockscreenVisibility(Notification.VISIBILITY_PRIVATE);
                NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
                if (manager != null) {
                    manager.createNotificationChannel(channel);
                }
            }

            //
            // Create a pending intent to start the MainActivity
            // if the notification is clicked
            //
            Intent activityIntent = new Intent(this, MainActivity.class);
            activityIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            PendingIntent pendingActivityIntent = PendingIntent.getActivity(this, 0, activityIntent, 0);

            //
            // Create a pending intent to stop the service
            // if the notification STOP action is clicked
            //
            Intent stopIntent = new Intent(this, TestService.class);
            stopIntent.setAction(STOP_FOREGROUND_ACTION);
            PendingIntent stopPendingIntent = PendingIntent.getService(this, 0, stopIntent, 0);

            //
            // Create a pending intent to share
            // the system time
            //
            Intent shareIntent = new Intent(this, TestService.class);
            shareIntent.setAction(SHARE_ACTION);
            PendingIntent sharePendingIntent = PendingIntent.getService(this, 0, shareIntent, 0);

            //
            // Build the notification with NotificationCompat
            // so it works with older Android versions
            //
            return new NotificationCompat.Builder(this, NOTIFICATION_CHANNEL)
                    .setSmallIcon(R.drawable.ic_timer)
                    .setContentTitle(getString(R.string.notification_title))
                    .setContentText(getString(R.string.notification_text))
                    .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                    .addAction(R.drawable.ic_action_stop, getResources().getString(R.string.action_stop_service), stopPendingIntent)
                    .addAction(R.drawable.ic_action_share, getResources().getString(R.string.action_share), sharePendingIntent)
                    .setContentIntent(pendingActivityIntent)
                    .setColor(this.getResources().getColor(R.color.colorPrimary));
        } catch (Exception ex) {
            ex.printStackTrace();
            return null;
        }
    }
}
