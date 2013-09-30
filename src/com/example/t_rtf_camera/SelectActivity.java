package com.example.t_rtf_camera;

import android.os.Bundle;
import android.app.Activity;
import android.content.Intent;
import android.view.Menu;
import android.view.View;

public class SelectActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_select);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.activity_select, menu);
		return true;
	}

	public void startRenderScript(View view){
		Intent intent = new Intent(this, PreviewActivity.class);
		startActivity(intent);
	}
	
	public void startOpenGL_ES(View view){
		Intent intent = new Intent(this, GLPreviewActivity.class);
		startActivity(intent);
	}
	
	public void startNDK(View view){
		Intent intent = new Intent(this, NativeProcessActivity.class);
		startActivity(intent);
	}
}
