package com.flappygod.lipo.limagegetter.downloader;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.lang.ref.WeakReference;
import java.net.HttpURLConnection;
import java.net.URL;

import com.flappygod.lipo.limagegetter.CookieHolder;
import com.flappygod.lipo.limagegetter.animation.AnimationBuilder;
import com.flappygod.lipo.limagegetter.callback.LXDownloadCallback;
import com.flappygod.lipo.limagegetter.option.LXImageReadSize;
import com.flappygod.lipo.limagegetter.tools.BitMapCache;
import com.flappygod.lipo.limagegetter.tools.BitmapRadiusTool;
import com.flappygod.lipo.limagegetter.tools.DirTool;
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
    protected String DIRPATH;
    /* 图片缓存 */
    protected BitMapCache cache;
    /* 上下文 */
    protected Context mcontext;
    /* 文件名称 */
    protected String imageName;
    /* 文件名称 */
    protected LXImageReadSize ImageReadedSize;
    // 终止下载线程的标志
    private boolean threadStopFlag = false;
    // cookie
    private CookieHolder holder;

    /************
     * 构造函数
     *
     * @param image           需要显示图片的View
     * @param mcontext        上下文
     * @param cache           缓存
     * @param url             图片下载的地址
     * @param DIRPATH         图片保存的文件夹
     * @param imageName       图片保存的名称
     * @param ImageReadedSize 图片加载的大小
     * @param callback        图片下载的回调
     */
    public ImageDownloadThread(ImageView image, Context mcontext,
                               BitMapCache cache, String url, String DIRPATH, String imageName,
                               LXImageReadSize ImageReadedSize, LXDownloadCallback callback) {
        // 设置弱应用
        if (image != null)
            this.mimageview = new WeakReference<ImageView>(image);
        // 下载地址
        this.urlStr = url;
        // 存储文件夹
        this.DIRPATH = DIRPATH;
        // 缓存
        this.cache = cache;
        // 上下文
        this.mcontext = mcontext;
        // 用户命名下载图片名称
        this.imageName = imageName;
        // 图片读取的大小设置
        this.ImageReadedSize = ImageReadedSize;
        // 创建handler，默认会显示加载图片
        this.handler = new ImageDownloadThreadHandler(image, urlStr, DIRPATH,
                imageName, callback, true);
    }

    public CookieHolder getCookieHolder() {
        return holder;
    }

    public void setCookieHolder(CookieHolder holder) {
        this.holder = holder;
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

    /*************************
     * 取消对正在下载图片的下载线程，之后显示操作也会被相应取消
     */
    public void cancelLoadingImage() {
        threadStopFlag = true;
    }

    public String getUrlStr() {
        return urlStr;
    }

    public void setUrlStr(String urlStr) {
        this.urlStr = urlStr;
    }

    public void run() {

        // 取得这个被保存的文件名
        String downImagePath = NameTool
                .getImagePath(DIRPATH, urlStr, imageName);

        // 首先查看SD卡中是否存在这个文件
        if (ImageReadTool.isFileExsitsAntNotDic(downImagePath)) {
            // 存在的情况下首先判断缓存中是否存在
            Bitmap cached = null;
            // 如果说没有大小的设置，那么就直接取缓存中的图片出来
            if (ImageReadedSize == null) {
                cached = cache.getBitmapFromCache(downImagePath);
            } else {
                // 获取这个特定宽高的缓存
                cached = cache.getBitmapFromCache(downImagePath + ImageReadedSize.getSizeStrAdditional());
            }
            // 如果缓存不为空
            if (cached != null) {
                // 直接发送过去
                Message msg = handler
                        .obtainMessage(
                                ImageDownloadThreadHandler.ThreadMessageWhat.DONESDCARD.nCode,
                                cached);
                handler.sendMessage(msg);
                // 结束
                return;
            } else {

                // 读取图片
                Bitmap bitmap = null;
                try {
                    // 读取图片
                    bitmap = ImageReadTool.readFileBitmap(downImagePath,
                            ImageReadedSize);
                } catch (Exception ex) {
                    // 直接发送过去
                    Message msg = handler
                            .obtainMessage(
                                    ImageDownloadThreadHandler.ThreadMessageWhat.ERROR.nCode,
                                    ex);
                    handler.sendMessage(msg);
                    // 结束
                    return;
                }

                // 直接发送过去
                Message msg = handler
                        .obtainMessage(
                                ImageDownloadThreadHandler.ThreadMessageWhat.DONESDCARD.nCode,
                                bitmap);
                handler.sendMessage(msg);
                // 保存到缓存中
                if (ImageReadedSize == null) {
                    cache.addBitmapToCache(downImagePath, bitmap);
                } else {
                    cache.addBitmapToCache(
                            downImagePath + ImageReadedSize.getSizeStrAdditional(), bitmap);
                }
                // 结束
                return;
            }
        }
        // SD卡中不存在这张图片，那么久没办法了，只能去下载了
        else {

            RandomAccessFile rafileApk = null;
            InputStream inputStream = null;
            int fileSize = 0;
            try {
                /**** 起始字节设置为零 ****/
                long start = 0;
                /**** 创建文件夹,保证需要保存到的文件夹存在 ****/
                DirTool.createDir(DIRPATH, true);
                /**** 获取断点续传文件 ****/
                File logfile = new File(downImagePath + ".cfg");
                /**** 获取下载的apk文件 ****/
                File file = new File(downImagePath + ".data");
                /**** 判断apkfile是否存在 ****/
                File imageFile = new File(downImagePath);

                /**** 如果两个文件都存在那么就读取log中的断点数据 ****/
                if (logfile.exists() && file.exists()) {
                    DataInputStream datain = null;
                    FileInputStream login = null;
                    try {
                        /**** 获取到已经存储的长度数据 ****/
                        login = new FileInputStream(logfile);
                        datain = new DataInputStream(login);
                        start = datain.readLong();// 读取到已经写入了多少
                    } catch (Exception e) {
                        /**** 进行初始化 ****/
                        start = 0;
                        logfile.createNewFile();
                        file.createNewFile();
                    } finally {
                        /**** 关闭流 ****/
                        if (login != null)
                            login.close();
                        if (datain != null)
                            datain.close();
                    }
                } else {
                    /**** 进行初始化 ****/
                    start = 0;
                    logfile.createNewFile();
                    file.createNewFile();
                }
                /**** 取得apk文件的写入 ****/
                rafileApk = new RandomAccessFile(file, "rw");
                /**** 定位到开始的地方 ****/
                rafileApk.seek(start);
                /**** url开始连接 ****/
                URL url = new URL(urlStr);
                /**** 获取HttpURLConnection ****/
                HttpURLConnection conn = (HttpURLConnection) url
                        .openConnection();
                if (holder != null && holder.getCookie() != null) {
                    conn.addRequestProperty("Cookie", holder.getCookie());
                }
                conn.setRequestProperty("Accept-Encoding", "identity");
                /**** 设置 User-Agent ****/
                conn.setRequestProperty("User-Agent", "NetFox");
                /**** 设置断点续传的开始位置 ****/
                conn.setRequestProperty("RANGE",
                        "bytes=" + Long.toString(start) + "-");

                /**** 设置总大小 ****/
                fileSize = (int) (conn.getContentLength() + start);
                int responseCode = conn.getResponseCode();
                /**** 失败抛异常 ****/
                if (!(responseCode == HttpURLConnection.HTTP_OK || responseCode == HttpURLConnection.HTTP_PARTIAL)) {
                    throw new Exception("connection error" + responseCode
                            + " url:" + url);
                }
                /**** 成功继续执行 ****/
                else {

                    generateSession(conn);

                    inputStream = conn.getInputStream();
                    byte[] buffer = new byte[1024];
                    int len = 0;
                    while ((len = inputStream.read(buffer)) != -1) {
                        /**** 如果说线程取消了，就不在写入了 ****/
                        if (threadStopFlag)
                            break;
                        rafileApk.write(buffer, 0, len);
                        /**** 新写入了len个字节 ****/
                        start = start + len;
                        Message m = new Message();
                        m.what = ImageDownloadThreadHandler.ThreadMessageWhat.DOWNLOADING.nCode;
                        m.arg1 = (int) (start * 100 / fileSize);
                        handler.removeMessages(ImageDownloadThreadHandler.ThreadMessageWhat.DOWNLOADING.nCode);
                        handler.sendMessage(m);
                    }
                    /**** 把信息存了 ****/
                    {
                        FileOutputStream logout = null;
                        DataOutputStream outlogdata = null;
                        try {
                            logout = new FileOutputStream(logfile);
                            outlogdata = new DataOutputStream(logout);
                            outlogdata.writeLong(start);
                            outlogdata.close();
                        } catch (Exception e) {
                            throw new Exception("log error" + responseCode
                                    + " url:" + url);
                        } finally {
                            if (outlogdata != null)
                                outlogdata.close();
                            if (logout != null)
                                logout.close();
                        }
                    }
                    /**** 如果说已经全部下载完了 ****/
                    if (len == -1) {
                        file.renameTo(imageFile);

                        // 读取图片
                        Bitmap bitmap = ImageReadTool.readFileBitmap(
                                downImagePath, ImageReadedSize);

                        // 直接发送过去
                        Message msg = handler
                                .obtainMessage(
                                        ImageDownloadThreadHandler.ThreadMessageWhat.DONE.nCode,
                                        bitmap);
                        handler.sendMessage(msg);
                        // 保存到缓存中
                        if (ImageReadedSize == null) {
                            cache.addBitmapToCache(downImagePath, bitmap);
                        } else {
                            cache.addBitmapToCache(downImagePath
                                    + ImageReadedSize.getSizeStrAdditional(), bitmap);
                        }

                        return;
                    }
                    /**** 如果还没有下载完就代表是取消的 ****/
                    else {
                        Message msg = handler
                                .obtainMessage(ImageDownloadThreadHandler.ThreadMessageWhat.CANCEL.nCode);
                        handler.sendMessage(msg);
                    }
                }
            } catch (Exception e) {
                Message msg = handler
                        .obtainMessage(
                                ImageDownloadThreadHandler.ThreadMessageWhat.ERROR.nCode,
                                e);
                handler.sendMessage(msg);
            } finally {
                /**** 关闭RandomAccessFile ****/
                if (rafileApk != null)
                    try {
                        rafileApk.close();
                    } catch (IOException e1) {
                        e1.printStackTrace();
                    }
                /**** 关闭inputStream ****/
                if (inputStream != null)
                    try {
                        inputStream.close();
                    } catch (IOException e1) {
                        e1.printStackTrace();
                    }

            }

        }

    }

    /************
     * 保存当前的cookie
     *
     * @param conn 连接
     */
    private void generateSession(HttpURLConnection conn) {
        if (holder != null) {
            String sessionId = "";
            String cookieVal = "";
            String key = null;
            // 取cookie
            for (int i = 1; (key = conn.getHeaderFieldKey(i)) != null; i++) {
                if (key.equalsIgnoreCase("set-cookie")) {
                    cookieVal = conn.getHeaderField(i);
                    cookieVal = cookieVal.substring(0, cookieVal.indexOf(";"));
                    sessionId = sessionId + cookieVal;
                    holder.setCookie(sessionId);
                }
            }
        }

    }

}
