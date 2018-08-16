package com.flappygod.lipo.limagegetter.callback;

import android.graphics.Bitmap;


/************
 * 
 * Package Name:com.flappygod.lipo.limagegetter.callback <br/> 
 * ClassName: LXReadSDCardCallback <br/> 
 * Function: 晚间读取的回调 <br/> 
 * date: 2016-3-10 上午9:59:57 <br/> 
 * 
 * @author lijunlin
 */

public interface LXReadSDCardCallback {
	
	/************
	 * 读取完成
	 * @param bitmap  图片
	 */
	void readDone(Bitmap bitmap);
	
	/**************
	 * 读取失败
	 * @param e   错误
	 */
	void readError(Exception e);

}
