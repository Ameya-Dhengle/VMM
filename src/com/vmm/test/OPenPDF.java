package com.vmm.test;

import java.io.File;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;

public class OPenPDF extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		//setContentView(R.layout.activity_main);
	
		
		String uriString=Environment.getExternalStorageDirectory().getAbsolutePath()+"/Download/vaibhav.pdf";

		
		File f = new File(uriString);
		Log.i("Vaibhavs", "file exists: "+f.exists());
		if(f.exists())
		{
		Intent fileIntent = new Intent(Intent.ACTION_VIEW);
		fileIntent.setDataAndType(Uri.fromFile(f),
				"application/pdf");

			fileIntent
		.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
		startActivity(fileIntent);
	
	}
	}
}
