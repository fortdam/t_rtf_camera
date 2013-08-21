package com.example.t_rtf_camera;

import android.os.Bundle;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.hardware.Camera;
import android.util.Log;
import android.view.Menu;
import android.view.SurfaceView;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.os.SystemClock;

public class PreviewActivity extends Activity implements Camera.PreviewCallback{

	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_preview);
		
        // Create an instance of Camera
        mCamera = getCameraInstance();
        mCamera.setDisplayOrientation(90);

        // Create our Preview view and set it as the content of our activity.
        mPreview = new CameraPreview(this, mCamera, this);
                
        FrameLayout preview = (FrameLayout) findViewById(R.id.camera_preview);
        preview.addView(mPreview);
        
        mBlackWhiteView = (ImageView) findViewById(R.id.bnw_preview);
        mSepiaToneView = (ImageView) findViewById(R.id.sepia_tone_preview);
        mRevertView = (ImageView) findViewById(R.id.revert_preview);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.activity_preview, menu);
		return true;
	}
	
	
	
	/** Check if this device has a camera */
	private boolean checkCameraHardware(Context context) {
	    if (context.getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA)){
	        // this device has a camera
	        return true;
	    } else {
	        // no camera on this device
	        return false;
	    }
	}
	
	/** A safe way to get an instance of the Camera object. */
	public static Camera getCameraInstance(){
	    Camera c = null;
	    try {
	        c = Camera.open(); // attempt to get a Camera instance
	    }
	    catch (Exception e){
	        // Camera is not available (in use or does not exist)
	    }
	    return c; // returns null if camera is unavailable
	}
	
	private int min(int a, int b){
		if (a>b){
			return b;
		}
		else{
			return a;
		}
	}

	@Override
	public void onPreviewFrame(byte[] data, Camera camera) {
		// TODO Auto-generated method stub
		long startTime = SystemClock.uptimeMillis();
		long midTime = 0;
		long endTime = 0;
		
		Camera.Parameters params = camera.getParameters();
		int width = params.getPreviewSize().width;
		int height = params.getPreviewSize().height;
		
		int[] colorsBNW = new int[width*height];
		int[] colorsSepiaTone = new int[width*height];
		int[] colorsRevert = new int[width*height];
		
		for (int i=0; i<width*height; i++){
			
			int red = (data[i*2+1] & 0xf8);
			int green = (data[i*2+1] & 0x07)<<5 | (data[i*2] & 0xe0)>>3;
			int blue = (data[i*2] & 0x1f) << 3;
			int greyScale = (red+green+blue)/3;
			int pos = (i/width) + (i%width)*height;  //rotate 90 degree

			colorsBNW[pos] = 0xff000000;
			colorsBNW[pos] |= greyScale << 16;
			colorsBNW[pos] |= greyScale << 8;
			colorsBNW[pos] |= greyScale;
			
			colorsSepiaTone[pos] = 0xff000000;
			colorsSepiaTone[pos] |= (min((int)(red*0.393f + green*0.769f + blue*0.189f), 255)) << 16;
			colorsSepiaTone[pos] |= (min((int)(red*0.349f + green*0.686f + blue*0.168f), 255)) << 8;
			colorsSepiaTone[pos] |= (min((int)(red*0.272f + green*0.534 + blue*0.131f), 255));
			
			colorsRevert[pos] = 0xff000000;
			colorsRevert[pos] |= (0xff ^ red)<<16;
			colorsRevert[pos] |= (0xff ^ green)<<8;
			colorsRevert[pos] |= 0xff ^ blue;
		}
		
		midTime = SystemClock.uptimeMillis();
		
		Bitmap bmpBNW = Bitmap.createBitmap(colorsBNW, height, width, Bitmap.Config.ARGB_8888);
		mBlackWhiteView.setImageBitmap(bmpBNW);
		
		Bitmap bmpSepiaTone = Bitmap.createBitmap(colorsSepiaTone, height, width, Bitmap.Config.ARGB_8888);
		mSepiaToneView.setImageBitmap(bmpSepiaTone);
		
		Bitmap bmpRevert = Bitmap.createBitmap(colorsRevert, height, width, Bitmap.Config.ARGB_8888);
		mRevertView.setImageBitmap(bmpRevert);
		
		endTime = SystemClock.uptimeMillis();
		
		Log.e("Tang zhiming", "data process="+(midTime-startTime)+" , total="+(endTime-startTime));
		
		
	}
	
	
	
	private Camera mCamera;
	private CameraPreview mPreview;
	private ImageView mBlackWhiteView;
	private ImageView mSepiaToneView;
	private ImageView mRevertView;
}
