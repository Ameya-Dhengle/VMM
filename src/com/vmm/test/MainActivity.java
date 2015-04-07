package com.vmm.test;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
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
import android.util.Log;
import android.view.Menu;
import android.webkit.ClientCertRequest;
import android.webkit.HttpAuthHandler;
import android.webkit.SslErrorHandler;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebSettings.RenderPriority;
import android.webkit.WebView;
import android.webkit.WebViewClient;

public class MainActivity extends Activity {

	private WebView mTestWebView;
	private long enqueue;
	private DownloadManager dm;
	ProgressDialog progressDialog;

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
		// web.getSettings().setSupportMultipleWindows(true);
		mTestWebView.getSettings().setBuiltInZoomControls(true);
		mTestWebView.getSettings().setSupportZoom(true);
		mTestWebView.getSettings().setLightTouchEnabled(true);
		mTestWebView.getSettings().setDomStorageEnabled(true);
		mTestWebView.getSettings().setPluginState(WebSettings.PluginState.ON);
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

		// web.getSettings().setPluginsEnabled(true);
		/*
		 * File f = new File(
		 * "file:///storage/emulated/0/Android/data/com.example.test/files/Download/rbmDownloads.pdf"
		 * ); Intent fileIntent = new Intent(Intent.ACTION_VIEW);
		 * fileIntent.setDataAndType(Uri.fromFile(f), "application/pdf");
		 * 
		 * fileIntent .setFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
		 * startActivity(fileIntent);
		 */
		// mTestWebView.getSettings().setLoadWithOverviewMode(overviewmode);
		// mTestWebView.getSettings().setUseWideViewPort(viewport);

		// these settings speed up page load into the webview
		mTestWebView.getSettings().setRenderPriority(RenderPriority.HIGH);
		// web.getSettings().setCacheMode(WebSettings.LOAD_NO_CACHE);
		mTestWebView.setWebViewClient(new TestWebViewClient());

		BroadcastReceiver receiver = new BroadcastReceiver() {
			@Override
			public void onReceive(Context context, Intent intent) {
				String action = intent.getAction();
				if (DownloadManager.ACTION_DOWNLOAD_COMPLETE.equals(action)) {
					long downloadId = intent.getLongExtra(
							DownloadManager.EXTRA_DOWNLOAD_ID, 0);
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
							Log.i("Vaibhavs", "Actual: " + uriString);
							// uriString=Environment.getExternalStorageDirectory().getAbsolutePath()+"/Download/"+uriString;

							// uriString=uriString.substring(7);

							Log.i("Vaibhavs", "HardCoded:" + uriString);

							File f = new File(uriString);
							Log.i("Vaibhavs", "file exists: " + f.exists());
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
				HttpAuthHandler handler, String host, String realm) {

			String[] credentials = view.getHttpAuthUsernamePassword(host, realm);
		    if (credentials != null) {
		        Log.d("Ameya", "Setting credentials for user : " + credentials[0]);
		        handler.proceed(credentials[0], credentials[1]);
		    } else {
		        Log.d("Ameya", "No credentials found");
		        view.setHttpAuthUsernamePassword(host, realm, "a929261", "Bestbuy8");
		    }
			super.onReceivedHttpAuthRequest(view, handler, host, realm);
		}
		
//		@Override
//		public void onReceivedClientCertRequest(WebView view,
//				ClientCertRequest request) {
//
//			try {
//
//				byte der[] = readFully(
//						MainActivity.this.getResources().openRawResource(
//								R.raw.vmm_certificate)).getBytes();
//
//				ByteArrayInputStream derInputStream = new ByteArrayInputStream(
//						der);
//				CertificateFactory certificateFactory = CertificateFactory
//						.getInstance("X.509");
//				X509Certificate[] certificates = new X509Certificate[1];
//
//				certificates[0] = (X509Certificate) certificateFactory
//						.generateCertificate(derInputStream);
//				PrivateKey key = getPrivateKey(1, MainActivity.this);
//				request.proceed(key, certificates);
//
//			} catch (Exception e) {
//			}
//		}

		@Override
		public void onPageFinished(WebView view, String url) {
			Log.i("Vaibhavs", "onPageFinished: " + url);

			progressDialog.dismiss();
			super.onPageFinished(view, url);
		}

		@Override
		public boolean shouldOverrideUrlLoading(WebView view, String url) {

			Log.i("Vaibhavs", "shouldOverridingURl: " + url);
			return super.shouldOverrideUrlLoading(view, url);

		}

		@Override
		public void onPageStarted(WebView view, String url, Bitmap favicon) {
			progressDialog = new ProgressDialog(MainActivity.this);
			progressDialog.setMessage("Loading...");
			progressDialog.show();

//			if (url.contains(".pdf") || url.contains(".PDF")) {
//				dm = (DownloadManager) getSystemService(DOWNLOAD_SERVICE);
//				Request request = new Request(Uri.parse(url));
//				request.setDestinationInExternalPublicDir(
//						Environment.DIRECTORY_DOWNLOADS, "newsample.pdf");
//				enqueue = dm.enqueue(request);
//			}
//			Log.i("Vaibhavs", "onPageStarted: "
//					+ Environment.getExternalStorageDirectory()
//							.getAbsolutePath());

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

}
