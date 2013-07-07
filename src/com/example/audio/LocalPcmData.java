package com.example.audio;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

public class LocalPcmData implements IPcmData {
	String mPath;
	FileInputStream mIStream;
	
	public LocalPcmData(String path) {
		mPath = path;
		try {
			mIStream = new FileInputStream(mPath);
		} catch (FileNotFoundException e) {
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
		if (mIStream != null) {
			try {
				readbytes = mIStream.read(buffer, byteOffset, byteCount);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return readbytes;
	}

	@Override
	public void destroy() {
		try {
			if (mIStream != null) {
				mIStream.close();
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
