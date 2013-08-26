package com.example.t_rtf_camera;

import java.util.concurrent.Semaphore;

import android.content.Context;
import android.graphics.Canvas;
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
	
	
	public void drawFrame(final int[] data){
		
		if (mSurfaceHolder == null){
			return;
		}
		
		new Thread(){
			public void run(){
				synchronized(data){
					//int width = Math.min(mPicWidth, mWidth);
					//int height = Math.min(mPicHeight, mHeight);
					Canvas c = mSurfaceHolder.lockCanvas();
					
					//c.drawARGB(0xff, red, green, blue);
					//c.scale(((float)mWidth)/mPicWidth, ((float)mHeight)/mPicHeight);
					c.drawBitmap(data, 0, mPicWidth, 0, 0, mPicWidth, mPicHeight, false, null);
					
					mSurfaceHolder.unlockCanvasAndPost(c);
				}
			}
		}.start();
	}
}
