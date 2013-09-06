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
import android.widget.FrameLayout;




public class GLPreviewActivity extends Activity implements OnFrameAvailableListener{
	
	class TextureCallback implements TextureView.SurfaceTextureListener {
		private int mIndex;
		private int mFilter;
		
		TextureCallback(int index, int filter){
			mIndex = index;
			mFilter = filter;
		}

		@Override
		public void onSurfaceTextureAvailable(SurfaceTexture surface,
				int width, int height) {
			mRenderThread[mIndex] = new GLCameraRenderThread(surface, mFilter);
			mRenderThread[mIndex].setRegion(width, height);
			mRenderThread[mIndex].start();		
		}

		@Override
		public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
			// TODO Auto-generated method stub
			return false;
		}

		@Override
		public void onSurfaceTextureSizeChanged(SurfaceTexture surface,
				int width, int height) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void onSurfaceTextureUpdated(SurfaceTexture surface) {
			// TODO Auto-generated method stub
			
		}
		
	}


	static private GLPreviewActivity appInst = null;
	private GLCameraRenderThread mRenderThread[] = new GLCameraRenderThread[9];
	private int mActiveRender = 0;
	
	static public GLPreviewActivity getAppInstance(){
		return appInst;
	}
	

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_glpreview);
		
		FrameLayout frame;
		TextureView texture;
		
		frame = (FrameLayout)findViewById(R.id.gl_preview1);
		texture = new TextureView(this);
		texture.setSurfaceTextureListener(new TextureCallback(0, GLCameraRenderThread.FILTER_GREY));
		
		frame.addView(texture);

		frame = (FrameLayout)findViewById(R.id.gl_preview2);
		texture = new TextureView(this);
		texture.setSurfaceTextureListener(new TextureCallback(1, GLCameraRenderThread.FILTER_CYAN));
		frame.addView(texture);
		
		frame = (FrameLayout)findViewById(R.id.gl_preview3);
		texture = new TextureView(this);
		texture.setSurfaceTextureListener(new TextureCallback(2, GLCameraRenderThread.FILTER_SEPIA_TONE));
		frame.addView(texture);
		
		frame = (FrameLayout)findViewById(R.id.gl_preview4);
		texture = new TextureView(this);
		texture.setSurfaceTextureListener(new TextureCallback(3, GLCameraRenderThread.FILTER_NONE));
		frame.addView(texture);

		frame = (FrameLayout)findViewById(R.id.gl_preview5);
		texture = new TextureView(this);
		texture.setSurfaceTextureListener(new TextureCallback(4, GLCameraRenderThread.FILTER_FISHEYE));
		frame.addView(texture);
		
		frame = (FrameLayout)findViewById(R.id.gl_preview6);
		texture = new TextureView(this);
		texture.setSurfaceTextureListener(new TextureCallback(5, GLCameraRenderThread.FILTER_NEGATIVE_COLOR));
		frame.addView(texture);
		
		frame = (FrameLayout)findViewById(R.id.gl_preview7);
		texture = new TextureView(this);
		texture.setSurfaceTextureListener(new TextureCallback(6, GLCameraRenderThread.FILTER_H_MIRROR));
		frame.addView(texture);
		
		frame = (FrameLayout)findViewById(R.id.gl_preview8);
		texture = new TextureView(this);
		texture.setSurfaceTextureListener(new TextureCallback(7, GLCameraRenderThread.FILTER_RADIAL_BLUR));
		frame.addView(texture);
		
		frame = (FrameLayout)findViewById(R.id.gl_preview9);
		texture = new TextureView(this);
		texture.setSurfaceTextureListener(new TextureCallback(8, GLCameraRenderThread.FILTER_V_MIRROR));
		frame.addView(texture);
		
		mActiveRender = 9;
		
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
        // this is a good place to re-allocate them.attachToGLContext
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
	    	
	    	mSurfaceTexture.detachFromGLContext();
    	}
    	else {
    		//mSurfaceTexture.attachToGLContext(texture);
    	}
    }

    public void attachCameraTexture(int texture){
    	mSurfaceTexture.attachToGLContext(texture);
    }
    
    public void detachCAmeraTexture() {
    	mSurfaceTexture.detachFromGLContext();
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

		for (int i=0; i<mActiveRender; i++){
			synchronized(mRenderThread[i]){
				mRenderThread[i].notify();
			}
		}
	}
	
	public void updateCamPreview(){
		mSurfaceTexture.updateTexImage();
	}
	
	private Camera mCamera;
	//private GLSurfaceView mGLSurfaceView;
	private SurfaceTexture mSurfaceTexture = null;
}
