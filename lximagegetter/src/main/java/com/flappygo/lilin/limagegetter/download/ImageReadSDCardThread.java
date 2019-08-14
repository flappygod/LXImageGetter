package com.flappygo.lilin.limagegetter.download;

import java.lang.ref.WeakReference;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.Message;
import android.widget.ImageView;

import com.flappygo.lilin.limagegetter.animation.AnimationBuilder;
import com.flappygo.lilin.limagegetter.callback.LXReadSDCardCallback;
import com.flappygo.lilin.limagegetter.option.LXImageReadOption;
import com.flappygo.lilin.limagegetter.tools.ImageReadTool;
import com.flappygo.lilin.limagegetter.tools.BitMapCache;
import com.flappygo.lilin.limagegetter.tools.NameTool;

public class ImageReadSDCardThread extends Thread {

	public final static String TAG = "ImageReadSDCardThread";

	/* 对ImageView进行弱引用，保证回收的顺利进行 */
	protected WeakReference<ImageView> mimageview;
	/* handler */
	protected ImageReadSDCardThreadHandler handler;
	/* 保存的地址 */
	protected String path;
	/* 图片缓存 */
	protected BitMapCache cache;
	/* 上下文 */
	protected Context mcontext;
	/* 文件名称 */
	protected LXImageReadOption ImageReadedSize;

	/************
	 * 构造函数
	 * 
	 * @param image
	 *            需要显示图片的View
	 * @param mcontext
	 *            上下文
	 * @param cache
	 *            缓存
	 * @param path
	 *            本地地址
	 * @param ImageReadedSize
	 *            图片加载的大小
	 * @param callback
	 *            图片下载的回调
	 */
	public ImageReadSDCardThread(ImageView image,
								 Context mcontext,
								 BitMapCache cache,
								 String path,
								 LXImageReadOption ImageReadedSize,
								 LXReadSDCardCallback callback) {
		// 设置弱应用
		this.mimageview = (image != null ? new WeakReference<ImageView>(image) : null);
		// 存储文件夹
		this.path = path;
		// 缓存
		this.cache = cache;
		// 上下文
		this.mcontext = mcontext;
		// 图片读取的大小设置
		this.ImageReadedSize = ImageReadedSize;
		// 创建handler，默认会显示加载图片
		this.handler = new ImageReadSDCardThreadHandler(image, path, callback,
				true);
	}

	/**********
	 * 获取下载完成之后显示时执行的animation
	 * 
	 * @return
	 */
	public AnimationBuilder getAnimationBuilder() {
		if (handler != null) {
			return handler.getAnimationBuilder();
		}
		return null;
	}

	/************
	 * 设置下载完成之后显示时执行的animation
	 * 
	 * @param builder
	 */
	public void setAnimationBuilder(AnimationBuilder builder) {
		if (handler != null) {
			handler.setAnimationBuilder(builder);
		}
	}

	/***********
	 * 判断这个ImageView是否正在被当前的线程占用
	 * 
	 * @param view
	 *            imageView
	 * @return
	 */
	public boolean isImageOnLoading(ImageView view) {
		return view == mimageview.get();
	}

	/*****************
	 * 获取当前的线程正在占用的ImageView
	 * 
	 * @return
	 */
	public ImageView getOnloadingImage() {
		if (mimageview != null) {
			return mimageview.get();
		}
		return null;
	}

	/********************
	 * 取消当前线程对当前ImageView的显示操作，可防止复用后的图片错位
	 */
	public void cancelShowImage() {
		if (handler != null) {
			handler.setShowImageFlag(false);
		}
	}

	/**********
	 * 如果当前线程正在对此ImageView进行下载显示， 则不再对此ImageView进行显示，但下载线程会继续执行
	 * 
	 * @param imageView
	 */
	public void cancelShowOnImageView(ImageView imageView) {
		if (isImageOnLoading(imageView)) {
			if (handler != null) {
				handler.setShowImageFlag(false);
			}
		}
	}

	public void run() {

		// 取得这个被保存的文件名
		String downImagePath = path;
		//获取key
		String absoluteKey= NameTool.getImageAbsoluteKey(downImagePath,ImageReadedSize);
		//获取是否存在
		Bitmap cached=cache.getBitmapFromCache(absoluteKey);
		// 如果缓存不为空
		if (cached != null) {
			// 直接发送过去
			Message msg = handler
					.obtainMessage(
							ImageReadSDCardThreadHandler.ThreadMessageWhat.DONE.nCode,
							cached);
			handler.sendMessage(msg);
			// 结束
			return;
		}else{
			try {
				// 读取文件
				Bitmap bitmap = ImageReadTool.readFileBitmap(downImagePath,ImageReadedSize);
				//成功发送
				Message msg = handler.obtainMessage(
						ImageReadSDCardThreadHandler.ThreadMessageWhat.DONE.nCode,
						bitmap);
				//成功发送
				handler.sendMessage(msg);
				//添加到缓存
				cache.addBitmapToCache(absoluteKey, bitmap);
			} catch (Exception e) {
				// 直接发送过去
				Message msg = handler.obtainMessage(
								ImageReadSDCardThreadHandler.ThreadMessageWhat.ERROR.nCode,
								e);
				handler.sendMessage(msg);
				// 结束
				return;
			}
		}
	}

}
