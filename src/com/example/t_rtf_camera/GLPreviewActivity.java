package com.example.t_rtf_camera;

import android.opengl.GLSurfaceView;
import android.os.Bundle;
import android.app.Activity;
import android.content.Context;
import android.graphics.SurfaceTexture;
import android.graphics.SurfaceTexture.OnFrameAvailableListener;
import android.hardware.Camera;
import android.util.Log;
import android.view.Menu;
import android.view.TextureView;
import android.view.WindowManager;



public class GLPreviewActivity extends Activity implements TextureView.SurfaceTextureListener, OnFrameAvailableListener{


	static private GLPreviewActivity appInst = null;
	private GLCameraRenderThread mRenderThread;
	
	static public GLPreviewActivity getAppInstance(){
		return appInst;
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		TextureView tempView = new TextureView(this);
		tempView.setSurfaceTextureListener(this);
		setContentView(tempView);

		appInst = this;
		
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
	}


    @Override
    protected void onPause() {
        super.onPause();
        // The following call pauses the rendering thread.
        // If your OpenGL application is memory intensive,
        // you should consider de-allocating objects that
        // consume significant memory here.
        //mGLSurfaceView.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        // The following call resumes a paused rendering thread.
        // If you de-allocated graphic objects for onPause()
        // this is a good place to re-allocate them.
        //mGLSurfaceView.onResume();
    }
    
    synchronized public void startCamera(int texture){
    	if (null == mSurfaceTexture){
	    	mCamera = getCameraInstance();
	    	
	    	mSurfaceTexture = new SurfaceTexture(texture);
	    	
	    	mSurfaceTexture.setOnFrameAvailableListener(this);
	    	
	    	try{
	    		mCamera.setDisplayOrientation(90);
	    		mCamera.setPreviewTexture(mSurfaceTexture);
	    		mCamera.startPreview();
	    	}
	    	catch (Exception e){
	    		e.printStackTrace();
	    	}
    	}
    	else {
    		mSurfaceTexture.attachToGLContext(texture);
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

	@Override
	public void onFrameAvailable(SurfaceTexture surfaceTexture) {
		// TODO Auto-generated method stubi
		int a = 1;
		Log.i("tang", "here");
		synchronized(mRenderThread){
			mRenderThread.notify();
		}
	}
	
	public void updateCamPreview(){
		mSurfaceTexture.updateTexImage();
	}
	
	private Camera mCamera;
	//private GLSurfaceView mGLSurfaceView;
	private SurfaceTexture mSurfaceTexture = null;

	@Override
	public void onSurfaceTextureAvailable(SurfaceTexture surface, int width,
			int height) {
		// TODO Auto-generated method stub
		mRenderThread = new GLCameraRenderThread(surface, GLCameraRenderThread.FILTER_SEPIA_TONE);
		mRenderThread.setRegion(width, height);
		mRenderThread.start();
	}

	@Override
	public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width,
			int height) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onSurfaceTextureUpdated(SurfaceTexture surface) {
		// TODO Auto-generated method stub
		
	}
	

}

/*
class PreviewGLSurfaceView extends GLSurfaceView {
	public PreviewGLSurfaceView(Context context){
		super(context);
		
		setEGLContextClientVersion(2);
		//setRenderMode(GLSurfaceView.RENDERMODE_CONTINUOUSLY);
		
		setRenderer(new PreviewGLRenderer());
		setRenderMode(GLSurfaceView.RENDERMODE_WHEN_DIRTY);

	}
}
*/