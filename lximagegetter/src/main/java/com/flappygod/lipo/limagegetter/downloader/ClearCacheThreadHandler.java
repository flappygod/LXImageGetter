package com.flappygod.lipo.limagegetter.downloader;

import com.flappygod.lipo.limagegetter.callback.ClearCacheCallback;

import android.os.Handler;
import android.os.Message;

public class ClearCacheThreadHandler extends Handler {

	//清理回调函数
	private ClearCacheCallback callback;

	public ClearCacheThreadHandler(ClearCacheCallback callback) {
		this.callback = callback;
	}

	// 接收到消息
	public void handleMessage(Message msg) {
		if (msg.what == 1) {
			if (callback != null)
				callback.done();
		} else {
			if (callback != null)
				callback.failed((Exception) msg.obj);
		}
	}

}
