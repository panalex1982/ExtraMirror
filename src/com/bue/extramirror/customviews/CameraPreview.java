package com.bue.extramirror.customviews;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import android.annotation.TargetApi;
import android.content.Context;
import android.hardware.Camera;
import android.hardware.Camera.Parameters;
import android.hardware.Camera.Size;
import android.os.Build;
import android.support.v4.view.ViewPager.LayoutParams;
import android.util.Log;
import android.view.Display;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.WindowManager;
import android.widget.Toast;

/** A basic Camera preview class */
@TargetApi(Build.VERSION_CODES.FROYO)
public class CameraPreview extends SurfaceView implements SurfaceHolder.Callback {
    private static final String TAG = "TAG_CAMERA";
	private SurfaceHolder mHolder;
    private Camera mCamera;
    private WindowManager windowManager;
    private int[] screenSize;
    private Camera.Size previewSize;
    //private ArrayList<Float> eventX;
    private ArrayList<Float> eventY;
    private Toast noZoomToast;
    private boolean hasToastShowedUp;
	private LayoutParams layoutParameters;
    
    public CameraPreview(Context context, Camera camera) {
        super(context);        
        screenSize=new int[2];
        windowManager = (WindowManager)context.getSystemService(Context.WINDOW_SERVICE);
    	screenSize[0] = windowManager.getDefaultDisplay().getWidth();
    	screenSize[1]=windowManager.getDefaultDisplay().getHeight();    	
        mCamera = camera;
        layoutParameters=new LayoutParams();
        setCameraParameters();
        //eventX=new ArrayList<Float>();
        eventY=new ArrayList<Float>();

        // Install a SurfaceHolder.Callback so we get notified when the
        // underlying surface is created and destroyed.
        mHolder = getHolder();
        mHolder.addCallback(this);
        // deprecated setting, but required on Android versions prior to 3.0
        mHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
        //mHolder.getSurface().setSize(previewSize.width, previewSize.height);       


        CharSequence noZoomText = "This Camera does not support zoom, please change camera and try again!";
        int duration = Toast.LENGTH_LONG;
        noZoomToast = Toast.makeText(context, noZoomText, duration);
        hasToastShowedUp=false;
    }
    
  

    public void surfaceCreated(SurfaceHolder holder) {
        // The Surface has been created, now tell the camera where to draw the preview.
        try {
            mCamera.setPreviewDisplay(holder);
            mCamera.startPreview();
        } catch (IOException e) {
            Log.d(TAG, "Error setting camera preview: " + e.getMessage());
        }
    }

    public void surfaceDestroyed(SurfaceHolder holder) {
        // empty. Take care of releasing the Camera preview in your activity.
    	mCamera.stopPreview();
    }

    public void surfaceChanged(SurfaceHolder holder, int format, int w, int h) {

        if (mHolder.getSurface() == null){
          // preview surface does not exist
          return;
        }

        // stop preview before making changes
        try {
            mCamera.stopPreview();
        } catch (Exception e){
          // ignore: tried to stop a non-existent preview
        }

        // start preview with new settings
        try {
        	//setCameraParameters();
        	mCamera.setPreviewDisplay(mHolder);
            mCamera.startPreview();

        } catch (Exception e){
            Log.d(TAG, "Error starting camera preview: " + e.getMessage());
        }
    }

	@Override
	public boolean onTouchEvent(MotionEvent event) {
    	long a=event.getDownTime();
    	int z=event.getPointerCount();
		//increaseZoom();
		//String actionString;
		
		
		switch (event.getAction())
		{
			case MotionEvent.ACTION_DOWN:
				//actionString = "DOWN";
				if(!eventY.isEmpty())
					eventY.clear();				
				eventY.add(event.getY());
				break;
			case MotionEvent.ACTION_MOVE:
				eventY.add(event.getY());
				float difference=eventY.get(0)-eventY.get(eventY.size()-1);
				if(difference>50){
					increaseZoom();
					eventY.clear();
				}else if(difference<-50){
					decreaseZoom();
					eventY.clear();
				}
				//actionString = "MOVE";
				break;			
		}
		//Log.d("ACTION", actionString);
		return true;
	}


	public void startPreview(){
    	try{
    		mCamera.startPreview();
    	}catch(Exception ex){
    		String exs=ex.toString();
    		Log.d("Camera Start Preview exception", exs);
    	}
    }
    
