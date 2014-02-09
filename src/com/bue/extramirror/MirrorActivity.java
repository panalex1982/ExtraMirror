package com.bue.extramirror;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.Color;
import android.hardware.Camera;
import android.hardware.Camera.CameraInfo;
/*import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;*/
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.ActionBar;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentActivity;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.CompoundButton;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.RelativeLayout.LayoutParams;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.ToggleButton;
import android.widget.CompoundButton.OnCheckedChangeListener;

import com.bue.extramirror.customviews.AnalogSpeedMeterView;
import com.bue.extramirror.customviews.CameraPreview;
import com.bue.extramirror.customviews.SettingsDialogFragment;
import com.bue.extramirror.tasks.TimerRunnable;
import com.bue.extramirror.utilities.Clock;
import com.bue.extramirror.utilities.ExtraMirrorSharedPreferences;
import com.bue.extramirror.utilities.Keys;
import com.bue.extramirror.utilities.RuntimeSharedObjects;
import com.google.ads.*;
import com.google.analytics.tracking.android.EasyTracker;

@SuppressLint("NewApi")
public class MirrorActivity extends FragmentActivity implements
        SettingsDialogFragment.SettingsDialogListener,
        ExtraMirrorSharedPreferences{
    private int FRONT_CAMERA = 0;
    private int MIRROR_CAMERA = 1;
    private final int NO_CAMERA = 2;

    // Save Instance Constants
    private static final String IDLE_TIME = "idleTime";
    private static final String DRIVING_TIME = "drivingTime";
    private static final String DRIVING_DISTANCE = "drivingDistance";
    private static final String ENGINE_STATE = "engineState";
    private static final String ENGINE_PREVIOUS_STATE = "enginePreviousState";

    private Camera mCamera;
    private int cameraId;
    private CameraPreview mPreview;
    private WindowManager.LayoutParams layoutParameters;
    private WindowManager.LayoutParams systemParameters;

    private Handler runnableHandlers;

    private LocationManager locationManager;
    private LocationListener locationListener;


    private SharedPreferences sharedSettings;
    private int measureUnit;// 0 means metric, 1 means imperial
    private boolean energySaving;
    private float brightness;
    private int speedometerIndicatorMode;
    private boolean isCameraEnabled;
    private boolean wasCameraEnabled;//was enabled when activity started first time

    private int speed;
    private float speedms;
    private double altitude;
    private boolean startEngine;
    private boolean wasEngineInactive;

    // Layout Controls
    private ActionBar actionBar;
    private FrameLayout preview;

    private AnalogSpeedMeterView analogSpeedMeter;
    private ImageView optionsMenuImageButton;

    private ToggleButton cameraToggleButton;

    // Ads Controls
    private AdView adView;

    private TimerRunnable timer;
    private int numOfCameras;



    @SuppressLint("NewApi")
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Intent extrasIntent=getIntent();

        //Temp variables
        long idleTime, drivingTime;
        float distance;

        // Retrieve Saved Settings
        sharedSettings = getSharedPreferences(PREFS_NAME, 0);
        measureUnit = sharedSettings.getInt(PREFS_MEASURE_UNIT, 0);
        brightness = sharedSettings.getFloat(PREFS_BRIGHTNESS, 1.0f);
        energySaving = sharedSettings.getBoolean(PREFS_ENERGY_SAVING, false);
        speedometerIndicatorMode = sharedSettings.getInt(PREFS_SPEEDINDICATOR,
                0);
        isCameraEnabled=sharedSettings.getBoolean(PREFS_CAMERA_ENABLED,true);
        wasCameraEnabled=isCameraEnabled;

        systemParameters = getWindow().getAttributes();

        // Initialize GUI
        layoutParameters = getWindow().getAttributes();
        try {
            getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                    WindowManager.LayoutParams.FLAG_FULLSCREEN);

            setRequestedOrientation(extrasIntent.getIntExtra(EXTRA_INTRO_ORINTATION,ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE));
        } catch (Exception ex) {
            String exs = ex.toString();
            Log.d("Exception", exs);
        }
        setContentView(R.layout.activity_mirror);
        int currentapiVersion = android.os.Build.VERSION.SDK_INT;

        setBrightness();
        setEnergySaving();

        TextView watchTextView = (TextView) findViewById(R.id.watchTextView);
        preview = (FrameLayout) findViewById(R.id.camera_preview);
        RelativeLayout indicatorsRelativeLayout = (RelativeLayout) findViewById(R.id.indicatorsRelativeLayout);
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
        analogSpeedMeter = new AnalogSpeedMeterView(this, smallerDimension,
                    measureUnit, speedometerIndicatorMode);
        LayoutParams lpAnalogSpeedMeter = new LayoutParams((int) smallerDimension,
                (int) smallerDimension);
        lpAnalogSpeedMeter.setMargins(5, 20, 50, 5);
        indicatorsRelativeLayout.addView(analogSpeedMeter, lpAnalogSpeedMeter);

        setSpeedometer();

        ToggleButton engineToggleButton = (ToggleButton) findViewById(R.id.engineToggleButton);

        //Seek Bar
        cameraToggleButton =(ToggleButton) findViewById(R.id.cameraToggleButton);

        changeTextColors(Color.RED);

        // Initialize Parameters
        if (savedInstanceState != null) {
            idleTime = savedInstanceState.getLong(IDLE_TIME);
            drivingTime = savedInstanceState.getLong(DRIVING_TIME);
            distance = savedInstanceState.getFloat(DRIVING_DISTANCE);
            startEngine = savedInstanceState.getBoolean(ENGINE_STATE);
            wasEngineInactive = savedInstanceState
                    .getBoolean(ENGINE_PREVIOUS_STATE);
            updateSpeedometer(distance);
        }else {
            idleTime = 0l;
            drivingTime = 0l;
            distance = 0.0f;//8000.3f;
            startEngine = false;
            wasEngineInactive = true;
        }
        speed = 0;
        speedms = 0.0f;
