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

class  GLCameraPreview {
	public static final int FILTER_NONE = 0;
	public static final int FILTER_GREY = 1;
	public static final int FILTER_SEPIA_TONE = 2;
	public static final int FILTER_NEGATIVE_COLOR = 3;
	public static final int FILTER_VIGNETTE = 4;
	public static final int FILTER_FISHEYE = 5;
	
	
    private final String vertexShaderCode =
            "attribute vec4 vPosition;\n" +
            "attribute vec2 vTexCoord;\n" +
            "varying vec2 outTexCoord;\n" +
            "void main() {\n" +
            "  gl_Position = vPosition;\n" +
            "  outTexCoord = vTexCoord;\n" +
            "}";

        private final String fragmentShaderCode =
        	"#extension GL_OES_EGL_image_external : require \n"+
            "precision mediump float;\n" +
            "varying vec2 outTexCoord;\n" +
            "uniform samplerExternalOES s_texture;\n" +
            "void main() {\n" +
            "  vec3 color = texture2D(s_texture, outTexCoord).rgb;\n" +
            "  float grey = (color.r + color.g + color.b)/3.0;\n" +
            "  vec3 greyScale = vec3(grey);\n" +
            "  gl_FragColor = vec4(greyScale, 1.0);\n" +
            "}";
	
        private static float shapeCoords[] = { -1.0f,  1.0f, 0.0f,   // top left
            -1.0f, -1.0f, 0.0f,   // bottom left
            1.0f, -1.0f, 0.0f,   // bottom right
            1.0f,  1.0f, 0.0f }; // top right
        //90 degree rotated
        private static float textureCoords[] = { 0.0f,  0.0f,   // top left
            1.0f, 0.0f,   // bottom left
            1.0f, 1.0f,    // bottom right
            0.0f,  1.0f}; // top right
        
        private static short drawOrder[] = { 0, 1, 2, 0, 2, 3};
         
        private static final int COORDS_PER_VERTEX = 3;
        private static final int TEXTURE_COORS_PER_VERTEX = 2;
        
    	private final int mProgram;
    	private FloatBuffer mVertexBuffer;
    	private FloatBuffer mTexCoordBuffer;
    	private ShortBuffer mDrawListBuffer;
    	
    	private int mTexName = 0;
    	
    	
    private int compileShader(final int filterType){
    	int program;
    	
		int vertexShader = GLES20.glCreateShader(GLES20.GL_VERTEX_SHADER);
		int fragmentShader = GLES20.glCreateShader(GLES20.GL_FRAGMENT_SHADER);
		
		GLES20.glShaderSource(vertexShader, vertexShaderCode);
		GLES20.glShaderSource(fragmentShader, fragmentShaderCode);
		
		int[] compileStatus = new int[1]; 
		GLES20.glCompileShader(vertexShader);;
		GLES20.glGetShaderiv(vertexShader, GLES20.GL_COMPILE_STATUS, compileStatus, 0);
		if (compileStatus[0] == 0){
			String err = GLES20.glGetShaderInfoLog(vertexShader);
			Log.e("t_rtf_camera:gl",err);
		}
		GLES20.glCompileShader(fragmentShader);
		GLES20.glGetShaderiv(fragmentShader, GLES20.GL_COMPILE_STATUS, compileStatus, 0);
		if (compileStatus[0] == 0){
			String err = GLES20.glGetShaderInfoLog(fragmentShader);
			Log.e("t_rtf_camera:gl",err);
		}
		
		program = GLES20.glCreateProgram();
		GLES20.glAttachShader(program, vertexShader);
		GLES20.glAttachShader(program, fragmentShader);
		GLES20.glLinkProgram(program);
		
		return program;
    }
    	
	public GLCameraPreview(final int filterType){
		
		mProgram = compileShader(filterType);
		startPreview();

		/*Prepare buffer*/
		ByteBuffer bb = ByteBuffer.allocateDirect(4*shapeCoords.length);
		bb.order(ByteOrder.nativeOrder());
		
		mVertexBuffer = bb.asFloatBuffer();
		mVertexBuffer.put(shapeCoords);
		mVertexBuffer.position(0);
		
		ByteBuffer txeb = ByteBuffer.allocateDirect(4*textureCoords.length);
		txeb.order(ByteOrder.nativeOrder());
		
		mTexCoordBuffer = txeb.asFloatBuffer();
		mTexCoordBuffer.put(textureCoords);
		mTexCoordBuffer.position(0);
		
		ByteBuffer dlb = ByteBuffer.allocateDirect(drawOrder.length * 2);
		dlb.order(ByteOrder.nativeOrder());
		
		mDrawListBuffer = dlb.asShortBuffer();
		mDrawListBuffer.put(drawOrder);
		mDrawListBuffer.position(0);		
	}
	
	public void startPreview(){
		int textures[] = new int[1];
		GLES20.glGenTextures(1, textures, 0);
		
		GLPreviewActivity app = GLPreviewActivity.getAppInstance();
		
		app.startCamera(textures[0]);
		mTexName = textures[0];
	}
	
	public void draw(){
		
		GLES20.glUseProgram(mProgram);
		
		int positionHandler = GLES20.glGetAttribLocation(mProgram, "vPosition");
		int texCoordHandler = GLES20.glGetAttribLocation(mProgram, "vTexCoord");
		int textureHandler = GLES20.glGetUniformLocation(mProgram, "s_texture");		

		GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
		GLES20.glBindTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, mTexName);

		GLES20.glTexParameteri(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_LINEAR);
		GLES20.glTexParameteri(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR);
		
		GLES20.glTexParameteri(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, GLES20.GL_TEXTURE_WRAP_S, GLES20.GL_CLAMP_TO_EDGE);
		GLES20.glTexParameteri(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, GLES20.GL_TEXTURE_WRAP_T, GLES20.GL_CLAMP_TO_EDGE);
		
		GLES20.glEnableVertexAttribArray(positionHandler);
		
        GLES20.glVertexAttribPointer(positionHandler, COORDS_PER_VERTEX,
                GLES20.GL_FLOAT, false,
                COORDS_PER_VERTEX*4, mVertexBuffer);

        GLES20.glEnableVertexAttribArray(texCoordHandler);
        GLES20.glVertexAttribPointer(texCoordHandler, TEXTURE_COORS_PER_VERTEX,
                GLES20.GL_FLOAT, false,
                TEXTURE_COORS_PER_VERTEX*4, mTexCoordBuffer);
        
		//float color[] = { 0.63671875f, 0.76953125f, 0.22265625f, 1.0f };
		//GLES20.glUniform4fv(colorHandler, 1, color, 0);
        GLES20.glUniform1i(textureHandler, 0);
		
		GLES20.glDrawElements(GLES20.GL_TRIANGLES, drawOrder.length, GLES20.GL_UNSIGNED_SHORT, mDrawListBuffer);
	
		GLES20.glDisableVertexAttribArray(positionHandler);
		GLES20.glDisableVertexAttribArray(texCoordHandler);
	}
	

}