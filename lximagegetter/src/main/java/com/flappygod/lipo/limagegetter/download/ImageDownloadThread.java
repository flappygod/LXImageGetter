package com.flappygod.lipo.limagegetter.download;

import java.lang.ref.WeakReference;

import com.flappygod.lipo.limagegetter.CookieHolder;
import com.flappygod.lipo.limagegetter.animation.AnimationBuilder;
import com.flappygod.lipo.limagegetter.callback.LXDownloadCallback;
import com.flappygod.lipo.limagegetter.download.actor.DownLoadActor;
import com.flappygod.lipo.limagegetter.download.actor.DownLoadListener;
import com.flappygod.lipo.limagegetter.option.LXImageReadOption;
import com.flappygod.lipo.limagegetter.tools.BitMapCache;
import com.flappygod.lipo.limagegetter.tools.ImageReadTool;
import com.flappygod.lipo.limagegetter.tools.NameTool;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.Message;
import android.widget.ImageView;

/**********
 * Package Name:com.flappygod.lipo.limagegetter.downloader <br/>
 * ClassName: ImageThread <br/>
 * Function: 图片的下载线程 <br/>
 * date: 2016-3-9 下午2:21:20 <br/>
 *
 * @author lijunlin
 */
public class ImageDownloadThread extends Thread {

    public final static String TAG = "ImageDownloadThread";
    /* 对ImageView进行弱引用，保证回收的顺利进行 */
    protected WeakReference<ImageView> mimageview;
    /* 网络下载地址 */
    protected String urlStr;
    /* handler */
    protected ImageDownloadThreadHandler handler;
    /* 保存的地址 */
    protected String dirpath;
    /* 图片缓存 */
    protected BitMapCache cache;
    /* 上下文 */
    protected Context mcontext;
    /* 文件名称 */
    protected String fileName;
    /* 文件名称 */
    protected LXImageReadOption imageReadOption;
    //下载器
    private DownLoadActor downLoadActor;

    /************
     * 构造函数
     *
     * @param image           需要显示图片的View
     * @param mcontext        上下文
     * @param cache           缓存
     * @param url             图片下载的地址
     * @param dirpath         图片保存的文件夹
     * @param imageName       图片保存的名称
     * @param ImageReadedSize 图片加载的大小
     * @param callback        图片下载的回调
     */
    public ImageDownloadThread(ImageView image,
                               Context mcontext,
                               BitMapCache cache,
                               String url,
                               String dirpath,
                               String imageName,
                               LXImageReadOption ImageReadedSize,
                               LXDownloadCallback callback) {
        // 设置弱引用
        this.mimageview =  (image != null ? new WeakReference<ImageView>(image):null);
        // 下载地址
        this.urlStr = url;
        // 存储文件夹
        this.dirpath = dirpath;
        // 缓存
        this.cache = cache;
        // 上下文
        this.mcontext = mcontext;
        // 用户命名下载图片名称
        this.fileName = imageName;
        // 图片读取的大小设置
        this.imageReadOption = ImageReadedSize;
        // 创建handler，默认会显示加载图片
        this.handler = new ImageDownloadThreadHandler(image,
                urlStr,
                dirpath,
                imageName,
                callback,
                true);
        //下载器
        this.downLoadActor = new DownLoadActor(urlStr, dirpath, fileName);
    }

    /*******
     * 获取cookie管理
     * @return
     */
    public CookieHolder getCookieHolder() {
        return downLoadActor.getHolder();
    }


    /*******
     * 设置cookie管理
     * @param holder
     */
    public void setCookieHolder(CookieHolder holder) {
        downLoadActor.setHolder(holder);
    }


    public String getUrlStr() {
        return urlStr;
    }

    public ImageDownloadThreadHandler getHandler() {
        return handler;
    }

    public String getDirpath() {
        return dirpath;
    }

    public String getFileName() {
        return fileName;
    }

    public LXImageReadOption getImageReadOption() {
        return imageReadOption;
    }

