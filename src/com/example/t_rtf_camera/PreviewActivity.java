package com.example.t_rtf_camera;


import java.util.concurrent.Semaphore;

import android.os.Bundle;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.hardware.Camera;
import android.util.Log;
import android.view.Menu;
import android.view.SurfaceView;
import android.view.View;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.os.SystemClock;
import android.renderscript.Allocation;
import android.renderscript.Element;
import android.renderscript.RenderScript;
import android.renderscript.Type;

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
        
        mRS = RenderScript.create(this);
        
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
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
		
		if (null == mInAllocation){
			Type.Builder inTypeBuilder = new Type.Builder(mRS, Element.U8_2(mRS));
			Type.Builder outTypeBuilder = new Type.Builder(mRS, Element.U8_4(mRS));
			Type.Builder resultTypeBuilder = new Type.Builder(mRS, Element.U8_4(mRS));
			
			inTypeBuilder.setX(width).setY(height);
			outTypeBuilder.setX(height).setY(width);
			resultTypeBuilder.setX(height).setY(width);
			
			Type inType = inTypeBuilder.create();
			Type outType = outTypeBuilder.create();
			Type resultType = resultTypeBuilder.create();
			
			mInAllocation = Allocation.createTyped(mRS, inType);	
		    mRotateAllocation = Allocation.createTyped(mRS, outType);
		    
		    mBlackWhiteBmp = Bitmap.createBitmap(height,width,Bitmap.Config.ARGB_8888);
		    mSepiaToneBmp = Bitmap.createBitmap(height,width,Bitmap.Config.ARGB_8888);
		    mRevertBmp = Bitmap.createBitmap(height,width,Bitmap.Config.ARGB_8888);
		    
		    mBlackWhiteAllocation = Allocation.createFromBitmap(mRS, mBlackWhiteBmp);
		    mSepiaToneAllocation = Allocation.createFromBitmap(mRS, mSepiaToneBmp);
		    mRevertAllocation = Allocation.createFromBitmap(mRS, mRevertBmp);
		    
            int[] rowIndices = new int[height];
            for (int i = 0; i < height; i++) {
                rowIndices[i] = i;
            }
            mRowIndicesAllocaction = Allocation.createSized(mRS, Element.I32(mRS), height, Allocation.USAGE_SCRIPT);
            mRowIndicesAllocaction.copyFrom(rowIndices);
		    
		    mScript = new ScriptC_filter(mRS, getResources(), R.raw.filter);
		    
		    mScript.set_mImageWidth(width);
		    mScript.set_mImageHeight(height);
		    		    
		    //will 90 rotate
		   mBNWPreview = new FilterPreview(this, null, height, width);
		   mSepiaTonePreview = new FilterPreview(this, null, height, width);
		   mRevertPreview = new FilterPreview(this, null, height, width);
		    
		   
		    mBNWPreview.getHolder().setFixedSize(height, width);
		    FrameLayout container = (FrameLayout)findViewById(R.id.bnw_preview);
		    container.addView(mBNWPreview);
		    
		    mSepiaTonePreview.getHolder().setFixedSize(height, width);
		    container = (FrameLayout)findViewById(R.id.sepia_tone_preview);
		    container.addView(mSepiaTonePreview);
		    
		    mRevertPreview.getHolder().setFixedSize(height, width);
		    container = (FrameLayout)findViewById(R.id.revert_preview);
		    container.addView(mRevertPreview);
		    
		}
		
		mInAllocation.copyFromUnchecked(data);
		camera.addCallbackBuffer(data);
		
		mScript.bind_gInPixels(mInAllocation);
		mScript.bind_gRotatePixels(mRotateAllocation);
		
		mScript.forEach_root(mRowIndicesAllocaction, mRowIndicesAllocaction);
		
		if (mBNWPreview.isActive()){
			mScript.forEach_blackwhite(mRotateAllocation, mBlackWhiteAllocation);
			synchronized(mBlackWhiteBmp){
				mBlackWhiteAllocation.copyTo(mBlackWhiteBmp);
			}
			mBNWPreview.drawFrame(mBlackWhiteBmp, true);
		}
		
		if (mSepiaTonePreview.isActive()){
			mScript.forEach_sepiatone(mRotateAllocation, mSepiaToneAllocation);
			synchronized(mSepiaToneBmp){
				mSepiaToneAllocation.copyTo(mSepiaToneBmp);
			}
			mSepiaTonePreview.drawFrame(mSepiaToneBmp, true);
		}
		
		if (mRevertPreview.isActive()){
			mScript.forEach_revert(mRotateAllocation, mRevertAllocation);
			synchronized(mRevertBmp){
				mRevertAllocation.copyTo(mRevertBmp);
			}
			mRevertPreview.drawFrame(mRevertBmp, true);
		}
		
		midTime = SystemClock.uptimeMillis();
		
		endTime = SystemClock.uptimeMillis();
		
		Log.e("Tang zhiming", "total="+(endTime-startTime));
		
		
	}
	
	
	
	private Camera mCamera;
	private CameraPreview mPreview;
	
	private FilterPreview mBNWPreview;
	private FilterPreview mSepiaTonePreview;
	private FilterPreview mRevertPreview;
	
	private Bitmap mBlackWhiteBmp; 
	private Bitmap mSepiaToneBmp;
	private Bitmap mRevertBmp;
	
    private RenderScript mRS;
    private ScriptC_filter mScript = null;
    private Allocation mInAllocation = null;
    private Allocation mRotateAllocation = null;
    
    private Allocation mBlackWhiteAllocation = null;
    private Allocation mSepiaToneAllocation = null;
    private Allocation mRevertAllocation = null;
    
    private Allocation mRowIndicesAllocaction = null;
}
