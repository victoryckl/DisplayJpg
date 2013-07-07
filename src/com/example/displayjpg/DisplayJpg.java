package com.example.displayjpg;

import java.io.File;

import android.R.bool;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

@SuppressLint("NewApi")
public class DisplayJpg extends Activity {
	private static final String TAG = "DisplayJpg";
	private Button mStart, mUp, mDown;
	private EditText mPath;
	private TextView mIndex, mSpeedText;
	private WebView mWebView;
	private int mCount = 0, mMs = 5;//fps
	private String urlPath = null;
	
	private Button mAudioStart;
	private EditText mAudioPath;
	private String mAudioUrl = null;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		Log.i(TAG, "onCreate()");
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_display_jpg);
		
		mStart = (Button)findViewById(R.id.start);
		mStart.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if (isPlaying()) {
					pause();
				} else {
					urlPath = getUrlPath();
					Log.i(TAG, urlPath);
					play();
				}
			}
		});
		mPath = (EditText)findViewById(R.id.edit);
		mIndex = (TextView)findViewById(R.id.index);
		mWebView = (WebView)findViewById(R.id.webview);
		settings(mWebView);
		
		mSpeedText = (TextView)findViewById(R.id.speed);
		findViewById(R.id.up).setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				mMs++;
				if (mMs > 500) {
					mMs = 500;
				}
				mSpeedText.setText("" + mMs + "fps");
			}
		});
		findViewById(R.id.down).setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				mMs--;
				if (mMs < 1) {
					mMs = 1;
				}
				mSpeedText.setText("" + mMs + "fps");
			}
		});
		
		mSpeedText.setText("" + mMs + "fps");
		
		
		findViewById(R.id.audio_start).setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
			}
		});
		mAudioPath = (EditText)findViewById(R.id.audio_edit);
	}

	private void settings(WebView webview) {
		WebSettings s = webview.getSettings();
		s.setJavaScriptEnabled(true);
	}
	
	private void loadHtml() {
		mWebView.loadDataWithBaseURL(null, getHtmlContent(urlPath, mCount), "text/html", "utf-8", null);
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.activity_display_jpg, menu);
		return true;
	}
	
	//--------------------------------
	private String getUrlPath() {
		urlPath = mPath.getText().toString();
		if (urlPath.isEmpty()) {
			Toast.makeText(getApplicationContext(), 
					"please input url path !-_-", Toast.LENGTH_LONG).show();
			return "";
		}
		if (!urlPath.endsWith(File.separator)) {
			urlPath = urlPath + File.separator;
		}
		return urlPath;
	}
	
	//--------------------------------
	private static final int MSG_PLAY = 0;
	private Handler mHandler = new Handler() {
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case MSG_PLAY:
				refreshImage();
				sendEmptyMessageDelayed(MSG_PLAY, 1000/mMs);
				break;
			default:
				break;
			}
		};
	};
	
	private boolean isPlaying() {
		return mHandler.hasMessages(MSG_PLAY);
	}
	
	private void play() {
		Log.i(TAG, "play");
		mHandler.sendEmptyMessageDelayed(MSG_PLAY, 100);
	}
	
	private void pause() {
		Log.i(TAG, "pause");
		mHandler.removeMessages(MSG_PLAY);
	}
	
	//----------------------------------
	private String getHtmlContent(String path, int index) {
		String html = "";
		/*
		<img id='imagecontain' width='100%' height='100%' src='image/0.jpg'/>
		<script type='text/javascript'>
		var nIndex = 0;
		window.setInterval(function(){
			url = 'file:///sdcard/image/' + nIndex + '.jpg';
			displayjpg.changeImage(url);
			nIndex++;
			if (nIndex > 27) {
				nIndex = 0;
			}
		},200);
		var displayjpg = {
			changeImage:function(url) {
				var obj = document.getElementById('imagecontain');
				obj.src = url;
			}
		};
		</script>
		*/
		
		html = ""
		+ "\n <img id='imagecontain' width='100%' height='100%' src='"+path+index+".jpg'/>"		
		+ "\n <script type='text/javascript'>"
		/*
		+ "\n var nIndex = 0;"
		+ "\n window.setInterval(function(){"
		+ "\n     url = 'file:///sdcard/image/' + nIndex + '.jpg'"
		+ "\n     displayjpg.changeImage(url);"
		+ "\n     nIndex++;"
		+ "\n     if (nIndex > 27) {"
		+ "\n         nIndex = 0;"
		+ "\n     }"
		+ "\n },1000);"
		/*/
		+ "\n var displayjpg = {"
		+ "\n     changeImage:function(url) {"
		+ "\n         var obj = document.getElementById('imagecontain');" 
		+ "\n         obj.src = url;"
		+ "\n     }"
		+ "\n }"
		+ "\n </script>";
		Log.i(TAG, html);
		return html;
	}
	
	private void refreshImage() {
		callJavaScript(mWebView, urlPath, mCount);
		mIndex.setText(""+ mCount + ".jpg");
		if (Build.VERSION.SDK_INT >= 11) {
			getActionBar().setTitle(""+urlPath+mCount+".jpg");
		}
		mCount++;
		if (mCount > 27) {
			mCount = 0;
		}
	}
	
	private void callJavaScript(WebView webview, String path, int index) {
		String cmd = "javascript:displayjpg.changeImage('"+path+index+".jpg')";
		Log.i(TAG, cmd);
		webview.loadUrl(cmd);
	}
	//----------------------------------
	protected void onStart() {
		Log.i(TAG, "onStart()");
		super.onStart();
	};
	protected void onResume() {
		Log.i(TAG, "onResume()");
		super.onResume();
		
		urlPath = getUrlPath();
		Log.i(TAG, urlPath);
		loadHtml();
	};
	@Override
	protected void onPause() {
		Log.i(TAG, "onPause()");
		mHandler.removeMessages(0);
		super.onPause();
	}
	@Override
	protected void onStop() {
		Log.i(TAG, "onStop()");
		super.onStop();
	}
	@Override
	protected void onDestroy() {
		Log.i(TAG, "onDestroy()");
		super.onDestroy();
	}
}
