package com.bue.extramirror;

import com.bue.extramirror.utilities.ExtraMirrorSharedPreferences;
import com.bue.extramirror.utilities.Keys;
import com.bue.extramirror.utilities.RuntimeSharedObjects;
import com.google.ads.AdRequest;
import com.google.ads.AdSize;
import com.google.ads.AdView;
import com.google.analytics.tracking.android.EasyTracker;

import android.os.Bundle;
import android.app.Activity;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.AnimationDrawable;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;
import android.util.Log;
import android.view.Menu;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;

public class IntroActivity extends FragmentActivity implements ExtraMirrorSharedPreferences {
	private AnimationDrawable introAnimation;
	private Button startButton;
	private boolean showIntro;
	private boolean isPausedAtMain;
	private SharedPreferences sharedSettings;
	
	//Ads Controls
		 private AdView adView;
		 private LinearLayout adsLinearLayout;


	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		sharedSettings=getSharedPreferences(PREFS_NAME, 0);
		showIntro=sharedSettings.getBoolean(PREFS_SHOW_INTRO, true);
		if(savedInstanceState!=null){
			isPausedAtMain=savedInstanceState.getBoolean(STATE_PAUSED_AT_MAIN);
		}else{
			isPausedAtMain=false;
		}
		//pausedActivity=sharedSettings.getInt(PREFS_PAUSED_ACTIVITY, PAUSED_AT_INTRO);
		if(showIntro){
				if(!isPausedAtMain){
					//Initialize Parameters
							try{
								requestWindowFeature(Window.FEATURE_NO_TITLE);
								getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
										WindowManager.LayoutParams.FLAG_FULLSCREEN);
							}catch(Exception ex){
								String exs=ex.toString();
								Log.d("Exception", exs);
							}
					setContentView(R.layout.activity_intro);
					
					//Create Animation
					ImageView introAnimationImageView=(ImageView) findViewById(R.id.introAnimationImageView);
					introAnimationImageView.setBackgroundResource(R.drawable.intro_animation);
					introAnimation=(AnimationDrawable) introAnimationImageView.getBackground();
					
					
					//Start Button
					startButton=(Button) findViewById(R.id.startButton);
					startButton.setOnTouchListener(new View.OnTouchListener() {				
						public boolean onTouch(View v, MotionEvent event) {
							if(event.getAction()==MotionEvent.ACTION_UP)
								openMainApplication();
							return false;
						}
					});
					//Add Advertisements
							// Create the adView
						    adView = new AdView(this, AdSize.BANNER, Keys.AD_MOB_KEY);
						    //adView.setRotationX(90.f);
							
							adsLinearLayout=(LinearLayout) findViewById(R.id.adsIntroLinearLayout);
							// Add the adView to it
							try{
								adsLinearLayout.addView(adView);
							}catch(Exception ex){
								String exs=ex.toString();
								Log.d("Exception Ads: ", exs);
							}
			
						    // Initiate a generic request to load it with an ad
							AdRequest adRequest = new AdRequest();
							//adRequest.addTestDevice(AdRequest.TEST_EMULATOR);               // Emulator
							//adRequest.addTestDevice("TEST_DEVICE_ID");                      // Test Android Device
						    adView.loadAd(adRequest);
				}else if(isPausedAtMain){
					openMainApplication();					
				}
		}else{		
			openMainApplication();
		}
	}
	
	@Override
	public void onWindowFocusChanged(boolean hasFocus) {
		if(showIntro)
			introAnimation.start();
		super.onWindowFocusChanged(hasFocus);
	}
	
	private void openMainApplication(){
		//Intent mainActivity=new Intent(IntroActivity.this, MirrorActivity.class);
		try{
			startActivity(new Intent(IntroActivity.this, MirrorActivity.class));
		}catch(Exception ex){
			String exs=ex.toString();
			Log.d("Exception", exs);
		}
		//finish();
        //System.exit(0);	
	}

	@Override
	protected void onPause() {
		super.onPause();		
		if(showIntro && !isPausedAtMain)//&&(pausedActivity==PAUSED_AT_INTRO)&&android.os.Build.VERSION.SDK_INT>android.os.Build.VERSION_CODES.ECLAIR_MR1)
			introAnimation.stop();
	}
	
	

	@Override
	protected void onDestroy() {
		super.onDestroy();
		if (adView != null) {
		      adView.destroy();
		}
	}

	@Override
	protected void onResume() {
		super.onResume();
		if(RuntimeSharedObjects.closeApplication){
			RuntimeSharedObjects.closeApplication=false;
			finish();			
		}
	}
	
	@Override
	protected void onStart() {
		super.onStart();
		EasyTracker.getInstance().activityStart(this);

	}

	@Override
	protected void onStop() {
		super.onStop();
		EasyTracker.getInstance().activityStop(this);
	}
	
	
	
	
	
	
}