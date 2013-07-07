package com.example.audio;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import android.nfc.tech.MifareClassic;
import android.util.Log;

public class HttpPcmDate implements IPcmData {
	private static final String TAG = "HttpPcmDate";
	HttpURLConnection mConnection;
	InputStream mIStream;
	
	public HttpPcmDate(String url) {
		try {
			URL mUrl = new URL(url);
			mConnection = (HttpURLConnection)mUrl.openConnection();
			int code = mConnection.getResponseCode();
			if (HttpURLConnection.HTTP_OK == code) {
				mConnection.connect();
				mIStream = mConnection.getInputStream();
			}
		} catch (MalformedURLException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	@Override
	public boolean isAvailable() {
		try {
			if (mIStream != null) {
				return mIStream.available() > 0;
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		return false;
	}
	
	@Override
	public int read(byte[] buffer, int byteOffset, int byteCount) {
		int readbytes = -1;
		try {
			if (mIStream != null) {
				readbytes = mIStream.read(buffer, byteOffset, byteCount);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		return readbytes;
	}
	
	@Override
	public void destroy() {
		try {
			if (mIStream != null) {
				mIStream.close();
				mIStream = null;
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		if (mConnection != null) {
			mConnection.disconnect();
			mConnection = null;
		}
	}
}
