package com.bue.extramirror.tasks;

import com.bue.extramirror.customviews.AnalogSpeedMeterView;
import com.bue.extramirror.utilities.Clock;

import android.os.Handler;
import android.text.format.Time;
import android.util.Log;
import android.widget.TextView;

/**
 * Created by Panagiotis Alexandropoulos on 18/11/2013.
 */
public class TimerRunnable implements Runnable{
    private Handler handler;
    private Time now;
    private TextView watchTextView;
    private AnalogSpeedMeterView speedMeterView;
    //motionState 0: no state, 1: standing, 2: moving
    private int motionState;
    private long drivingTime,
                idleTime;
    private float distance,
                   speedms;

    public TimerRunnable(Handler handler, TextView watchTextView, AnalogSpeedMeterView speedMeterView,
                         long drivingTime, long idleTime, float distance){
        now = new Time();
        motionState=0;
        this.handler=handler;
        this.watchTextView=watchTextView;
        this.drivingTime=drivingTime;
        this.idleTime=idleTime;
        this.distance=distance;
        this.speedMeterView=speedMeterView;
        speedms=0.0f;
    }

    public TimerRunnable(Handler handler, long drivingTime, long idleTime, float distance){
        now = new Time();
        motionState=0;
        this.handler=handler;
        this.watchTextView=null;
        this.speedMeterView=null;
        this.drivingTime=drivingTime;
        this.idleTime=idleTime;
        this.distance=distance;
        speedms=0.0f;
    }

    @Override
    public void run() {
        now.setToNow();
        if(speedMeterView!=null)
            watchTextView.setText(now.format("%k:%M|%d-%m-%Y"));
        switch(motionState){
            case 1:
                idleTime += 1000;
                if(speedMeterView!=null){
                    speedMeterView.setTimers(Clock.convertTime(idleTime), Clock.convertTime(drivingTime), motionState);
                }
                break;
            case 2:
                drivingTime += 1000;
                if(speedMeterView!=null){
                    speedMeterView.setTimers(Clock.convertTime(idleTime), Clock.convertTime(drivingTime), motionState);
                }
                distance += speedms;
                break;
        }
        handler.postDelayed(this,1000);
        if(speedMeterView==null)
            Log.i("Moving/Standing Service:", drivingTime + "/" + idleTime);
        else
            Log.i("Moving/Standing:", drivingTime + "/" + idleTime);
    }

    public void closeEngine(){
        motionState=0;
        if(speedMeterView!=null)
            speedMeterView.setTimers(Clock.convertTime(idleTime), Clock.convertTime(drivingTime),1);
    }

    public void stopMoving(){
        motionState=1;
    }

    public void startMoving(){
        motionState=2;
    }

    public void setSpeedms(float speedms) {
        this.speedms = speedms;
    }

    public long getIdleTime() {
        return idleTime;
    }

    public long getDrivingTime() {
        return drivingTime;
    }

    public float getDistance() {
        return distance;
    }


}
