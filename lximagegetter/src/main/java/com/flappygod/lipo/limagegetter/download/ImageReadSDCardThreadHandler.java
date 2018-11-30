package com.flappygod.lipo.limagegetter.download;

import java.lang.ref.WeakReference;

import android.graphics.Bitmap;
import android.os.Handler;
import android.os.Message;
import android.view.animation.Animation;
import android.widget.ImageView;

import com.flappygod.lipo.limagegetter.animation.AnimationBuilder;
import com.flappygod.lipo.limagegetter.animation.AnimationBuilder.ImageSourceType;
import com.flappygod.lipo.limagegetter.callback.LXReadSDCardCallback;

/************
 * 
 * Package Name:com.flappygod.lipo.limagegetter.downloader <br/>
 * ClassName: ImageReadSDCardThreadHandler <br/>
 * Function: 读取本地图片处理的handler <br/>
 * date: 2016-3-10 上午10:14:32 <br/>
 * 
 * @author lijunlin
 */
public class ImageReadSDCardThreadHandler extends Handler {

	/* 本地读取地址 */
	protected String path;
	/* 回调 */
	private LXReadSDCardCallback callBack;
	/* 对ImageView进行弱引用，保证回收的顺利进行 */
	protected WeakReference<ImageView> mimageview;
	/* 下载完成后是否显示 */
	protected boolean showImageFlag;
	/* 成功时的动画 */
	private AnimationBuilder builder;

	public AnimationBuilder getAnimationBuilder() {
		return builder;
	}

	public void setAnimationBuilder(AnimationBuilder builder) {
		this.builder = builder;
	}

	public boolean isShowImageFlag() {
		return showImageFlag;
	}

	public void setShowImageFlag(boolean showImageFlag) {
		this.showImageFlag = showImageFlag;
	}

	/*********
	 * 构建handle
	 * 
	 * @param image
	 *            imageView
	 * @param path
	 *            保存的本地文件夹地址
	 * @param showImageFlag
	 *            是否显示图片
	 * @param callBack
	 *            回调
	 */
	public ImageReadSDCardThreadHandler(ImageView image, String path,
			LXReadSDCardCallback callBack, boolean showImageFlag) {
		super();
		// 弱引用
		this.mimageview = (image != null ? new WeakReference<ImageView>(image) : null);
		// 下载地址
		this.path = path;
		// 回调
		this.callBack = callBack;
		// 是否显示下载完成的图片
		this.showImageFlag = showImageFlag;
	}

	// 返回过来的消息的类型
	public enum ThreadMessageWhat {
		ERROR(0), DONE(1);
		// 定义私有变量
		public final int nCode;

		// 构造函数，枚举类型只能为私有
		private ThreadMessageWhat(int nCode) {
			this.nCode = nCode;
		}

		@Override
		public String toString() {
			return String.valueOf(this.nCode);
		}
	}

	// 接收到消息
	public void handleMessage(Message msg) {
		// 下载出错
		if (msg.what == ThreadMessageWhat.ERROR.nCode) {
			if (callBack != null)
				callBack.readError((Exception) msg.obj);
		}
		// 下载完成
		else if (msg.what == ThreadMessageWhat.DONE.nCode) {
			// 回调
			if (callBack != null)
				callBack.readDone((Bitmap) msg.obj);
			if (showImageFlag) {
				ImageView imageview = (mimageview != null ? mimageview.get()
						: null);
				// 获取imageView,设置图片
				if (imageview != null&&msg.obj!=null&&msg.obj instanceof Bitmap) {
					imageview.setImageBitmap((Bitmap) msg.obj);
				}
				// 判断不为空
				if (imageview != null && builder != null) {
					Animation animation=builder.buildAnimation(imageview,ImageSourceType.FROM_SDCARD);
					if(animation!=null) {
						imageview.startAnimation(animation);
					}
				}
			}

		}

	}

}