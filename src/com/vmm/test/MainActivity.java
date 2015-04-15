package com.vmm.test;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.spec.PKCS8EncodedKeySpec;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.DownloadManager;
import android.app.DownloadManager.Query;
import android.app.DownloadManager.Request;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.net.http.SslError;
import android.os.Bundle;
import android.os.Environment;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.webkit.HttpAuthHandler;
import android.webkit.SslErrorHandler;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.EditText;

public class MainActivity extends Activity {

	private WebView mTestWebView;
	private long enqueue;
	private DownloadManager dm;
	ProgressDialog progressDialog;
	Context context = this;

	String username = null;
	String password = null;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		mTestWebView = (WebView) findViewById(R.id.webViewTest);
		mTestWebView.getSettings().setJavaScriptEnabled(true);
		mTestWebView.loadUrl("https://bby-demo.rbmfrontline.com/mobile");
		mTestWebView.setClickable(true);
		mTestWebView.setFocusableInTouchMode(true);
		mTestWebView.getSettings().setJavaScriptEnabled(true);
		mTestWebView.getSettings().setJavaScriptCanOpenWindowsAutomatically(
				true);
		mTestWebView.getSettings().setBuiltInZoomControls(true);
		mTestWebView.getSettings().setSupportZoom(true);
		mTestWebView.getSettings().setDomStorageEnabled(true);
		WebChromeClient webChromeClient = new WebChromeClient();
		mTestWebView.setWebChromeClient(webChromeClient);
		mTestWebView.setWebChromeClient(new WebChromeClient() {
			@Override
			public boolean onJsAlert(WebView view, String url, String message,
					final android.webkit.JsResult result) {
				new AlertDialog.Builder(view.getContext())
						.setTitle("ALert")
						.setMessage(message)
						.setPositiveButton(android.R.string.ok,
								new AlertDialog.OnClickListener() {
									public void onClick(DialogInterface dialog,
											int which) {
										result.confirm();
									}
								}).setCancelable(false).create().show();

				return true;
			};
		});

		// these settings speed up page load into the webview
		mTestWebView.setWebViewClient(new TestWebViewClient());

		BroadcastReceiver receiver = new BroadcastReceiver() {
			@Override
			public void onReceive(Context context, Intent intent) {
				String action = intent.getAction();
				if (DownloadManager.ACTION_DOWNLOAD_COMPLETE.equals(action)) {
					Query query = new Query();
					query.setFilterById(enqueue);
					Cursor c = dm.query(query);
					if (c.moveToFirst()) {
						int columnIndex = c
								.getColumnIndex(DownloadManager.COLUMN_STATUS);
						if (DownloadManager.STATUS_SUCCESSFUL == c
								.getInt(columnIndex)) {

							String uriString = c
									.getString(c
											.getColumnIndex(DownloadManager.COLUMN_LOCAL_FILENAME));
							File f = new File(uriString);
							if (f.exists()) {
								Intent fileIntent = new Intent(
										Intent.ACTION_VIEW);
								fileIntent.setDataAndType(Uri.fromFile(f),
										"application/pdf");

								fileIntent
										.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
								startActivity(fileIntent);
							}
						}
					}
				}
			}
		};

		registerReceiver(receiver, new IntentFilter(
				DownloadManager.ACTION_DOWNLOAD_COMPLETE));
	}

	public static String readFully(InputStream inputStream) throws IOException {

		if (inputStream == null) {
			return "";
		}

		BufferedInputStream bufferedInputStream = null;
		ByteArrayOutputStream byteArrayOutputStream = null;

		try {
			bufferedInputStream = new BufferedInputStream(inputStream);
			byteArrayOutputStream = new ByteArrayOutputStream();

			final byte[] buffer = new byte[1024];
			int available = 0;

			while ((available = bufferedInputStream.read(buffer)) >= 0) {
				byteArrayOutputStream.write(buffer, 0, available);
			}

			return byteArrayOutputStream.toString();

		} finally {
			if (bufferedInputStream != null) {
				bufferedInputStream.close();
			}
		}
	}

	public class TestWebViewClient extends WebViewClient {

		@Override
		public void onReceivedSslError(WebView view, SslErrorHandler handler,
				SslError error) {
			handler.proceed();
		}

		@Override
		public void onReceivedHttpAuthRequest(WebView view,
				final HttpAuthHandler handler, String host, String realm) {

			if (username != null && password != null) {

				handler.proceed(username, password);
			} else {

				LayoutInflater li = LayoutInflater.from(context);
				View promptsView = li.inflate(R.layout.layout_login_prompt,
						null);

				AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(
						context);

				// set prompts.xml to alertdialog builder
				alertDialogBuilder.setView(promptsView);

				final EditText edittext_username = (EditText) promptsView
						.findViewById(R.id.edittext_username);

				final EditText edittext_password = (EditText) promptsView
						.findViewById(R.id.edittext_password);

				alertDialogBuilder
						.setCancelable(false)
						.setPositiveButton("OK",
								new DialogInterface.OnClickListener() {
									public void onClick(DialogInterface dialog,
											int id) {
										username = edittext_username.getText()
												.toString();
										password = edittext_password.getText()
												.toString();
										handler.cancel();
										mTestWebView.goBack();

									}
								})
						.setNegativeButton("Cancel",
								new DialogInterface.OnClickListener() {
									public void onClick(DialogInterface dialog,
											int id) {
										dialog.cancel();
									}
								});

				// create alert dialog
				AlertDialog alertDialog = alertDialogBuilder.create();

				// show it
				alertDialog.show();
			}
		}

		@Override
		public void onPageFinished(WebView view, String url) {

			progressDialog.dismiss();
			super.onPageFinished(view, url);
		}

		@Override
		public boolean shouldOverrideUrlLoading(WebView view, String url) {

			if (url.contains(".pdf") || url.contains(".PDF")) {
				dm = (DownloadManager) getSystemService(DOWNLOAD_SERVICE);
				Request request = new Request(
						Uri.parse("http://www.stat.berkeley.edu/~census/sample.pdf"));
				request.setDestinationInExternalPublicDir(
						Environment.DIRECTORY_DOWNLOADS, "newsample.pdf");
				enqueue = dm.enqueue(request);

				return false;
			}

			return super.shouldOverrideUrlLoading(view, url);

		}

		@Override
		public void onPageStarted(WebView view, String url, Bitmap favicon) {
			progressDialog = new ProgressDialog(MainActivity.this);
			progressDialog.setMessage("Loading...");
			progressDialog.show();
			super.onPageStarted(view, url, favicon);
		}
	}

	public PrivateKey getPrivateKey(int key, Context context) {

		PrivateKey privateKey = null;

		try {
			String privateString = "testkey";
			if (privateString != null) {
				byte[] binCpk = Base64.decode(privateString, Base64.DEFAULT);
				KeyFactory keyFactory = KeyFactory.getInstance("RSA");
				PKCS8EncodedKeySpec privateKeySpec = new PKCS8EncodedKeySpec(
						binCpk);
				privateKey = keyFactory.generatePrivate(privateKeySpec);
			}
		} catch (Exception e) {
		}
		return privateKey;
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public void onBackPressed() {

		if (this.mTestWebView.canGoBack())
			this.mTestWebView.goBack();
		else
			super.onBackPressed();
	}

}
