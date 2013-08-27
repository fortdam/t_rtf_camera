package com.example.t_rtf_camera;

import java.util.concurrent.Semaphore;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.os.SystemClock;
import android.renderscript.Allocation;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceHolder.Callback;
import android.view.SurfaceView;

public class FilterPreview extends SurfaceView implements SurfaceHolder.Callback{

	private int[] mDispData = null;
	public SurfaceHolder mSurfaceHolder = null;
	private Semaphore mDataReady = null;
	
	private int mWidth = 0;
	private int mHeight = 0;
	
	private int mPicWidth = 0;
	private int mPicHeight = 0;
	
	private long mLastFrameTime = 0;
	private long mFrameCountStartTime = 0;
	private long mFrameCount = 0;
	private long mDispFrameCount = 0;
	
	private Thread mThread = null;
	
	private SurfaceHolder mHolder = null;
	
	
	public FilterPreview(Context context, int[] data, int width, int height){
		super(context);
		
		mHolder = getHolder();
		mHolder.addCallback(this);
			
		mDispData = data;
		mPicWidth = width;
		mPicHeight = height;
	}
	
	@Override
	public void surfaceChanged(SurfaceHolder holder, int format, int width,
			int height) {
		// TODO Auto-generated method stub
		mSurfaceHolder = holder;
		mWidth = width;
		mHeight = height;
	}

	@Override
	public void surfaceCreated(SurfaceHolder holder) {
		// TODO Auto-generated method stub
		mSurfaceHolder = holder;
	}

	@Override
	public void surfaceDestroyed(SurfaceHolder holder) {
		// TODO Auto-generated method stub
		
	}
	
	
	public void drawFrame(final Bitmap bmp, final boolean debug){
		
		if (mSurfaceHolder == null){
			return;
		}
		
		
		final long curr = SystemClock.uptimeMillis();
		final long diff = curr - mLastFrameTime;
		mLastFrameTime = curr;
		
		if (mFrameCountStartTime == 0){
			mFrameCountStartTime = curr;
			mFrameCount = -1;
		}
		else if (mFrameCount > 0){
			mFrameCount = -1;
		}
		else {
			mFrameCount--;
			if ((curr - mFrameCountStartTime)>1000){
				mFrameCountStartTime = curr;
				mFrameCount = 0-mFrameCount;
				mDispFrameCount = mFrameCount;
			}
		}
		
		new Thread(){
			public void run(){
				synchronized(bmp){
					Canvas c = mSurfaceHolder.lockCanvas();
					
					c.drawBitmap(bmp, 0, 0, null);

					if (debug){
						int fps = (int) (1000/diff);
						Paint pt = new Paint();
						pt.setColor(0xffffffff);
						pt.setTextSize(30);
						if (fps>=10){
							c.drawText("fps: "+fps+"/"+mDispFrameCount, 100, 100, pt);
						}
						else{
							c.drawText("fps: 0"+fps+"/"+mDispFrameCount, 100, 100, pt);
						}
					}
					
					mSurfaceHolder.unlockCanvasAndPost(c);
				}
			}
		}.start();
	}
	
	public boolean onTouchEvent(MotionEvent event){
		if (event.ACTION_DOWN == event.getAction()){
			if (mActive){
				mActive = false;
			}
			else {
				mActive = true;
			}
		}
		return false;
	}
	
	private boolean mActive = true;
	
	public boolean isActive(){
		return mActive;
	}
}