    public DownLoadActor getDownLoadActor() {
        return downLoadActor;
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
     * @param view imageView
     * @return
     */
    public boolean isImageOnLoading(ImageView view) {
        if (mimageview != null) {
            return view == mimageview.get();
        } else {
            return false;
        }
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
     * 如果当前线程正在对此ImageView进行下载显示， 则不再对此ImageView进行显示，但下载线程会继续执行
     *
     * @param imageView
     */
    public void cancelShowIfIsImageView(ImageView imageView) {
        if (isImageOnLoading(imageView)) {
            if (handler != null) {
                handler.setShowImageFlag(false);
            }
        }
    }

    /********************
     * 取消当前线程对当前ImageView的显示操作，可防止复用后的图片错位
     */
    public void cancelShowImage() {
        if (handler != null) {
            handler.setShowImageFlag(false);
        }
    }

    /********************
     * 取消对正在下载图片的下载线程，之后显示操作也会被相应取消
     */
    public void cancelLoadingImage() {
        //取消下载
        this.downLoadActor.cancle();
        //显示handler不再显示
        if (handler != null) {
            handler.setShowImageFlag(false);
        }
    }


    //开始执行
    public void run() {
        // 取得当前文件的
        final String absolutePath = NameTool.getImageAbsolutePath(dirpath, urlStr, fileName);
        // 取得当前文件的在缓存中的key名称
        final String absoluteKey = NameTool.getImageAbsoluteKey(dirpath, urlStr, fileName, imageReadOption);
        // 首先查看SD卡中是否存在这个文件
        if (ImageReadTool.isFileExsitsAntNotDic(absolutePath)) {
            // 存在的情况下首先判断缓存中是否存在
            Bitmap cached = cache.getBitmapFromCache(absoluteKey);
            // 如果缓存不为空
            if (cached != null) {
                // 直接发送过去
                Message msg = handler.obtainMessage(ImageDownloadThreadHandler.ThreadMessageWhat.DONECACHE.nCode, cached);
                //发送消息
                handler.sendMessage(msg);
                // 结束
                return;
            } else {
                try {
                    // 读取图片
                    Bitmap bitmap = ImageReadTool.readFileBitmap(absolutePath, imageReadOption);
                    // 直接发送过去
                    Message msg = handler.obtainMessage(ImageDownloadThreadHandler.ThreadMessageWhat.DONESDCARD.nCode, bitmap);
                    //发送
                    handler.sendMessage(msg);
                    //添加到缓存中
                    cache.addBitmapToCache(absoluteKey, bitmap);
                    // 结束
                    return;
                } catch (Exception ex) {
                    // 直接发送过去
                    Message msg = handler.obtainMessage(ImageDownloadThreadHandler.ThreadMessageWhat.ERROR.nCode, ex);
                    //发送消息
                    handler.sendMessage(msg);
                    // 结束
                    return;
                }
            }
        }
        // SD卡中不存在这张图片，那么久没办法了，只能去下载了
        else {
            //设置下载监听
            downLoadActor.setDownLoadListener(new DownLoadListener() {
                @Override
                public void downLoadSuccess(String path, String name) {
                    try {
                        // 读取图片
                        Bitmap bitmap = ImageReadTool.readFileBitmap(absolutePath, imageReadOption);
                        // 直接发送过去
                        Message msg = handler.obtainMessage(ImageDownloadThreadHandler.ThreadMessageWhat.DONENET.nCode, bitmap);
                        //发送消息
                        handler.sendMessage(msg);
                        // 保存到缓存中
                        cache.addBitmapToCache(absoluteKey, bitmap);
                    } catch (Exception e) {
                        //读取失败
                        Message msg = handler.obtainMessage(ImageDownloadThreadHandler.ThreadMessageWhat.ERROR.nCode, e);
                        handler.sendMessage(msg);
                    }
                }
                @Override
                public void downLoading(int progress) {
                    //下载进度
                    Message m = new Message();
                    m.what = ImageDownloadThreadHandler.ThreadMessageWhat.DOWNLOADING.nCode;
                    m.arg1 = progress;
                    handler.removeMessages(ImageDownloadThreadHandler.ThreadMessageWhat.DOWNLOADING.nCode);
                    handler.sendMessage(m);
                }
                @Override
                public void downloadError(Exception e) {
                    //下载出错
                    Message msg = handler.obtainMessage(ImageDownloadThreadHandler.ThreadMessageWhat.ERROR.nCode, e);
                    handler.sendMessage(msg);
                }
                @Override
                public void downloadCancled() {
                    //下载取消
                    Message msg = handler.obtainMessage(ImageDownloadThreadHandler.ThreadMessageWhat.CANCEL.nCode);
                    handler.sendMessage(msg);
                }
            });
            //同步的方式执行任务
            downLoadActor.excutesync();
        }
    }


}
