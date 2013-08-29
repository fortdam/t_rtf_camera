package com.example.t_rtf_camera;

import android.opengl.GLSurfaceView;
import android.os.Bundle;
import android.app.Activity;
import android.content.Context;
import android.view.Menu;



public class GLPreviewActivity extends Activity {
	

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		mGLView = new PreviewGLSurfaceView(this);
		setContentView(mGLView);
	}


    @Override
    protected void onPause() {
        super.onPause();
        // The following call pauses the rendering thread.
        // If your OpenGL application is memory intensive,
        // you should consider de-allocating objects that
        // consume significant memory here.
        mGLView.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        // The following call resumes a paused rendering thread.
        // If you de-allocated graphic objects for onPause()
        // this is a good place to re-allocate them.
        mGLView.onResume();
    }


	private GLSurfaceView mGLView;
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