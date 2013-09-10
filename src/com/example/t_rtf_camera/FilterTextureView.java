package com.example.t_rtf_camera;

import android.content.Context;
import android.view.MotionEvent;
import android.view.TextureView;

public class FilterTextureView extends TextureView{
	
	private int mIndex = 0;
	
	public FilterTextureView(Context context, int index) {
		super(context);
		mIndex = index;
		// TODO Auto-generated constructor stub
	}

	@Override
	public boolean onTouchEvent(MotionEvent event){
		int action = event.getActionMasked();
		
		GLPreviewActivity app = GLPreviewActivity.getAppInstance();
		
		if (MotionEvent.ACTION_DOWN == action){
			if (app.isZoomIn()){
				app.zoomOut();
			}
			else {
				app.zoomIn(mIndex);
			}
			return true;
		}
		return false;
	}
}
