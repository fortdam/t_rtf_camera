package com.example.t_rtf_camera;

import java.io.IOException;
import java.util.List;


import android.app.Activity;
import android.content.Context;
import android.graphics.ImageFormat;
import android.hardware.Camera;
import android.util.Log;
import android.view.Display;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.WindowManager;

/** A basic Camera preview class */
public class CameraPreview extends SurfaceView implements SurfaceHolder.Callback {
	private String TAG="t_rtf_camera";
	
    private SurfaceHolder mHolder;
    private Camera mCamera;
    
    private Camera.PreviewCallback mPreviewCallback = null;
    
    private boolean isBigScreen = false;

    public CameraPreview(Context context, Camera camera, Camera.PreviewCallback callback) {
        super(context);
        mCamera = camera;

        // Install a SurfaceHolder.Callback so we get notified when the
        // underlying surface is created and destroyed.
        mHolder = getHolder();
        mHolder.addCallback(this);
        mPreviewCallback = callback;
       
        Activity app = (Activity)context;
        WindowManager wm = app.getWindowManager();
        Display disp = wm.getDefaultDisplay();
        int width = disp.getWidth();
        
        if (width >= 960){
        	isBigScreen = true; 
        }
    }

    public void surfaceCreated(SurfaceHolder holder) {
        // The Surface has been created, now tell the camera where to draw the preview.
        try {
        	Camera.Parameters params = mCamera.getParameters();

            List<Camera.Size> sizes = params.getSupportedPreviewSizes();
            for (int i=0; i < sizes.size(); i++){
                Camera.Size size = sizes.get(i);
            }
            
            if (false == isBigScreen){
            	params.setPreviewSize(320, 240);
            }
        	params.setPreviewFormat(ImageFormat.RGB_565);
        	params.setPreviewFrameRate(30);
        	int width = params.getPreviewSize().width;
        	int height = params.getPreviewSize().height;
        	
        	mCamera.setParameters(params);
        	
        	byte[] buffer = new byte[width*height*2];
        	
        	mCamera.addCallbackBuffer(buffer);
            
            mCamera.setPreviewDisplay(holder);
            mCamera.startPreview();         
            
            mCamera.setPreviewCallback(mPreviewCallback);


        } catch (IOException e) {
            Log.d(TAG, "Error setting camera preview: " + e.getMessage());
        }
    }

    public void surfaceDestroyed(SurfaceHolder holder) {
        // empty. Take care of releasing the Camera preview in your activity.
    }

    public void surfaceChanged(SurfaceHolder holder, int format, int w, int h) {
        // If your preview can change or rotate, take care of those events here.
        // Make sure to stop the preview before resizing or reformatting it.
    	
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

        // set preview size and make any resize, rotate or
        // reformatting changes here

        // start preview with new settings
        try {
            mCamera.setPreviewDisplay(mHolder);
            mCamera.startPreview();
        	mCamera.setPreviewCallback(mPreviewCallback);

        } catch (Exception e){
            Log.d(TAG, "Error starting camera preview: " + e.getMessage());
        }
    }
}