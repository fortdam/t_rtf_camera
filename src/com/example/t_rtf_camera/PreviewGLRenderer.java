package com.example.t_rtf_camera;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.ShortBuffer;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import android.opengl.GLES20;
import android.opengl.GLSurfaceView;

public class PreviewGLRenderer implements GLSurfaceView.Renderer{

	private GLCameraPreview mView;
	@Override
	public void onDrawFrame(GL10 gl) {
		// TODO Auto-generated method stub
		GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT);
		
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

class  GLCameraPreview {
	public static final int FILTER_NONE = 0;
	public static final int FILTER_GREY = 1;
	public static final int FILTER_SEPIA_TONE = 2;
	public static final int FILTER_NEGATIVE_COLOR = 3;
	public static final int FILTER_VIGNETTE = 4;
	public static final int FILTER_FISHEYE = 5;
	
	
    private final String vertexShaderCode =
            "attribute vec4 vPosition;" +
            "void main() {" +
            "  gl_Position = vPosition;" +
            "}";

        private final String fragmentShaderCode =
            "precision mediump float;" +
            "uniform vec4 vColor;" +
            "void main() {" +
            "  gl_FragColor = vColor;" +
            "}";
	
        private static float shapeCoords[] = { -0.5f,  0.5f, 0.0f,   // top left
            -0.5f, -0.5f, 0.0f,   // bottom left
            0.5f, -0.5f, 0.0f,   // bottom right
            0.5f,  0.5f, 0.0f }; // top right
        
        private static short drawOrder[] = { 0, 1, 2, 0, 2, 3};
         
        private static final int COORDS_PER_VERTEX = 3;
        
        
    	private final int mProgram;
    	private FloatBuffer mVertexBuffer;
    	private ShortBuffer mDrawListBuffer;
    	
    	
	public GLCameraPreview(final int filterType){
		
		int vertexShader = GLES20.glCreateShader(GLES20.GL_VERTEX_SHADER);
		int fragmentShader = GLES20.glCreateShader(GLES20.GL_FRAGMENT_SHADER);
		
		GLES20.glShaderSource(vertexShader, vertexShaderCode);
		GLES20.glShaderSource(fragmentShader, fragmentShaderCode);
		
		GLES20.glCompileShader(vertexShader);
		GLES20.glCompileShader(fragmentShader);
				
		mProgram = GLES20.glCreateProgram();
		GLES20.glAttachShader(mProgram, vertexShader);
		GLES20.glAttachShader(mProgram, fragmentShader);
		GLES20.glLinkProgram(mProgram);
		
		ByteBuffer bb = ByteBuffer.allocateDirect(4*shapeCoords.length);
		bb.order(ByteOrder.nativeOrder());
		
		mVertexBuffer = bb.asFloatBuffer();
		mVertexBuffer.put(shapeCoords);
		mVertexBuffer.position(0);
		
		ByteBuffer dlb = ByteBuffer.allocateDirect(drawOrder.length * 2);
		dlb.order(ByteOrder.nativeOrder());
		mDrawListBuffer = dlb.asShortBuffer();
		mDrawListBuffer.put(drawOrder);
		mDrawListBuffer.position(0);
		
	}
	
	public void draw(){
		GLES20.glUseProgram(mProgram);
		
		int positionHandler = GLES20.glGetAttribLocation(mProgram, "vPosition");
		int colorHandler = GLES20.glGetUniformLocation(mProgram, "vColor");
		
		GLES20.glEnableVertexAttribArray(positionHandler);
		
        GLES20.glVertexAttribPointer(positionHandler, COORDS_PER_VERTEX,
                GLES20.GL_FLOAT, false,
                COORDS_PER_VERTEX*4, mVertexBuffer);
		
		float color[] = { 0.63671875f, 0.76953125f, 0.22265625f, 1.0f };
		GLES20.glUniform4fv(colorHandler, 1, color, 0);
		
		GLES20.glDrawElements(GLES20.GL_TRIANGLES, drawOrder.length, GLES20.GL_UNSIGNED_SHORT, mDrawListBuffer);
	}
	

}