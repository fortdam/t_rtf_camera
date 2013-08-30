package com.example.t_rtf_camera;

import android.opengl.GLSurfaceView;
import android.os.Bundle;
import android.app.Activity;
import android.content.Context;
import android.graphics.SurfaceTexture;
import android.hardware.Camera;
import android.view.Menu;
import android.view.WindowManager;



public class GLPreviewActivity extends Activity implements SurfaceTexture.OnFrameAvailableListener{


	static private GLPreviewActivity appInst = null;
	
	static public GLPreviewActivity getAppInstance(){
		return appInst;
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		mGLSurfaceView = new PreviewGLSurfaceView(this);
		setContentView(mGLSurfaceView);

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
        mGLSurfaceView.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        // The following call resumes a paused rendering thread.
        // If you de-allocated graphic objects for onPause()
        // this is a good place to re-allocate them.
        mGLSurfaceView.onResume();
    }
    
    public void startCamera(int texture){
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
		// TODO Auto-generated method stub
		mGLSurfaceView.requestRender();
	}
	
	public void updateCamPreview(){
		mSurfaceTexture.updateTexImage();
	}
	
	private Camera mCamera;
	private GLSurfaceView mGLSurfaceView;
	private SurfaceTexture mSurfaceTexture;
}

class PreviewGLSurfaceView extends GLSurfaceView {
	public PreviewGLSurfaceView(Context context){
		super(context);
		
		setEGLContextClientVersion(2);
		//setRenderMode(GLSurfaceView.RENDERMODE_CONTINUOUSLY);
		
		setRenderer(new PreviewGLRenderer());
		setRenderMode(GLSurfaceView.RENDERMODE_WHEN_DIRTY);

	}
}