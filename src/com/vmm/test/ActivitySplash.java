package com.vmm.test;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;

public class ActivitySplash extends Activity {

	@Override
	public void onCreate(Bundle savedInstanceState) {
	
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_splash_layout);

		Handler handler = new Handler();

		handler.postDelayed(new Runnable() {

			@Override
			public void run() {

				Intent intent = new Intent(ActivitySplash.this,
						MainActivity.class);
				intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
				startActivity(intent);

			}
		}, 4000);
	}

}
