package com.example.audio;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.media.AudioFormat;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.os.StrictMode;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.example.displayjpg.R;

@SuppressLint("NewApi")
public class AudioPlayerDemoActivity extends Activity implements
		OnClickListener {

	private TextView mTextViewState; // 播放状态

	private Button mBtnPlayButton; // 播放

	private Button mBtnPauseButton; // 暂停

	private Button mBtnStopButton; // 停止

	private AudioPlayer mAudioPlayer; // 播放器

	private Handler mHandler;
	
	private EditText mEditText;
	
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);

		initView();

		// initLogic();
		new Handler() {
			@Override
			public void handleMessage(Message msg) {
				initLogic();
			}
		}.sendEmptyMessageDelayed(0, 200);
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		mAudioPlayer.release();
	}

	public void initView() {
		mTextViewState = (TextView) findViewById(R.id.tvPlayState);

		mBtnPlayButton = (Button) findViewById(R.id.buttonPlay);
		mBtnPlayButton.setOnClickListener(this);

		mBtnPauseButton = (Button) findViewById(R.id.buttonPause);
		mBtnPauseButton.setOnClickListener(this);

		mBtnStopButton = (Button) findViewById(R.id.buttonStop);
		mBtnStopButton.setOnClickListener(this);
		
		mEditText = (EditText)findViewById(R.id.edit);
	}

	public void initLogic() {
		if (Build.VERSION.SDK_INT > 10) {
			StrictMode.setThreadPolicy(new StrictMode.ThreadPolicy.Builder()
					.detectDiskReads().detectDiskWrites().detectNetwork() // 这里可以替换为detectAll()
																			// 就包括了磁盘读写和网络I/O
					.penaltyLog() // 打印logcat，当然也可以定位到dropbox，通过文件保存相应的log
					.build());
			StrictMode.setVmPolicy(new StrictMode.VmPolicy.Builder()
					.detectLeakedSqlLiteObjects() // 探测SQLite数据库操作
					.penaltyLog() // 打印logcat
					.penaltyDeath().build());
		}
		
		mHandler = new Handler() {
			@Override
			public void handleMessage(Message msg) {
				switch (msg.what) {
				case AudioPlayer.STATE_MSG_ID:
					showState((Integer) msg.obj);
					break;
				}
			}
		};

		mAudioPlayer = new AudioPlayer(mHandler);

		// 获取音频参数
		AudioParam audioParam = getAudioParam();
		mAudioPlayer.setAudioParam(audioParam);

		// LocalPcmData lPcmData = new LocalPcmData(filePath);
		HttpPcmDate lPcmData = new HttpPcmDate(mEditText.getText().toString());
		mAudioPlayer.setPcmDate(lPcmData);

		// 音频源就绪
		mAudioPlayer.prepare();

		if (!lPcmData.isAvailable()) {
			mTextViewState.setText(filePath + "：该路径下不存在文件！");
		}
	}

	@Override
	public void onClick(View view) {
		switch (view.getId()) {
		case R.id.buttonPlay:
//			play();
			new Handler() {
				@Override
				public void handleMessage(Message msg) {
					play();
				}
			}.sendEmptyMessageDelayed(0, 10);
			break;
		case R.id.buttonPause:
			pause();
			break;
		case R.id.buttonStop:
			stop();
			break;
		}
	}

	public void play() {
		initPcmDate();
		mAudioPlayer.play();
	}

	public void pause() {
		mAudioPlayer.pause();
	}

	public void stop() {
		mAudioPlayer.stop();
		mLastUrl = null;
	}
	
	private String mLastUrl;
	private void initPcmDate() {
		String url = mEditText.getText().toString();
		if (mLastUrl == null || !mLastUrl.equals(url) || mAudioPlayer.getPcmDate() == null) {
			mAudioPlayer.stop();
			
			mLastUrl = url;
			Toast.makeText(getApplicationContext(), url, Toast.LENGTH_LONG).show();
			
			HttpPcmDate lPcmData = new HttpPcmDate(url);
			mAudioPlayer.setPcmDate(lPcmData);
			mAudioPlayer.prepare();
		} else {
			//do nothing
		}
	}

	public void showState(int state) {
		String showString = "";

		switch (state) {
		case PlayState.MPS_UNINIT:
			showString = "MPS_UNINIT";
			break;
		case PlayState.MPS_PREPARE:
			showString = "MPS_PREPARE";
			break;
		case PlayState.MPS_PLAYING:
			showString = "MPS_PLAYING";
			break;
		case PlayState.MPS_PAUSE:
			showString = "MPS_PAUSE";
			break;
		}

		showState(showString);
	}

	public void showState(String str) {
		mTextViewState.setText(str);
	}

	/*
	 * 获得PCM音频数据参数
	 */
	public AudioParam getAudioParam() {
		AudioParam audioParam = new AudioParam();
		audioParam.mFrequency = 44100;
		audioParam.mChannel = AudioFormat.CHANNEL_CONFIGURATION_STEREO;
		audioParam.mSampBit = AudioFormat.ENCODING_PCM_16BIT;

		return audioParam;
	}

	String filePath = Environment.getExternalStorageDirectory()
			.getAbsolutePath() + "/a.pcm";

	/*
	 * 获得PCM音频数据
	 */
	public byte[] getPCMData() {
		File file = new File(filePath);
		if (file == null) {
			return null;
		}

		FileInputStream inStream;
		try {
			inStream = new FileInputStream(file);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			return null;
		}

		byte[] data_pack = null;
		if (inStream != null) {
			long size = file.length();

			data_pack = new byte[(int) size];
			try {
				inStream.read(data_pack);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				return null;
			}

		}

		return data_pack;
	}

}