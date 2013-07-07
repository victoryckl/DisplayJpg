package com.example.audio;

import android.media.AudioManager;
import android.media.AudioTrack;
import android.os.Handler;
import android.os.Message;
import android.provider.ContactsContract.Contacts.Data;
import android.util.Log;

public class AudioPlayer implements IPlayComplete {

	private final static String TAG = "AudioPlayer";

	public final static int STATE_MSG_ID = 0x0010;

	private Handler mHandler;

	private AudioParam mAudioParam; // ��Ƶ����

	private IPcmData mPcmData;

	private AudioTrack mAudioTrack; // AudioTrack����

	private boolean mBReady = false; // ����Դ�Ƿ����

	private PlayAudioThread mPlayAudioThread; // �����߳�

	public AudioPlayer(Handler handler) {
		mHandler = handler;
	}

	public AudioPlayer(Handler handler, AudioParam audioParam) {
		mHandler = handler;
		setAudioParam(audioParam);
	}

	/*
	 * ������Ƶ����
	 */
	public void setAudioParam(AudioParam audioParam) {
		mAudioParam = audioParam;
	}

	public void setPcmDate(IPcmData pcmData) {
		mPcmData = pcmData;
	}
	
	public IPcmData getPcmDate() {
		return mPcmData;
	}

	/*
	 * ��������Դ
	 */
	public boolean prepare() {
		if (mPcmData == null || mAudioParam == null) {
			return false;
		}
		if (!mPcmData.isAvailable()) {
			return false;
		}
		if (mBReady == true) {
			return true;
		}
		try {
			createAudioTrack();
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
		mBReady = true;
		setPlayState(PlayState.MPS_PREPARE);
		return true;
	}

	/*
	 * �ͷŲ���Դ
	 */
	public boolean release() {
		stop();
		releaseAudioTrack();
		mBReady = false;
		setPlayState(PlayState.MPS_UNINIT);
		return true;
	}

	/*
	 * ����
	 */
	public boolean play() {
		if (mBReady == false) {
			return false;
		}
		switch (mPlayState) {
		case PlayState.MPS_PREPARE:
			setPlayState(PlayState.MPS_PLAYING);
			startThread();
			break;
		case PlayState.MPS_PAUSE:
			setPlayState(PlayState.MPS_PLAYING);
			startThread();
			break;
		}
		return true;
	}

	/*
	 * ��ͣ
	 */
	public boolean pause() {
		if (mBReady == false) {
			return false;
		}
		if (mPlayState == PlayState.MPS_PLAYING) {
			setPlayState(PlayState.MPS_PAUSE);
			stopThread();
		}
		return true;
	}

	/*
	 * ֹͣ
	 */
	public boolean stop() {

		if (mBReady == false) {
			return false;
		}

		setPlayState(PlayState.MPS_PREPARE);
		stopThread();
		mAudioTrack.stop();
		destroyPcm();
		return true;
	}

	private synchronized void setPlayState(int state) {
		mPlayState = state;

		if (mHandler != null) {
			Message msg = mHandler.obtainMessage(STATE_MSG_ID);
			msg.obj = mPlayState;
			msg.sendToTarget();
		}
	}

	private void createAudioTrack() throws Exception {

		// ��ù����������С��������С
		int minBufSize = AudioTrack.getMinBufferSize(mAudioParam.mFrequency,
				mAudioParam.mChannel, mAudioParam.mSampBit);

		mPrimePlaySize = minBufSize * 2;
		Log.d(TAG, "mPrimePlaySize = " + mPrimePlaySize);

		// STREAM_ALARM��������
		// STREAM_MUSCI��������������music��
		// STREAM_RING������
		// STREAM_SYSTEM��ϵͳ����
		// STREAM_VOCIE_CALL���绰����
		mAudioTrack = new AudioTrack(AudioManager.STREAM_MUSIC,
				mAudioParam.mFrequency, mAudioParam.mChannel,
				mAudioParam.mSampBit, minBufSize, AudioTrack.MODE_STREAM);
		// AudioTrack����MODE_STATIC��MODE_STREAM���ַ��ࡣ
		// STREAM����˼�����û���Ӧ�ó���ͨ��write��ʽ������һ��һ�ε�д��audiotrack�С�
		// �����������socket�з�������һ����Ӧ�ò��ĳ���ط���ȡ���ݣ�����ͨ�������õ�PCM���ݣ�Ȼ��write��audiotrack��
		// ���ַ�ʽ�Ļ�������������JAVA���Native�㽻����Ч����ʧ�ϴ�
		// ��STATIC����˼��һ��ʼ������ʱ�򣬾Ͱ���Ƶ���ݷŵ�һ���̶���buffer��Ȼ��ֱ�Ӵ���audiotrack��
		// �����Ͳ���һ�δε�write�ˡ�AudioTrack���Լ��������buffer�е����ݡ�
		// ���ַ��������������ڴ�ռ�ý�С����ʱҪ��ϸߵ�������˵�����á�
	}

	private void releaseAudioTrack() {
		if (mAudioTrack != null) {
			mAudioTrack.stop();
			mAudioTrack.release();
			mAudioTrack = null;
		}
	}

	private void startThread() {
		if (mPlayAudioThread == null) {
			mThreadExitFlag = false;
			mPlayAudioThread = new PlayAudioThread();
			mPlayAudioThread.start();
		}
	}

	private void stopThread() {
		if (mPlayAudioThread != null) {
			mThreadExitFlag = true;
			mPlayAudioThread = null;
		}
	}
	
	private void destroyPcm() {
		if (mPcmData != null) {
			mPcmData.destroy();
			mPcmData = null;
		}
	}

	private boolean mThreadExitFlag = false; // �߳��˳���־
	private int mPrimePlaySize = 0; // ���Ų��ſ��С
	private int mPlayState = 0; // ��ǰ����״̬

	/*
	 * ������Ƶ���߳�
	 */
	class PlayAudioThread extends Thread {

		@Override
		public void run() {
			byte[] buffer = new byte[mPrimePlaySize];
			int readbytes = -1;

			mAudioTrack.play();
			Log.i(TAG, "PlayAudioThread() mThreadExitFlag = " + mThreadExitFlag);
			while (true) {
				if (mThreadExitFlag == true) {
					break;
				}
				try {
					readbytes = mPcmData.read(buffer, 0, mPrimePlaySize);
					if (readbytes > 0) {
						mAudioTrack.write(buffer, 0, readbytes);
					}
				} catch (Exception e) {
					e.printStackTrace();
					AudioPlayer.this.onPlayComplete();
					break;
				}
				if (readbytes <= 0) {
					Log.i(TAG, "readbytes = " + readbytes);
					AudioPlayer.this.onPlayComplete();
					break;
				}
			}
			mAudioTrack.stop();
			Log.d(TAG, "PlayAudioThread complete...");
		}
	}

	@Override
	public void onPlayComplete() {
		// TODO Auto-generated method stub
		mPlayAudioThread = null;
		destroyPcm();
		if (mPlayState != PlayState.MPS_PAUSE) {
			setPlayState(PlayState.MPS_PREPARE);
		}
	}
}