    public void stopPreview(){
    	mCamera.stopPreview();
    }
    
    public void releaseCamera(){
    	mCamera.release();
    	mCamera=null;
    	mHolder.removeCallback(this);
    }
    
    public void changeCamera(Camera camera){
    	mCamera = camera;
    	setCameraParameters();
        mHolder.addCallback(this);
        // deprecated setting, but required on Android versions prior to 3.0
        mHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
        //mCamera.setPreviewDisplay(holder);
        //mHolder.getSurface().setSize(previewSize.width, previewSize.height);
//        LayoutParams params=new LayoutParams();
//        params.width=previewSize.width;
//        params.height=previewSize.height;
//        this.setLayoutParams(params);
        hasToastShowedUp=false;
    }
    
    public void setCameraParameters(){
    	Parameters params=mCamera.getParameters();  
    	previewSize=params.getSupportedPreviewSizes().get(0);
        //Rotate preview according to screen rotation
        Display display = (windowManager).getDefaultDisplay();
        switch(display.getRotation()){
            case Surface.ROTATION_0:
                mCamera.setDisplayOrientation(90);
                //swapDimension();
                calculateCameraSurface(params,1);
                layoutParameters.height=previewSize.width;//This according to stackoverflow answer: http://stackoverflow.com/questions/17126633/camera-setdisplayorientation-in-portrait-mode-breaks-aspect-ratio
                layoutParameters.width=previewSize.height;
                this.setLayoutParams(layoutParameters);
                break;
            case Surface.ROTATION_270:
                mCamera.setDisplayOrientation(180);
                //swapDimension();
                calculateCameraSurface(params,0);
                layoutParameters.width=previewSize.width;
                layoutParameters.height=previewSize.height;
                this.setLayoutParams(layoutParameters);
                break;
            case Surface.ROTATION_180:
            	mCamera.setDisplayOrientation(0);
            	calculateCameraSurface(params,1);
            	layoutParameters.height=previewSize.width;
            	layoutParameters.width=previewSize.height;
                this.setLayoutParams(layoutParameters);
                break;
            case Surface.ROTATION_90:
            	mCamera.setDisplayOrientation(0);
            	calculateCameraSurface(params,0);
            	layoutParameters.width=previewSize.width;
                layoutParameters.height=previewSize.height;
                this.setLayoutParams(layoutParameters);
                break;
        }
        
    	params.setPreviewSize(previewSize.width, previewSize.height);    	
    	mCamera.setParameters(params);
    }
    
    @TargetApi(Build.VERSION_CODES.FROYO)
	public void increaseZoom(){
    	Parameters params=mCamera.getParameters();
    	if(params.isZoomSupported()){
	    	int zoom=params.getZoom();
	    	zoom+=1;
	    	int maxZoom=params.getMaxZoom();
	    	if(zoom>maxZoom)
	    		zoom=maxZoom;
	    	params.setZoom(zoom);
	    	mCamera.setParameters(params);
    	}else if(!hasToastShowedUp){
    		noZoomToast.show();
    		hasToastShowedUp=true;
    	}
    }
    
    @TargetApi(Build.VERSION_CODES.FROYO)
	public void decreaseZoom(){
    	Parameters params=mCamera.getParameters();
    	if(params.isZoomSupported()){
	    	int zoom=params.getZoom();
	    	zoom-=1;
	    	int maxZoom=params.getMaxZoom();
	    	if(zoom<0)
	    		zoom=0;
	    	params.setZoom(zoom);
	    	mCamera.setParameters(params);
    	} 
    }
    
    public boolean isZoomSupported(){
    	Parameters params=mCamera.getParameters();
    	return params.isZoomSupported();
    }
    
    /**
     * Calculate the surface of the camera 
     * @param params
     * @param i 0 for landscape, 1 for portrait
     */
    private void calculateCameraSurface(Parameters params, int i){
    	for(Camera.Size support : params.getSupportedPreviewSizes()){
            Log.i("Size: ", support.width+", "+support.height);
    		if(previewSize.width>support.width){
    			previewSize=support;
    		}
    	}
    	
    	for(Camera.Size support : params.getSupportedPreviewSizes()){
    		if((support.width>support.height)&&(support.width<screenSize[i])&&(previewSize.width<support.width)){
    			previewSize=support;
    		}
    	}    	    	
    }
   
}
