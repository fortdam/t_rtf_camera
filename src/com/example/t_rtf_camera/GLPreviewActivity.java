package com.example.t_rtf_camera;

import java.util.Timer;
import java.util.TimerTask;

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
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.ViewTreeObserver.OnDrawListener;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;




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
			mRenderThread[mIndex].setRegion(width, height);
		}

		@Override
		public void onSurfaceTextureUpdated(SurfaceTexture surface) {
			// TODO Auto-generated method stub
			
		}
		
	}
	
	private int mZoomIndex = -1;
	
	private int mAnimXOffset = 0;
	private int mAnimYOffset = 0;
	private ViewGroup.LayoutParams mBackupZoomedLP = null;
	
	
	public boolean isZoomIn(){
		return (mZoomIndex >= 0); 
	}
	
	public void zoomIn(final int index){
		final View frame = (View)findViewById(R.id.screen);
		
		mAnimXOffset = (index%3 - 1) * (-1080);
		mAnimYOffset = (index/3 - 1) * (-1920);
		
		frame.animate().scaleX(3.0f).scaleY(3.0f).translationX(mAnimXOffset).translationY(mAnimYOffset).withEndAction(new Runnable(){

			@Override
			public void run() {
				frame.setScaleX(1.0f);
				frame.setScaleY(1.0f);
				frame.setTranslationX(0);
				frame.setTranslationY(0);
				
				for (int i=0; i<9; i++){
					View preview = (View)findViewById(R.id.gl_preview1+i);

					if (i != index){
						preview.setVisibility(View.GONE);
						mRenderThread[i].suspendRendering();
					}
					else {
						final GLCameraRenderThread renderer = mRenderThread[i];
						mBackupZoomedLP = preview.getLayoutParams();
						RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(1080, 1920);
						preview.setLayoutParams(params);
						renderer.suspendRendering();
						
						final ViewTreeObserver observer = frame.getViewTreeObserver();
						observer.addOnDrawListener(new OnDrawListener(){

							@Override
							public void onDraw() {
								// TODO Auto-generated method stub
								observer.removeOnDrawListener(this);
								
								frame.post(new Runnable(){

									@Override
									public void run() {
										// TODO Auto-generated method stub
										renderer.resumeRendering();
										onFrameAvailable(null);
									}
									
								});
							}
							
						});
						
					}
				}
				//frame.requestLayout();				
			}
			
		});
		
		mZoomIndex = index;
	}
	
	public void zoomOut(){
		final View frame = (View)findViewById(R.id.screen);
		
		View zoomedPreview = (View)findViewById(R.id.gl_preview1+mZoomIndex);
		zoomedPreview.setLayoutParams(mBackupZoomedLP);
				
		for (int i=0; i<9; i++){
			View preview = (View)findViewById(R.id.gl_preview1+i);
			preview.setVisibility(View.VISIBLE);
		}
		
		mRenderThread[mZoomIndex].suspendRendering();
		frame.requestLayout();
		
		frame.setScaleX(3.0f);
		frame.setScaleY(3.0f);
		frame.setTranslationX(mAnimXOffset);
		frame.setTranslationY(mAnimYOffset);
				
		frame.animate().scaleX(1.0f).scaleY(1.0f).translationX(0).translationY(0).withEndAction(new Runnable(){

			@Override
			public void run() {
				// TODO Auto-generated method stub
				for (int i=0; i<9; i++){
					mRenderThread[i].resumeRendering();
				}
				onFrameAvailable(null);//Since nobody updated after last frame, we kick it again
			}
			
		});
		
		mZoomIndex = -1;
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
		texture = new FilterTextureView(this, 0);
		texture.setSurfaceTextureListener(new TextureCallback(0, GLCameraRenderThread.FILTER_GREY));
		
		frame.addView(texture);

		frame = (FrameLayout)findViewById(R.id.gl_preview2);
		texture = new FilterTextureView(this, 1);
		texture.setSurfaceTextureListener(new TextureCallback(1, GLCameraRenderThread.FILTER_CYAN));
		frame.addView(texture);
		
		frame = (FrameLayout)findViewById(R.id.gl_preview3);
		texture = new FilterTextureView(this, 2);
		texture.setSurfaceTextureListener(new TextureCallback(2, GLCameraRenderThread.FILTER_SEPIA_TONE));
		frame.addView(texture);
		
		frame = (FrameLayout)findViewById(R.id.gl_preview4);
		texture = new FilterTextureView(this, 3);
		texture.setSurfaceTextureListener(new TextureCallback(3, GLCameraRenderThread.FILTER_NONE));
		frame.addView(texture);

		frame = (FrameLayout)findViewById(R.id.gl_preview5);
		texture = new FilterTextureView(this, 4);
		texture.setSurfaceTextureListener(new TextureCallback(4, GLCameraRenderThread.FILTER_FISHEYE));
		frame.addView(texture);
		
		frame = (FrameLayout)findViewById(R.id.gl_preview6);
		texture = new FilterTextureView(this, 5);
		texture.setSurfaceTextureListener(new TextureCallback(5, GLCameraRenderThread.FILTER_NEGATIVE_COLOR));
		frame.addView(texture);
		
		frame = (FrameLayout)findViewById(R.id.gl_preview7);
		texture = new FilterTextureView(this, 6);
		texture.setSurfaceTextureListener(new TextureCallback(6, GLCameraRenderThread.FILTER_H_MIRROR));
		frame.addView(texture);
		
		frame = (FrameLayout)findViewById(R.id.gl_preview8);
		texture = new FilterTextureView(this, 7);
		texture.setSurfaceTextureListener(new TextureCallback(7, GLCameraRenderThread.FILTER_RADIAL_BLUR));
		frame.addView(texture);
		
		frame = (FrameLayout)findViewById(R.id.gl_preview9);
		texture = new FilterTextureView(this, 8);
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
	    		Camera.Parameters params = mCamera.getParameters();

	    		params.setPreviewSize(960, 540);
	    		mCamera.setParameters(params);
	    		mCamera.setDisplayOrientation(90);
	    		mCamera.setPreviewTexture(mSurfaceTexture);
	    		mCamera.startPreview();
	    		
	    		mAFTimer = new Timer();
	    		mAFTimer.scheduleAtFixedRate(new TimerTask(){

					@Override
					public void run() {
						// TODO Auto-generated method stub
						mCamera.autoFocus(null);
						
					}
	    			
	    		}, 5000, 3000);
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
		// TODO Auto-generated method stub

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
	private Timer mAFTimer;
	//private GLSurfaceView mGLSurfaceView;
	private SurfaceTexture mSurfaceTexture = null;
}
