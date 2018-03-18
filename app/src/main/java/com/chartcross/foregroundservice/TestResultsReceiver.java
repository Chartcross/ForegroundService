package com.chartcross.foregroundservice;

import android.os.Bundle;
import android.os.Handler;
import android.os.ResultReceiver;

import java.lang.ref.WeakReference;

/**
 * Copyright (C) 2017 Chartcross Limited
 * Created by Mark on 18/03/2018.
 */
public class TestResultsReceiver extends ResultReceiver {

    private WeakReference<Receiver> mReceiver;

    public TestResultsReceiver(Handler handler) {
        super(handler);
    }

    public interface Receiver {
        void onReceiveResult(int resultCode, Bundle resultData);
    }

    public void setReceiver(Receiver receiver) {
        mReceiver = new WeakReference<>(receiver);
    }

    @Override
    protected void onReceiveResult(int resultCode, Bundle resultData) {
        if (mReceiver != null && mReceiver.get() != null) {
            mReceiver.get().onReceiveResult(resultCode, resultData);
        }
    }
}
