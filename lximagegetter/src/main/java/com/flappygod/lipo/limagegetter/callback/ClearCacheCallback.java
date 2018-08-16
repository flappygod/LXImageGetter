package com.flappygod.lipo.limagegetter.callback;

/***************
 * 
 * Package Name:com.LimageGetter.getter.callback <br/> 
 * ClassName: ClearCallback <br/> 
 * Function: 清理缓存 <br/> 
 * date: 2015-10-19 下午4:21:01 <br/> 
 * 
 * @author lijunlin
 */
public interface ClearCacheCallback {
	/**********
	 * 清理完成
	 */
	void done();
	/************
	 * 清理成功
	 */
	void failed(Exception error);
}