//        latitude = 0.0;
//        longitude = 0.0;

        runnableHandlers = new Handler();

        timer=new TimerRunnable(runnableHandlers, watchTextView, analogSpeedMeter, drivingTime, idleTime, distance);
        runnableHandlers.post(timer);
        locationManager = (LocationManager) this
                .getSystemService(Context.LOCATION_SERVICE);

        // Define a listener that responds to location updates
        locationListener = new LocationListener() {
            int prvSpeed = 0;

            public void onLocationChanged(Location location) {
                // Random randomGenerator = new Random();
                // randomGenerator.setSeed((long)(location.getAltitude()*100));
                speedms = location.getSpeed(); //19.94f;// //randomGenerator.nextFloat()*72.2f;
                timer.setSpeedms(speedms);
                speed = (int) (speedms * 3600.0f);
                altitude = location.getAltitude();
//                latitude = location.getLatitude();
//                longitude = location.getLongitude();
                updateSpeedometer(timer.getDistance());

                if (startEngine) {
                    if (speed == 0) {
                        if (prvSpeed != 0)
                            timer.stopMoving();
                    } else if ((prvSpeed == 0 || wasEngineInactive)
                            && speed > 0) {
                        timer.startMoving();
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
                    .setOnCheckedChangeListener(new OnCheckedChangeListener() {

                        public void onCheckedChanged(CompoundButton buttonView,
                                                     boolean isChecked) {
                            startEngine = isChecked;
                            if (startEngine) {
                                timer.stopMoving();
                                changeTextColors(Color.GREEN);
                            } else {
                                timer.closeEngine();
                                changeTextColors(Color.RED);
                                wasEngineInactive = true;
                            }

                        }

                    });
        if(isCameraEnabled){
            cameraId = NO_CAMERA;
            //Initializes the camera preview if exist
            if (mPreview == null) {
                initializeCameraPreview(sharedSettings.getInt(PREFS_ACTIVE_CAMERA,
                        0));
            }

            cameraToggleButton.setOnCheckedChangeListener(new OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {
                    int newCamera=0;
                    switch(numOfCameras){
                        case 0:
                            break;
                        case 1:
                            break;
                        case 2:
                            newCamera=(isChecked)?MIRROR_CAMERA:FRONT_CAMERA;
                            break;
                        default:
                            break;
                    }
                    if (newCamera != cameraId) {
                        if (mPreview == null) {
                            initializeCameraPreview(newCamera);
                        } else {
                            changeCamera(cameraId, newCamera);
                        }
                    }
                }
            });

        }else{
            cameraToggleButton.setEnabled(false);
        }

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

        // Add Advertisements
        // Create the adView
        adView = new AdView(this, AdSize.BANNER, Keys.AD_MOB_KEY);

        LinearLayout adsLinearLayout = (LinearLayout) findViewById(R.id.adsLinearLayout);
        // Add the adView to it
        try {
            adsLinearLayout.addView(adView);
        } catch (Exception ex) {
            String exs = ex.toString();
            Log.d("Exception Ads: ", exs);
        }

        // Initiate a generic request to load it with an ad
        AdRequest adRequest = new AdRequest();
        /*adRequest.addTestDevice(AdRequest.TEST_EMULATOR); // Emulator
        adRequest.addTestDevice(Keys.SONY_DEVICE_ID); // Test Android Device*/
        adView.loadAd(adRequest);
    }

    private float calculateSpeedmeterSize(DisplayMetrics metrics) {
        float smallerDimension;
        if ((metrics.heightPixels == 320 && metrics.widthPixels == 240)
                || (metrics.heightPixels == 240 && metrics.widthPixels == 320)) {
            smallerDimension = (metrics.widthPixels / 2 < metrics.heightPixels) ? metrics.widthPixels / 2 - 30
                    : metrics.heightPixels - 30;
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

    }

    /*
     * (non-Javadoc)
     *
     * @see android.app.Activity#onResume()
     */
    @Override
    protected void onResume() {
        super.onResume();
        if (isCameraEnabled && wasCameraEnabled && mPreview == null) {
            initializeCameraPreview(sharedSettings.getInt(PREFS_ACTIVE_CAMERA,
                    0));
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        EasyTracker.getInstance(this).activityStart(this);

    }

    @Override
    protected void onStop() {
        super.onStop();
        EasyTracker.getInstance(this).activityStop(this);
    }

	/*
	 * Method for handling Setting Dialog
	 */

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putLong(IDLE_TIME, timer.getIdleTime());//idleTime);
        outState.putLong(DRIVING_TIME, timer.getDrivingTime());//drivingTime);
        outState.putFloat(DRIVING_DISTANCE, timer.getDistance());
        outState.putBoolean(ENGINE_STATE, startEngine);
        outState.putBoolean(ENGINE_PREVIOUS_STATE, wasEngineInactive);
        outState.putBoolean(STATE_PAUSED_AT_MAIN, true);
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

    @SuppressLint("NewApi")
    public void initializeCameraPreview(int lastActiveCamera) {
        numOfCameras=Camera.getNumberOfCameras();
        CameraInfo cameraInfo = new CameraInfo();
        // Check number of cameras
        switch (numOfCameras){
            case 0:
                cameraToggleButton.setEnabled(false);
                cameraId = NO_CAMERA;
                cameraToggleButton.setEnabled(false);
                break;
            case 1:
                Camera.getCameraInfo(0, cameraInfo);
                if (cameraInfo.facing == CameraInfo.CAMERA_FACING_FRONT) {
                    FRONT_CAMERA = 0;
                    MIRROR_CAMERA = 1;
                    // Create camera
                    mCamera = getCameraInstance(0);
                    cameraId = FRONT_CAMERA;

                    // Create our Previews view and set it as the content of our
                    // activity.
                    mPreview = new CameraPreview(this, mCamera);
                    mPreview.startPreview();
                    preview.addView(mPreview);

                    cameraToggleButton.setEnabled(false);
                } else {
                    MIRROR_CAMERA = 0;
                    FRONT_CAMERA = 1;
                    // Create camera
                    mCamera = getCameraInstance(0);
                    cameraId = MIRROR_CAMERA;

                    // Create our Previews view and set it as the content of our
                    // activity.
                    mPreview = new CameraPreview(this, mCamera);
                    mPreview.startPreview();
                    preview.addView(mPreview);

                    cameraToggleButton.setEnabled(false);
                }
                break;
            case 2:
                for (int i = 0; i < 2; i++) {
                    Camera.getCameraInfo(i, cameraInfo);
                    if (cameraInfo.facing == CameraInfo.CAMERA_FACING_FRONT) {
                        MIRROR_CAMERA = i;
                    } else {
                        FRONT_CAMERA = i;
                    }
                }
                if (lastActiveCamera == MIRROR_CAMERA
                        ) {
                    // Create camera
                    mCamera = getCameraInstance(MIRROR_CAMERA);
                    cameraId = MIRROR_CAMERA;

                    // Create our Previews view and set it as the content of our
                    // activity.
                    mPreview = new CameraPreview(this, mCamera);
                    mPreview.startPreview();
                    preview.addView(mPreview);

                    cameraToggleButton.setChecked(true);

                } else if (lastActiveCamera == FRONT_CAMERA) {
//                    cameraJustChanged = false;
                    // Create camera
                    mCamera = getCameraInstance(FRONT_CAMERA);
                    cameraId = FRONT_CAMERA;

                    // Create our Previews view and set it as the content of our
                    // activity.
                    mPreview = new CameraPreview(this, mCamera);
                    mPreview.startPreview();
                    preview.addView(mPreview);

                    cameraToggleButton.setChecked(false);
                }
                break;

        }
    }

    public void changeCamera(int previousCamera, int newCamera) {
        if (newCamera == NO_CAMERA) {
            preview.removeAllViews();
            mPreview.stopPreview();
            mPreview.releaseCamera();
            mPreview = null;
            mCamera.release();
        } else {
            if (previousCamera == NO_CAMERA) {
                mCamera = getCameraInstance(newCamera);
                mPreview = new CameraPreview(this, mCamera);
                mPreview.startPreview();
                preview.addView(mPreview);
            } else {
                mPreview.stopPreview();
                mPreview.releaseCamera();
                mCamera.release();
                preview.removeAllViews();

                mCamera = getCameraInstance(newCamera);
                mPreview.changeCamera(mCamera);
                mPreview.startPreview();
                preview.addView(mPreview);

            }
        }
        cameraId = newCamera;
    }

    private void changeTextColors(int color) {
        analogSpeedMeter.setDigitalSpeedometerColor(color);
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

    private void updateSpeedometer(float distance) {
        analogSpeedMeter.setSpeed(speed);
        analogSpeedMeter.setDistance(distance);
        analogSpeedMeter.setAltitude(altitude);
    }

	/*
	 * Implementation of SettingsDialogFragment.SettingsDialogListener
	 * (non-Javadoc)
	 * 
	 * @see com.bue.extramirror.SettingsDialogFragment.SettingsDialogListener#
	 * onDialogPositiveClick(android.support.v4.app.DialogFragment)
	 */
    public void onDialogPositiveClick(DialogFragment dialog) {
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
        boolean showIntro = settings.isShowIntro();
        editor.putBoolean(PREFS_SHOW_INTRO, showIntro);
        isCameraEnabled=settings.isCameraEnabled();
        editor.putBoolean(PREFS_CAMERA_ENABLED,isCameraEnabled);
        
        measureUnit = settings.getMeasureUnit();
        analogSpeedMeter.setMeasureUnit(measureUnit);
        updateSpeedometer(timer.getDistance());
        editor.putInt(PREFS_MEASURE_UNIT, measureUnit);
        editor.commit();
    }

    public void onDialogNegativeClick(DialogFragment dialog) {
        setBrightness();
    }
}
