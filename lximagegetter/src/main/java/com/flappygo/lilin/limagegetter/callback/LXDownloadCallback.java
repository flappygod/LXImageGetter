package com.flappygo.lilin.limagegetter.callback;

import android.graphics.Bitmap;

/**************
 *
 * Package Name:com.flappygo.lipo.limagegetter.callback <br/>
 * ClassName: LXDownloadCallback <br/>
 * Function: 图片下载的回调函数 <br/>
 * date: 2016-3-9 下午2:08:31 <br/>
 * 
 * @author lijunlin
 */
public interface LXDownloadCallback {

	/*********
	 * 图片下载完成
	 * 
	 * @param urlStr
	 *            下载的网络地址
	 * @param pathStr
	 *            下载后的SD卡地址
	 * @param bitmap
	 *            下载成功后的图片
	 */
	void downLoadReady(String urlStr, String pathStr, Bitmap bitmap);

	/************
	 * 下载出错
	 * 
	 * @param e
	 */
	void downLoadError(Exception e);

	/*************
	 * 下载中的回调
	 * 
	 * @param progress
	 *            下载进度
	 */
	void downLoading(int progress);
	
	
	/************
	 * 下载被取消
	 */
	void downLoadingCanceled();

}
