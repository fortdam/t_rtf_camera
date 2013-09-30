package com.example.t_rtf_camera;

import android.os.Bundle;
import android.app.Activity;
import android.view.Menu;
import android.view.WindowManager;

public class NativeProcessActivity extends Activity {

	public String test;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_native_process);
		
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
		
		test = testJNI();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.activity_native_process, menu);
		return true;
	}

	static {
		System.loadLibrary("image_process");
	}
	
	public native String testJNI();
}
