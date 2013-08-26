package com.example.t_rtf_camera;

import java.util.concurrent.Semaphore;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.os.SystemClock;
import android.renderscript.Allocation;
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
	
	private Thread mThread = null;
	
	private SurfaceHolder mHolder = null;
	
	
	public FilterPreview(Context context, Semaphore dataReady, int[] data, int width, int height){
		super(context);
		
		mHolder = getHolder();
		mHolder.addCallback(this);
			
		mDispData = data;
		mPicWidth = width;
		mPicHeight = height;
		mDataReady = dataReady; 
	}

	public void setDisplayData(int[] data){
		mDispData = data;
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
	
	
	public void drawFrame(final int[] data, final boolean debug){
		
		if (mSurfaceHolder == null){
			return;
		}
		
		final long curr = SystemClock.uptimeMillis();
		final long diff = curr - mLastFrameTime;
		mLastFrameTime = curr;
		
		new Thread(){
			public void run(){
				synchronized(data){
					Canvas c = mSurfaceHolder.lockCanvas();
					
					c.drawBitmap(data, 0, mPicWidth, 0, 0, mPicWidth, mPicHeight, false, null);

					if (debug){
						int fps = (int) (1000/diff);
						Paint pt = new Paint();
						pt.setColor(0xffffffff);
						pt.setTextSize(30);
						if (fps>=10){
							c.drawText("fps: "+fps, 100, 100, pt);
						}
						else{
							c.drawText("fps: 0"+fps, 100, 100, pt);
						}
					}
					
					mSurfaceHolder.unlockCanvasAndPost(c);
				}
			}
		}.start();
	}
}
