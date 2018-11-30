package com.flappygod.lipo.limagegetter.download;

import android.graphics.Bitmap;
import android.os.Handler;
import android.os.Message;
import android.view.animation.Animation;
import android.widget.ImageView;

import com.flappygod.lipo.limagegetter.animation.AnimationBuilder;
import com.flappygod.lipo.limagegetter.animation.AnimationBuilder.ImageSourceType;
import com.flappygod.lipo.limagegetter.callback.LXDownloadCallback;
import com.flappygod.lipo.limagegetter.tools.NameTool;

import java.lang.ref.WeakReference;

/**************
 * Package Name:com.flappygod.lipo.limagegetter.downloader <br/>
 * ClassName: ImageThreadHandler <br/>
 * Function: 用于接收回调消息 <br/>
 * date: 2016-3-9 下午6:31:21 <br/>
 *
 * @author lijunlin
 */
public class ImageDownloadThreadHandler extends Handler {

    /* 网络下载地址 */
    protected String urlStr;
    /* 图片存储地址 */
    private String mDirpath;
    /* 文件名称 */
    protected String imageName;
    /* 回调 */
    private LXDownloadCallback callBack;
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
     * @param urlStr    网络地址
     * @param mDirpath  保存的本地文件夹地址
     * @param imageName 保存的文件名称
     * @param callBack  回调
     */
    public ImageDownloadThreadHandler(ImageView image, String urlStr,
                                      String mDirpath, String imageName, LXDownloadCallback callBack,
                                      boolean showImageFlag) {
        super();
        // 弱引用
        this.mimageview = (image != null ? new WeakReference<ImageView>(image) : null);
        // 下载地址
        this.urlStr = urlStr;
        // 下载文件夹
        this.mDirpath = mDirpath;
        // 用户设置的名称
        this.imageName = imageName;
        // 回调
        this.callBack = callBack;
        // 是否显示下载完成的图片
        this.showImageFlag = showImageFlag;
    }

    // 返回过来的消息的类型
    public enum ThreadMessageWhat {
        ERROR(0), DONENET(1), CANCEL(2), DOWNLOADING(3), DONESDCARD(4), DONECACHE(5);
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
            if (callBack != null) {
                callBack.downLoadError((Exception) msg.obj);
            }
        }
        // 下载完成
        else if (msg.what == ThreadMessageWhat.DONENET.nCode) {
            if (showImageFlag) {
                ImageView imageview = (mimageview != null ? mimageview.get(): null);
                // 获取imageView,设置图片
                if (imageview != null && msg.obj != null && msg.obj instanceof Bitmap) {
                    imageview.setImageBitmap((Bitmap) msg.obj);
                }
                // 开始动画
                if (builder != null && imageview != null) {
                    Animation animation = builder.buildAnimation(imageview, ImageSourceType.FROM_NET);
                    if (animation != null) {
                        imageview.startAnimation(animation);
                    }
                }
            }
             // 回调
            if (callBack != null) {
                callBack.downLoadReady(urlStr, NameTool.getImageAbsolutePath(mDirpath, urlStr, imageName),(Bitmap) msg.obj);
            }
        }
        // 从SD卡读取完成的
        else if (msg.what == ThreadMessageWhat.DONESDCARD.nCode) {
            if (showImageFlag) {
                ImageView imageview = (mimageview != null ? mimageview.get(): null);
                // 获取imageView,设置图片
                if (imageview != null && msg.obj != null && msg.obj instanceof Bitmap) {
                    imageview.setImageBitmap((Bitmap) msg.obj);
                }
                // 开始动画
                if (builder != null && imageview != null) {
                    Animation animation = builder.buildAnimation(imageview, ImageSourceType.FROM_SDCARD);
                    if (animation != null) {
                        imageview.startAnimation(animation);
                    }
                }
            }
            // 回调
            if (callBack != null) {
                callBack.downLoadReady(urlStr, NameTool.getImageAbsolutePath(mDirpath, urlStr, imageName), (Bitmap) msg.obj);
            }
        }
        // 从CACHE读取完成的
        else if (msg.what == ThreadMessageWhat.DONECACHE.nCode) {
            if (showImageFlag) {
                ImageView imageview = (mimageview != null ? mimageview.get() : null);
                // 获取imageView,设置图片
                if (imageview != null && msg.obj != null && msg.obj instanceof Bitmap) {
                    imageview.setImageBitmap((Bitmap) msg.obj);
                }
                // 开始动画
                if (builder != null && imageview != null) {
                    Animation animation = builder.buildAnimation(imageview, ImageSourceType.FROM_CACHE);
                    if (animation != null) {
                        imageview.startAnimation(animation);
                    }
                }
            }
            // 回调
            if (callBack != null) {
                callBack.downLoadReady(urlStr,NameTool.getImageAbsolutePath(mDirpath, urlStr, imageName),(Bitmap) msg.obj);
            }
        }
        // 下载中
        else if (msg.what == ThreadMessageWhat.DOWNLOADING.nCode) {
            if (callBack != null) {
                callBack.downLoading(msg.arg1);
            }
        }
        // 下载被取消
        else if (msg.what == ThreadMessageWhat.CANCEL.nCode) {
            if (callBack != null) {
                callBack.downLoadingCanceled();
            }
        }

    }

}
