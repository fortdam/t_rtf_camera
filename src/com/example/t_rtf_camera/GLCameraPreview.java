package com.example.t_rtf_camera;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.ShortBuffer;

import android.content.Context;
import android.opengl.GLES11Ext;
import android.opengl.GLES20;
import android.util.Log;

class  GLCameraPreview {
	public static final int FILTER_NONE = 0;
	public static final int FILTER_GREY = 1;
	public static final int FILTER_SEPIA_TONE = 2;
	public static final int FILTER_NEGATIVE_COLOR = 3;
	public static final int FILTER_VIGNETTE = 4;
	public static final int FILTER_FISHEYE = 5;
	
    private static float shapeCoords[] = { -0.5f,  0.3f, 0.0f,   // top left
        -0.5f, -0.3f, 0.0f,   // bottom left
        0.5f, -0.3f, 0.0f,   // bottom right
        0.5f,  0.3f, 0.0f }; // top right
    //90 degree rotated
    private static float textureCoords[] = { 0.0f,  1.0f,   // top left
        1.0f, 1.0f,   // bottom left
        1.0f, 0.0f,    // bottom right
        0.0f,  0.0f}; // top right
        
    private static short drawOrder[] = { 0, 1, 2, 0, 2, 3};
         
    private static final int COORDS_PER_VERTEX = 3;
    private static final int TEXTURE_COORS_PER_VERTEX = 2;
        
    private final int mProgram;
    private FloatBuffer mVertexBuffer;
    private FloatBuffer mTexCoordBuffer;
    private ShortBuffer mDrawListBuffer;
    	
    private int mTexName = 0;
    	
    private static String readRawTextFile(Context context, int resId){
        InputStream inputStream = context.getResources().openRawResource(resId);
            
        InputStreamReader inputreader = new InputStreamReader(inputStream);
        BufferedReader buffreader = new BufferedReader(inputreader);
        String line;
        StringBuilder text = new StringBuilder();
            
        try {
            while (( line = buffreader.readLine()) != null) {
                text.append(line);
                text.append('\n');
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return text.toString();
    }	
    	
    private int compileShader(final int filterType){
    	int program;
    	GLPreviewActivity app = GLPreviewActivity.getAppInstance();
    	
		int vertexShader = GLES20.glCreateShader(GLES20.GL_VERTEX_SHADER);
		int fragmentShader = GLES20.glCreateShader(GLES20.GL_FRAGMENT_SHADER);
		
		String vertexShaderCode = readRawTextFile(app, R.raw.vertex);
		String fragmentShaderCode = readRawTextFile(app, R.raw.fragment_fish_eye);
		
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
		
		int positionHandler = GLES20.glGetAttribLocation(mProgram, "aPosition");
		int texCoordHandler = GLES20.glGetAttribLocation(mProgram, "aTextureCoord");
		int textureHandler = GLES20.glGetUniformLocation(mProgram, "sTexture");		

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
