package com.example.audio;

public interface IPcmData {
	boolean isAvailable();
	int read(byte[] buffer, int byteOffset, int byteCount);
	void destroy();
}
