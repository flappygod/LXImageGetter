package com.flappygo.lilin.limagegetter.download;

import com.flappygo.lilin.limagegetter.tools.DirTool;

import android.os.Handler;
import android.os.Message;


/*******
 *
 * Package Name:com.flappygo.lipo.limagegetter.downloader <br/>
 * ClassName: ClearCacheThread <br/> 
 * Function: 清理缓存的线程  <br/> 
 * date: 2016-3-10 上午10:47:01 <br/> 
 * 
 * @author lijunlin
 */
public class ClearCacheThread extends Thread{
	
	//清理的地址
	private String path;
	//清理接受的的handler
	private Handler handler;
	
	public ClearCacheThread(String path,Handler handler){
		this.path=path;
		this.handler=handler;
	}

	public void run() {
		try {
			DirTool.deleteDirFiles(path, false);
			handler.sendEmptyMessage(1);
		} catch (Exception e) {
			Message msg=handler.obtainMessage(0,e);
			handler.sendMessage(msg);
		}
	}

}
