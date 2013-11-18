package com.bue.extramirror;

import java.text.DecimalFormat;
import java.util.Random;

import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.graphics.Color;
import android.hardware.Camera;
import android.hardware.Camera.CameraInfo;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.ActionBar;
import android.app.AlertDialog;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;
import android.text.format.Time;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.RelativeLayout.LayoutParams;
import android.widget.TextView;
import android.widget.ToggleButton;
import android.widget.CompoundButton.OnCheckedChangeListener;

import com.bue.extramirror.customviews.AnalogSpeedMeterView;
import com.bue.extramirror.customviews.CameraPreview;
import com.bue.extramirror.customviews.SettingsDialogFragment;
import com.bue.extramirror.utilities.Clock;
import com.bue.extramirror.utilities.ExtraMirrorSharedPreferences;
import com.bue.extramirror.utilities.Keys;
import com.bue.extramirror.utilities.RuntimeSharedObjects;
import com.google.ads.*;

@SuppressLint("NewApi")
public class MirrorActivity extends FragmentActivity implements
        SettingsDialogFragment.SettingsDialogListener,
        ExtraMirrorSharedPreferences,
        SensorEventListener {
    private int FRONT_CAMERA = 0;
    private int MIRROR_CAMERA = 1;
    private final int NO_CAMERA = 2;

    // Save Instance Constants
    private static final String IDLE_TIME = "idleTime";
    private static final String DRIVING_TIME = "drivingTime";
    private static final String DRIVING_DISTANCE = "drivingDistance";
    private static final String ENGINE_STATE = "engineState";
    private static final String ENGINE_PREVIOUS_STATE = "enginePreviousState";

    // private int ANALOG_SPEED_METER=0;
    // private int DIGITAL_SPEED_METER=1;

    private int currentapiVersion;

    private Camera mCamera;
    private int cameraId;
    private CameraPreview mPreview;
    private WindowManager.LayoutParams layoutParameters;
    private WindowManager.LayoutParams systemParameters;

    private Handler runnableHandlers;
    // private Intent extraMirrorServiceIntent;

    private LocationManager locationManager;
    private LocationListener locationListener;
    private Time now;

    // TODO: Notification private NotificationManager mNotificationManager;

    private SharedPreferences sharedSettings;
    private int measureUnit;// 0 means metric, 1 means imperial
    private boolean energySaving;
    private float brightness;
    private int speedometerIndicatorMode;
    private boolean showIntro;
    private boolean backPressed;
    protected boolean cameraJustChanged;

    private int speed;
    protected long idleTime;
    protected long drivingTime;
    private float speedms;
    protected float distance;
    private double altitude;
    private boolean startEngine;
    private boolean wasEngineInactive;

    // Layout Controls
    private ActionBar actionBar;
    private FrameLayout preview;
    private RelativeLayout indicatorsRelativeLayout;
    private LayoutParams lpAnalogSpeedMeter;

    private TextView elapsedTimeTextView;
    private TextView idleTimeTextView;
    private TextView altimiterLabelTextView;
    private TextView altimiterIndicatorTextView;
    private TextView watchTextView;
    private AnalogSpeedMeterView analogSpeedMeter;
    private ImageView optionsMenuImageButton;

    private CheckBox mirrorCameraCheckBox, frontCameraCheckBox;
    private ToggleButton engineToggleButton;

    private double latitude, longitude;

    //Orientation variable
    float prv_roll_angle;

    // Ads Controls
    private AdView adView;
    private LinearLayout adsLinearLayout;
    private SensorManager mSensorManager;
    private Sensor mOrientation;


    @SuppressLint("NewApi")
    @Override
    public void onCreate(Bundle savedInstanceState) {
        // try{
        super.onCreate(savedInstanceState);
        // }catch(Exception ex){
        // String exs=ex.toString();
        // Log.d("Exception", exs);
        // }

        // Retrieve Saved Settings
        sharedSettings = getSharedPreferences(PREFS_NAME, 0);
        measureUnit = sharedSettings.getInt(PREFS_MEASURE_UNIT, 0);
        brightness = sharedSettings.getFloat(PREFS_BRIGHTNESS, 1.0f);
        energySaving = sharedSettings.getBoolean(PREFS_ENERGY_SAVING, false);
        speedometerIndicatorMode = sharedSettings.getInt(PREFS_SPEEDINDICATOR,
                0);
        backPressed = false;
        cameraJustChanged = false;
        systemParameters = getWindow().getAttributes();
        // setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LOCKED);

        // Initialize GUI
        layoutParameters = getWindow().getAttributes();
        try {
            // requestWindowFeature(Window.FEATURE_NO_TITLE);
            getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                    WindowManager.LayoutParams.FLAG_FULLSCREEN);
        } catch (Exception ex) {
            String exs = ex.toString();
            Log.d("Exception", exs);
        }
        setContentView(R.layout.activity_mirror);
        currentapiVersion = android.os.Build.VERSION.SDK_INT;

        setBrightness();
        setEnergySaving();

        elapsedTimeTextView = (TextView) findViewById(R.id.elapsedTimeTextView);
        idleTimeTextView = (TextView) findViewById(R.id.idleTimeTextView);
        altimiterIndicatorTextView = (TextView) findViewById(R.id.altimiterIndicatorTextView);
        altimiterLabelTextView = (TextView) findViewById(R.id.altimiterLabelTextView);
        watchTextView = (TextView) findViewById(R.id.watchTextView);
        preview = (FrameLayout) findViewById(R.id.camera_preview);
        indicatorsRelativeLayout = (RelativeLayout) findViewById(R.id.indicatorsRelativeLayout);
        optionsMenuImageButton = (ImageView) findViewById(R.id.optionsMenuImageButton);
        if (currentapiVersion < android.os.Build.VERSION_CODES.HONEYCOMB)
            optionsMenuImageButton.setVisibility(View.INVISIBLE);
        else {
            actionBar = getActionBar();
            actionBar.hide();
        }

        //Initialize Speedometer
        DisplayMetrics metrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(metrics);
        float smallerDimension=calculateSpeedmeterSize(metrics);
        if (measureUnit == 0)
            analogSpeedMeter = new AnalogSpeedMeterView(this, smallerDimension,
                    "Km/h", speedometerIndicatorMode);
        else
            analogSpeedMeter = new AnalogSpeedMeterView(this, smallerDimension,
                    "mph", speedometerIndicatorMode);
        lpAnalogSpeedMeter = new LayoutParams((int) smallerDimension,
                (int) smallerDimension);
        lpAnalogSpeedMeter.setMargins(5, 20, 50, 5);
        indicatorsRelativeLayout.addView(analogSpeedMeter, lpAnalogSpeedMeter);

        setSpeedometer();

    engineToggleButton = null;
        engineToggleButton = (ToggleButton) findViewById(R.id.engineToggleButton);
        mirrorCameraCheckBox = (CheckBox) findViewById(R.id.mirrorCameraCheckBox);
        frontCameraCheckBox = (CheckBox) findViewById(R.id.frontCameraCheckBox);

        changeTextColors(Color.RED);
        altimiterIndicatorTextView.setText(formatHeight(0.0));
        Clock clock = new Clock();
        elapsedTimeTextView.setText(clock.convertTime(0l));
        idleTimeTextView.setText(clock.convertTime(0l));

        // Initialize Parameters
        // TODO: Notification boolean toggleEngine=false;//It is used to toggle
        // engine button if it was active before Notification Button used
        // TODO: Notification Bundle extras = getIntent().getExtras();

        if (savedInstanceState != null) {
            idleTime = savedInstanceState.getLong(IDLE_TIME);
            drivingTime = savedInstanceState.getLong(DRIVING_TIME);
            distance = savedInstanceState.getFloat(DRIVING_DISTANCE);
            startEngine = savedInstanceState.getBoolean(ENGINE_STATE);
            wasEngineInactive = savedInstanceState
                    .getBoolean(ENGINE_PREVIOUS_STATE);
            elapsedTimeTextView.setText(clock.convertTime(drivingTime));
            idleTimeTextView.setText(clock.convertTime(idleTime));
        }/*
         * TODO: Notificationelse else if(extras!=null){
		 * idleTime=extras.getLong(IDLE_TIME);
		 * drivingTime=extras.getLong(DRIVING_TIME);
		 * distance=extras.getFloat(DRIVING_DISTANCE);
		 * elapsedTimeTextView.setText(clock.convertTime(drivingTime));
		 * idleTimeTextView.setText(clock.convertTime(idleTime)); startEngine =
		 * extras.getBoolean(ENGINE_STATE);
		 * wasEngineInactive=extras.getBoolean(ENGINE_PREVIOUS_STATE);
		 * if(startEngine) toggleEngine=true; }
		 */ else {
            idleTime = 0l;
            drivingTime = 0l;
            distance = 8000.3f;// 0.0f;
            startEngine = false;
            wasEngineInactive = true;
        }
        speed = 0;
        speedms = 0.0f;
        latitude = 0.0;
        longitude = 0.0;
        now = new Time();

        runnableHandlers = new Handler();
        final Runnable drivingTimeRunnable = new Runnable() {
            public void run() {
                drivingTime += 1000;

                // TODO: open on debug Log.d("TimerRunning", "Driving for... " +
                // drivingTime);
                Clock clock = new Clock();
                elapsedTimeTextView.setText(clock.convertTime(drivingTime));
                distance += speedms;
                updateDistance();

                runnableHandlers.postDelayed(this, 1000);
            }
        };

        final Runnable idleTimeRunnable = new Runnable() {
            public void run() {
                idleTime += 1000;
                // TODO: open on debug Log.d("TimerIdle", "Idle for... " +
                // idleTime);
                Clock clock = new Clock();
                idleTimeTextView.setText(clock.convertTime(idleTime));
                runnableHandlers.postDelayed(this, 1000);
            }
        };

        final Runnable watch = new Runnable() {
            public void run() {

                now.setToNow();
                watchTextView.setText(now.format("%k:%M|%d-%m-%Y"));
                // TODO: open on debug Log.d("TimerWatch",
                // "Watch for... "+now.format("%k:%M|%d-%m-%Y"));
                runnableHandlers.postDelayed(this, 1000);
            }
        };
        runnableHandlers.post(watch);

		/* I register all Listeners below */

        // Acquire a reference to the system Location Manager
        locationManager = (LocationManager) this
                .getSystemService(Context.LOCATION_SERVICE);

        // Define a listener that responds to location updates
        locationListener = new LocationListener() {
            int prvSpeed = 0;

            public void onLocationChanged(Location location) {
                // Random randomGenerator = new Random();
                // randomGenerator.setSeed((long)(location.getAltitude()*100));
                speedms = 19.94f;// location.getSpeed();//randomGenerator.nextFloat()*72.2f;
                speed = (int) (speedms * 3600.0f);
                altitude = location.getAltitude();
                latitude = location.getLatitude();
                longitude = location.getLongitude();
                // TODO: open on debug Log.d("Locatiob: ",
                // latitude+":"+longitude);

                updateSpeed();

                altimiterIndicatorTextView.setText(formatHeight(altitude));
                if (startEngine) {
                    if (speed == 0) {
                        runnableHandlers.removeCallbacks(drivingTimeRunnable);
                        if (prvSpeed != 0)
                            runnableHandlers
                                    .postDelayed(idleTimeRunnable, 1000);
                    } else if ((prvSpeed == 0 || wasEngineInactive)
                            && speed > 0) {
                        runnableHandlers.removeCallbacks(idleTimeRunnable);
                        runnableHandlers.postDelayed(drivingTimeRunnable, 1000);
                    }
                    wasEngineInactive = false;
                }
                prvSpeed = speed;
            }

            public void onStatusChanged(String provider, int status,
                                        Bundle extras) {
            }

            public void onProviderEnabled(String provider) {
            }

            public void onProviderDisabled(String provider) {
            }

        };

        // Register the listener with the Location Manager to receive location
        // updates
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0,
                0, locationListener);

        // Start Engine Toggle Button
        if (engineToggleButton != null)
            engineToggleButton
                    // .setOnClickListener(new View.OnClickListener() {
                    // public void onClick(View view) {
                    // startEngine= !startEngine;
                    // if (startEngine) {
                    // runnableHandlers.postDelayed(idleTimeRunnable, 1000);
                    // engineToggleButton.setImageDrawable(getResources().getDrawable(R.drawable.green_key));
                    // changeTextColors(Color.GREEN);
                    // } else {
                    // runnableHandlers.removeCallbacks(idleTimeRunnable);
                    // runnableHandlers.removeCallbacks(drivingTimeRunnable);
                    // engineToggleButton.setImageDrawable(getResources().getDrawable(R.drawable.red_key));
                    // changeTextColors(Color.RED);
                    // wasEngineInactive = true;
                    // }
                    // }
                    // });
                    .setOnCheckedChangeListener(new OnCheckedChangeListener() {

                        public void onCheckedChanged(CompoundButton buttonView,
                                                     boolean isChecked) {
                            startEngine = isChecked;
                            if (startEngine) {
                                runnableHandlers.postDelayed(idleTimeRunnable,
                                        1000);
                                changeTextColors(Color.GREEN);
                            } else {
                                runnableHandlers
                                        .removeCallbacks(idleTimeRunnable);
                                runnableHandlers
                                        .removeCallbacks(drivingTimeRunnable);
                                changeTextColors(Color.RED);
                                wasEngineInactive = true;
                            }

                        }

                    });

        cameraId = NO_CAMERA;
        //Initializes the camera preview if exist
        if (mPreview == null) {
            initializeCameraPreview(sharedSettings.getInt(PREFS_ACTIVE_CAMERA,
                    0));
        }

        // Mirror Camera Listener
        mirrorCameraCheckBox
                .setOnCheckedChangeListener(new OnCheckedChangeListener() {
                    public void onCheckedChanged(CompoundButton buttonView,
                                                 boolean isChecked) {
                        if (mPreview == null) {
                            initializeCameraPreview(MIRROR_CAMERA);
                        } else {
                            if (cameraJustChanged) {
                                cameraJustChanged = false;
                            } else {
                                int newCamera = MIRROR_CAMERA;
                                if (isChecked) {
                                    changeCamera(cameraId, newCamera);
                                    cameraJustChanged = true;
                                    if (frontCameraCheckBox.isChecked()) {
                                        frontCameraCheckBox.setChecked(false);
                                    } else
                                        cameraJustChanged = false;
                                } else {
                                    newCamera = NO_CAMERA;
                                    changeCamera(cameraId, newCamera);
                                }
                            }
                        }
                    }


                });

        // Front Camera Listener

        frontCameraCheckBox
                .setOnCheckedChangeListener(new OnCheckedChangeListener() {

                    public void onCheckedChanged(CompoundButton buttonView,
                                                 boolean isChecked) {
                        if (mPreview == null) {
                            initializeCameraPreview(FRONT_CAMERA);
                        } else {
                            if (cameraJustChanged) {
                                cameraJustChanged = false;
                            } else {
                                int newCamera = FRONT_CAMERA;
                                if (isChecked) {
                                    changeCamera(cameraId, newCamera);
                                    cameraJustChanged = true;
                                    if (mirrorCameraCheckBox.isChecked()) {
                                        mirrorCameraCheckBox.setChecked(false);
                                    } else
                                        cameraJustChanged = false;
                                } else {
                                    newCamera = NO_CAMERA;
                                    changeCamera(cameraId, newCamera);
                                }
                            }
                        }
                    }
                });

        // Options Menu ImageView Listener
        optionsMenuImageButton.setOnTouchListener(new View.OnTouchListener() {
            public boolean onTouch(View arg0, MotionEvent event) {
                optionsMenuImageButton
                        .setImageResource(R.drawable.menu_moreoverflow_normal_holo_light_rotate_90_pressed);
                switch (event.getAction()) {
                    case MotionEvent.ACTION_UP:
                        if (actionBar.isShowing())
                            actionBar.hide();
                        else
                            actionBar.show();
                        optionsMenuImageButton
                                .setImageResource(R.drawable.menu_moreoverflow_normal_holo_light_rotate_90);
                        break;
                }
                return true;
            }

        });

		/*
         * TODO: Notification if(toggleEngine) engineToggleButton.toggle();
		 */

        // Add Advertisements
        // Create the adView
        adView = new AdView(this, AdSize.SMART_BANNER, Keys.AD_MOB_KEY);
        // adView.setRotationX(90.f);

        adsLinearLayout=(LinearLayout) findViewById(R.id.adsLinearLayout);
        // Add the adView to it
        try {
            adsLinearLayout.addView(adView);
        } catch (Exception ex) {
            String exs = ex.toString();
            Log.d("Exception Ads: ", exs);
        }

        // Initiate a generic request to load it with an ad
        AdRequest adRequest = new AdRequest();
        adRequest.addTestDevice(AdRequest.TEST_EMULATOR); // Emulator
        // adRequest.addTestDevice("TEST_DEVICE_ID"); // Test Android Device
        //adView.loadAd(adRequest);

        mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        mOrientation = mSensorManager.getDefaultSensor(Sensor.TYPE_ORIENTATION);
    }

    private float calculateSpeedmeterSize(DisplayMetrics metrics) {
        float smallerDimension;
        if ((metrics.heightPixels == 320 && metrics.widthPixels == 240)
                || (metrics.heightPixels == 240 && metrics.widthPixels == 320)) {
            smallerDimension = (metrics.widthPixels / 2 < metrics.heightPixels) ? metrics.widthPixels / 2 - 20
                    : metrics.heightPixels - 20;
        } else if ((metrics.heightPixels == 480 && metrics.widthPixels == 800)
                || (metrics.heightPixels == 800 && metrics.widthPixels == 480)) {
            smallerDimension = ((2 * metrics.widthPixels) / 3 < metrics.heightPixels) ? (2 * metrics.widthPixels) / 3 - 20
                    : metrics.heightPixels - 20;
        } else {
            smallerDimension = (metrics.widthPixels < metrics.heightPixels) ? metrics.widthPixels - 20
                    : metrics.heightPixels - 20;
        }
        smallerDimension = 15 * smallerDimension / 16;
        return smallerDimension;
    }

    /*
                 * Handle options menu
                 */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_mirror, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_settings:
                showSettingsDialog();
                return true;
            case R.id.menu_exit:
                // getWindow().setAttributes(systemParameters);//Set System Screen
                // Settings
                backPressed = true;
                RuntimeSharedObjects.closeApplication = true;
                finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onBackPressed() {
        // Put up the Yes/No message box
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Close Extra Mirror!")
                .setMessage("Are you sure?")
                .setIcon(android.R.drawable.ic_dialog_alert)
                .setPositiveButton("Yes",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog,
                                                int which) {
                                backPressed = true;
                                RuntimeSharedObjects.closeApplication = true;
                                finish();
                            }
                        }).setNegativeButton("No", null) // Do nothing on no
                .show();
    }

    /*
     * (non-Javadoc)
     *
     * @see android.app.Activity#onDestroy()
     */
    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (adView != null) {
            adView.destroy();
        }
        getWindow().setAttributes(systemParameters);// Set System Screen
        // Settings
        runnableHandlers.removeCallbacksAndMessages(null);
        locationManager.removeUpdates(locationListener);
        // mNotificationManager.cancel(0);
        // mNotificationManager.cancelAll();
        // stopService(extraMirrorServiceIntent);
    }

    /*
     * (non-Javadoc)
     *
     * @see android.app.Activity#onPause()
     */
    @Override
    protected void onPause() {
        super.onPause();
        if (mPreview != null) {
            mPreview.stopPreview();
            mPreview.releaseCamera();
            mPreview = null;
            mCamera.release();
        }
        SharedPreferences.Editor editor = sharedSettings.edit();
        editor.putInt(PREFS_ACTIVE_CAMERA, cameraId);
        editor.commit();
        getWindow().setAttributes(systemParameters);// Set System Screen
        // Settings
        // Intent intent = new Intent(Intent.ACTION_MAIN);
        // intent.addCategory(Intent.CATEGORY_HOME);
        // intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);//***Change Here***

        mSensorManager.unregisterListener(this);
    }

    /*
     * (non-Javadoc)
     *
     * @see android.app.Activity#onResume()
     */
    @Override
    protected void onResume() {
        super.onResume();
        if (mPreview == null) {
            initializeCameraPreview(sharedSettings.getInt(PREFS_ACTIVE_CAMERA,
                    0));
        }
        mSensorManager.registerListener(this, mOrientation, SensorManager.SENSOR_DELAY_NORMAL);
    }

	/*
	 * Method for handling Setting Dialog
	 */

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putLong(IDLE_TIME, idleTime);
        outState.putLong(DRIVING_TIME, drivingTime);
        outState.putFloat(DRIVING_DISTANCE, distance);
        outState.putBoolean(ENGINE_STATE, startEngine);
        outState.putBoolean(ENGINE_PREVIOUS_STATE, wasEngineInactive);
        outState.putBoolean(STATE_PAUSED_AT_MAIN, true);
        // TODO: Notification createNotification(outState);
        super.onSaveInstanceState(outState);
    }

    public void showSettingsDialog() {
        // Create an instance of the dialog fragment and show it
        DialogFragment dialog = new SettingsDialogFragment();
        dialog.show(getSupportFragmentManager(), "SettingsDialogFragment");
    }

	/*
	 * Utilities methods
	 */

    /**
     * A safe way to get an instance of the Camera object.
     */
    @TargetApi(9)
    public static Camera getCameraInstance(int camera) {
        Camera c = null;
        try {
            c = Camera.open(camera); // attempt to get a Camera instance
        } catch (Exception e) {
            // Camera is not available (in use or does not exist)
        }
        return c; // returns null if camera is unavailable
    }

    public static Camera getCameraInstance() {
        Camera c = null;
        try {
            c = Camera.open(); // attempt to get a Camera instance
        } catch (Exception e) {
            // Camera is not available (in use or does not exist)
        }
        return c; // returns null if camera is unavailable
    }

    @SuppressLint("NewApi")
    public void initializeCameraPreview(int lastActiveCamera) {
        // Check number of cameras
        if (Camera.getNumberOfCameras() == 0) {
            frontCameraCheckBox.setEnabled(false);
            mirrorCameraCheckBox.setEnabled(false);
            cameraId = NO_CAMERA;
        } else if (Camera.getNumberOfCameras() == 2) {
            CameraInfo cameraInfo = new CameraInfo();
            for (int i = 0; i < 2; i++) {
                Camera.getCameraInfo(i, cameraInfo);
                if (cameraInfo.facing == CameraInfo.CAMERA_FACING_FRONT) {
                    MIRROR_CAMERA = i;
                } else {
                    FRONT_CAMERA = i;
                }
            }
            if (lastActiveCamera == MIRROR_CAMERA
                    ) {//|| lastActiveCamera == NO_CAMERA
                cameraJustChanged = true;
                mirrorCameraCheckBox.setChecked(true);
                cameraJustChanged = false;
                // Create camera
                // Create an instances of Cameras
                mCamera = getCameraInstance(MIRROR_CAMERA);
                cameraId = MIRROR_CAMERA;

                // Create our Previews view and set it as the content of our
                // activity.
                mPreview = new CameraPreview(this, mCamera);
                mPreview.startPreview();
                preview.addView(mPreview);
            } else if (lastActiveCamera == FRONT_CAMERA) {
                cameraJustChanged = true;
                frontCameraCheckBox.setChecked(true);
                cameraJustChanged = false;
                // Create camera
                // Create an instances of Cameras
                mCamera = getCameraInstance(FRONT_CAMERA);
                cameraId = FRONT_CAMERA;

                // Create our Previews view and set it as the content of our
                // activity.
                mPreview = new CameraPreview(this, mCamera);
                mPreview.startPreview();
                preview.addView(mPreview);
            }/*
			 * mCamera = getCameraInstance(FRONT_CAMERA); cameraId = NO_CAMERA;
			 * 
			 * // Create our Previews view and set it as the content of our //
			 * activity. mPreview = new CameraPreview(this, mCamera);
			 * mPreview.startPreview(); preview.addView(mPreview);
			 * preview.removeAllViews(); mPreview.stopPreview();
			 * //mPreview.releaseCamera(); }
			 */
        } else if (Camera.getNumberOfCameras() == 1) {
            CameraInfo cameraInfo = new CameraInfo();
            Camera.getCameraInfo(0, cameraInfo);
            if (cameraInfo.facing == CameraInfo.CAMERA_FACING_FRONT) {
                FRONT_CAMERA = 0;
                MIRROR_CAMERA = 1;
                mirrorCameraCheckBox.setEnabled(false);
                frontCameraCheckBox.setChecked(true);
                // Create camera
                // Create an instances of Cameras
                mCamera = getCameraInstance(0);
                cameraId = FRONT_CAMERA;

                // Create our Previews view and set it as the content of our
                // activity.
                mPreview = new CameraPreview(this, mCamera);
                mPreview.startPreview();
                preview.addView(mPreview);
            } else {
                MIRROR_CAMERA = 0;
                FRONT_CAMERA = 1;
                mirrorCameraCheckBox.setChecked(true);
                frontCameraCheckBox.setEnabled(false);
                // Create camera
                // Create an instances of Cameras
                mCamera = getCameraInstance(0);
                cameraId = MIRROR_CAMERA;

                // Create our Previews view and set it as the content of our
                // activity.
                mPreview = new CameraPreview(this, mCamera);
                mPreview.startPreview();
                preview.addView(mPreview);
            }
        }
    }

    // public void changeCamera(int camera) {
    // mPreview.stopPreview();
    // mPreview.releaseCamera();
    // mCamera = getCameraInstance(camera);
    // mPreview.changeCamera(mCamera);
    // cameraId = camera;
    // }

    public void changeCamera(int previousCamera, int newCamera) {
        if (newCamera == NO_CAMERA) {
            preview.removeAllViews();
            mPreview.stopPreview();
            mPreview.releaseCamera();
            mPreview = null;
            mCamera.release();
            // mCamera=null;
            // mPreview.releaseCamera();
        } else {
            if (previousCamera == NO_CAMERA) {
                // mPreview.releaseCamera();
                mCamera = getCameraInstance(newCamera);
                mPreview = new CameraPreview(this, mCamera);
                mPreview.startPreview();
                preview.addView(mPreview);
                // mPreview.changeCamera(mCamera);
                // mPreview.startPreview();
                // preview.addView(mPreview);
            } else {
                mPreview.stopPreview();
                mPreview.releaseCamera();
                mCamera.release();
                preview.removeAllViews();
                // mCamera=null;
                mCamera = getCameraInstance(newCamera);
                mPreview.changeCamera(mCamera);
                mPreview.startPreview();
                preview.addView(mPreview);

            }
        }
        cameraId = newCamera;
    }

    private void changeTextColors(int color) {
        altimiterLabelTextView.setTextColor(color);
        altimiterIndicatorTextView.setTextColor(color);
        analogSpeedMeter.setDigitalSpeedometerColor(color);
    }

    private String formatHeight(double heightMeters) {
        String height = "Unknown";
        switch (measureUnit) {
            case 0:
                String heightMetersText = " m";
                DecimalFormat df = new DecimalFormat("#.#");
                height = df.format(heightMeters) + heightMetersText;
                break;
            case 1:
                String heightMilesText = " ft";
                DecimalFormat dfm = new DecimalFormat("#.#");
                height = dfm.format(heightMeters * 3.2808) + heightMilesText;
                break;
        }
        return height;
    }

    private void setBrightness() {
        layoutParameters.screenBrightness = brightness;
        getWindow().setAttributes(layoutParameters);
    }

    private void setEnergySaving() {
        try {
            if (energySaving) {
                getWindow().clearFlags(
                        WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
            } else {
                getWindow().addFlags(
                        WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
            }
        } catch (Exception ex) {
            String exs = ex.toString();
            Log.d("Energy Saving Exception", exs);
        }
    }

    private void setSpeedometer() {
        analogSpeedMeter.changeMode(speedometerIndicatorMode);
    }

    private void updateSpeed() {
        analogSpeedMeter.setSpeed(speed);
    }

    private void updateDistance() {
        analogSpeedMeter.setDistance(distance);
    }

	/*
	 * Implementation of SettingsDialogFragment.SettingsDialogListener
	 * (non-Javadoc)
	 * 
	 * @see com.bue.extramirror.SettingsDialogFragment.SettingsDialogListener#
	 * onDialogPositiveClick(android.support.v4.app.DialogFragment)
	 */

    public void onDialogPositiveClick(DialogFragment dialog) {
        // TODO: open on debug
        // Log.d("onDialogPositiveClick","onDialogPositiveClick");
        SettingsDialogFragment settings = (SettingsDialogFragment) dialog;
        SharedPreferences.Editor editor = sharedSettings.edit();
        brightness = settings.getBrightness();
        editor.putFloat(PREFS_BRIGHTNESS, brightness);
        energySaving = settings.getEnergySaving();
        editor.putBoolean(PREFS_ENERGY_SAVING, energySaving);
        setEnergySaving();
        speedometerIndicatorMode = settings.getSpeedometerIndicatorMode();
        editor.putInt(PREFS_SPEEDINDICATOR, speedometerIndicatorMode);
        setSpeedometer();
        showIntro = settings.isShowIntro();
        editor.putBoolean(PREFS_SHOW_INTRO, showIntro);
        measureUnit = settings.getMeasureUnit();
        switch (measureUnit) {
            case 0:
                analogSpeedMeter.setMeasureUnit("Km/h");
                updateSpeed();
                editor.putInt(PREFS_MEASURE_UNIT, 0);
                editor.commit();
                break;
            case 1:
                analogSpeedMeter.setMeasureUnit("mph");
                updateSpeed();
                editor.putInt(PREFS_MEASURE_UNIT, 1);
                editor.commit();
                break;
            default:
                editor.putInt(PREFS_MEASURE_UNIT, 0);
                editor.commit();
                break;
        }

        this.altimiterIndicatorTextView.setText(formatHeight(altitude));

    }

    public void onDialogNegativeClick(DialogFragment dialog) {
        // Log.d("onDialogNegativeClick", "onDialogNegativeClick");
        setBrightness();
    }

    public void createNotification(Bundle outState) {
        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(
                this).setSmallIcon(R.drawable.ic_launcher)
                .setContentTitle("Extra Mirror").setAutoCancel(true);
        Intent resultIntent = new Intent(this, MirrorActivity.class);
        resultIntent.setAction(Intent.ACTION_MAIN);
        resultIntent.addCategory(Intent.CATEGORY_LAUNCHER);
        resultIntent.putExtras(outState);
        TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);
        stackBuilder.addParentStack(IntroActivity.class);
        stackBuilder.addNextIntent(resultIntent);
        PendingIntent resultPendingIntent = stackBuilder.getPendingIntent(0,
                PendingIntent.FLAG_UPDATE_CURRENT);
        mBuilder.setContentIntent(resultPendingIntent);

        NotificationManager mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        // mId allows you to update the notification later on.
        mNotificationManager.notify(1, mBuilder.build());
    }

    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {
//        float azimuth_angle = sensorEvent.values[0];
//        float pitch_angle = sensorEvent.values[1];
        float roll_angle = sensorEvent.values[2];
        if(prv_roll_angle==1000)
            prv_roll_angle=roll_angle;
        if(roll_angle>60 || roll_angle<-60){
            if(Math.abs(prv_roll_angle-roll_angle)>100){
                if(cameraId!=NO_CAMERA)
                   changeCamera(cameraId,cameraId);
//                Log.i("Math.abs(prv_roll_angle-roll_angle) :",Math.abs(prv_roll_angle-roll_angle)+"");
            }
            prv_roll_angle=roll_angle;
        }
//        Log.i("Sensor Result(zxy) :",azimuth_angle+", "+pitch_angle+", "+roll_angle);
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {

    }
}
