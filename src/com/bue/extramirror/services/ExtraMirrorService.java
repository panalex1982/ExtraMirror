/*package com.bue.extramirror.services;

import android.app.IntentService;
import android.content.Intent;
import android.os.Handler;

import com.bue.extramirror.tasks.TimerRunnable;

*//**
 * Created by Panagiotis Alexandropoulos on 18/11/2013.
 *//*
public class ExtraMirrorService extends IntentService {
    private Handler handler;
    private final TimerRunnable timer;
    public ExtraMirrorService() {
        super("ExtraMirrorService");
        handler = new Handler();
        timer=new TimerRunnable(handler,0l,0l,0.0f);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        handler.postDelayed(timer,1000);
    }
}*/
