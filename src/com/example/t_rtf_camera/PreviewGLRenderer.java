package com.example.t_rtf_camera;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.nio.ShortBuffer;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import android.graphics.Bitmap;
import android.graphics.SurfaceTexture;
import android.hardware.Camera;
import android.opengl.GLES20;
import android.opengl.GLES11Ext;
import android.opengl.GLSurfaceView;
import android.opengl.GLUtils;
import android.util.Log;

public class PreviewGLRenderer implements GLSurfaceView.Renderer{

	private GLCameraPreview mView;
	
	@Override
	public void onDrawFrame(GL10 gl) {
		// TODO Auto-generated method stub
		GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT);
		
		GLPreviewActivity app = GLPreviewActivity.getAppInstance();
		app.updateCamPreview();
		
		mView.draw();
	}

	@Override
	public void onSurfaceChanged(GL10 gl, int width, int height) {
		// TODO Auto-generated method stub
		GLES20.glViewport(0,0,width,height);
	}

	@Override
	public void onSurfaceCreated(GL10 gl, EGLConfig config) {
		// TODO Auto-generated method stub
		GLES20.glClearColor(1.0f, 0, 0, 1.0f);
		
		mView = new GLCameraPreview(0);
	}

}

