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

	private TextView mTextViewState; // ����״̬

	private Button mBtnPlayButton; // ����

	private Button mBtnPauseButton; // ��ͣ

	private Button mBtnStopButton; // ֹͣ

	private AudioPlayer mAudioPlayer; // ������

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
					.detectDiskReads().detectDiskWrites().detectNetwork() // ��������滻ΪdetectAll()
																			// �Ͱ����˴��̶�д������I/O
					.penaltyLog() // ��ӡlogcat����ȻҲ���Զ�λ��dropbox��ͨ���ļ�������Ӧ��log
					.build());
			StrictMode.setVmPolicy(new StrictMode.VmPolicy.Builder()
					.detectLeakedSqlLiteObjects() // ̽��SQLite���ݿ����
					.penaltyLog() // ��ӡlogcat
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

		// ��ȡ��Ƶ����
		AudioParam audioParam = getAudioParam();
		mAudioPlayer.setAudioParam(audioParam);

		// LocalPcmData lPcmData = new LocalPcmData(filePath);
		HttpPcmDate lPcmData = new HttpPcmDate(mEditText.getText().toString());
		mAudioPlayer.setPcmDate(lPcmData);

		// ��ƵԴ����
		mAudioPlayer.prepare();

		if (!lPcmData.isAvailable()) {
			mTextViewState.setText(filePath + "����·���²������ļ���");
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
	 * ���PCM��Ƶ���ݲ���
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
	 * ���PCM��Ƶ����
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