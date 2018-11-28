package com.flappygod.lipo.limagegetter;

import android.content.Context;
import android.graphics.Bitmap;
import android.view.animation.Animation;
import android.widget.ImageView;

import com.flappygod.lipo.limagegetter.animation.AnimationBuilder;
import com.flappygod.lipo.limagegetter.animation.AnimationBuilder.ImageSourceType;
import com.flappygod.lipo.limagegetter.animation.DefaultAnimationBuilder;
import com.flappygod.lipo.limagegetter.callback.ClearCacheCallback;
import com.flappygod.lipo.limagegetter.callback.LXDownloadCallback;
import com.flappygod.lipo.limagegetter.callback.LXReadSDCardCallback;
import com.flappygod.lipo.limagegetter.download.ClearCacheThread;
import com.flappygod.lipo.limagegetter.download.ClearCacheThreadHandler;
import com.flappygod.lipo.limagegetter.download.ImageDownloadThread;
import com.flappygod.lipo.limagegetter.download.ImageReadSDCardThread;
import com.flappygod.lipo.limagegetter.exception.LDirException;
import com.flappygod.lipo.limagegetter.option.LXImageReadOption;
import com.flappygod.lipo.limagegetter.option.LXOption.ImageCacheOption;
import com.flappygod.lipo.limagegetter.threadpool.ExcutePoolExecutor;
import com.flappygod.lipo.limagegetter.tools.BitMapCache;
import com.flappygod.lipo.limagegetter.tools.BitmapTool;
import com.flappygod.lipo.limagegetter.tools.FileSizeTool;
import com.flappygod.lipo.limagegetter.tools.ImageReadTool;
import com.flappygod.lipo.limagegetter.tools.LogTool;
import com.flappygod.lipo.limagegetter.tools.NameTool;

import java.io.File;
import java.util.List;


/**********
 * Package Name:com.flappygod.lipo.limagegetter <br/>
 * ClassName: LXImageGetter <br/>
 * Function: 下载器 <br/>
 * date: 2016-3-9 下午1:40:46 <br/>
 *
 * @author lijunlin
 */

public class LXImageGetter {

    // 用于log的tag
    protected static final String TAG = "LXImageGetter";
    /* 单例的情况下 */
    private static LXImageGetter instance;
    /* 上下文 application */
    private Context mcontext;
    /* 成功时的动画 */
    private AnimationBuilder builder;
    // 线程池
    private ExcutePoolExecutor threadpool;
    /* 缓存 */
    private BitMapCache mCache;
    /* 图片存储地址 */
    private String mDirpath;
    /* 移除ImageView 的时候关闭线程 */
    private boolean cancelDownloadingReuse = false;
    // cookie
    private CookieHolder cookieHolder;

    private Bitmap bitmap;

    /******************
     * 创建imagegetter
     *
     * @param appContext 上下文
     */
    public LXImageGetter(Context appContext) {
        // 首先保存下上下文
        this.mcontext = appContext.getApplicationContext();
        // 创建缓存
        this.mCache = new BitMapCache(mcontext);
        // 创建一个大小为3的线程池
        this.threadpool = new ExcutePoolExecutor(3);
        // 设置保存的地地址为默认地址
        this.mDirpath = getDefaultDirPath(mcontext);
        // 设置一下动画,默认动画
        this.builder = new DefaultAnimationBuilder();
        //透明背景
        bitmap = BitmapTool.getAlphaBitMap(10, 10);
    }

    /************
     * 创建imagegetter
     *
     * @param appContext 上下文
     * @param poolSize   线程池的大小
     */
    public LXImageGetter(Context appContext, int poolSize) {
        // 首先保存下上下文
        this.mcontext = appContext.getApplicationContext();
        // 创建缓存
        this.mCache = new BitMapCache(mcontext);
        // 创建一个大小为3的线程池
        this.threadpool = new ExcutePoolExecutor(poolSize);
        // 设置保存的地地址为默认地址
        this.mDirpath = getDefaultDirPath(mcontext);
        // 设置一下动画,默认动画
        this.builder = new DefaultAnimationBuilder();
        //透明背景
        this.bitmap = BitmapTool.getAlphaBitMap(10, 10);
    }

    /*******************
     * 创建imagegetter
     *
     * @param appContext 上下文
     * @param poolSize   线程池的大小
     * @param dirPath    存储的文件夹
     */
    public LXImageGetter(Context appContext, int poolSize, String dirPath) {
        // 首先保存下上下文
        this.mcontext = appContext.getApplicationContext();
        // 创建缓存
        this.mCache = new BitMapCache(mcontext);
        // 创建一个大小为3的线程池
        this.threadpool = new ExcutePoolExecutor(poolSize);
        // 设置保存的地地址为默认地址
        this.mDirpath = dirPath;
        // 设置一下动画,默认动画
        this.builder = new DefaultAnimationBuilder();
        //透明背景
        this.bitmap = BitmapTool.getAlphaBitMap(10, 10);
    }

    public String getDirpath() {
        return mDirpath;
    }

    /*********************
     * 构造器
     *
     * @param appContext   上下文
     * @param poolSize     线程池大小
     * @param dirPath      保存位置
     * @param LruCacheSize 硬缓存大小
     * @param softRefSize  软缓存大小
     */
    public LXImageGetter(Context appContext,
                         int poolSize,
                         String dirPath,
                         int LruCacheSize,
                         int softRefSize) {
        // 首先保存下上下文
        this.mcontext = appContext.getApplicationContext();
        // 创建缓存
        this.mCache = new BitMapCache(mcontext, LruCacheSize, softRefSize);
        // 创建一个大小为3的线程池
        this.threadpool = new ExcutePoolExecutor(poolSize);
        if (dirPath == null) {
            // 设置保存的地地址为默认地址
            this.mDirpath = getDefaultDirPath(appContext);
        } else {
            // 设置保存的地地址为默认地址
            this.mDirpath = dirPath;
        }
        // 设置一下动画,默认动画
        this.builder = new DefaultAnimationBuilder();
        //透明背景
        this.bitmap = BitmapTool.getAlphaBitMap(10, 10);
    }

    /************
     * 获取图片保存的地址
     *
     * @param URl  网络地址
     * @param name 自命名名称
     * @return
     */
    public String getImageSDpath(String URl, String name) {
        // 取得这个被保存的文件名
        String downImagePath = NameTool.getImageAbsolutePath(mDirpath, URl, name);
        return downImagePath;
    }


    public CookieHolder getCookieHolder() {
        return cookieHolder;
    }

    public void setCookieHolder(CookieHolder cookieHolder) {
        this.cookieHolder = cookieHolder;
    }

    /****************
     * 设置缓存的大小
     *
     * @param LruCacheSize 硬引用缓存 M(兆)
     * @param softRefSize  sofeRef个数(个)
     */
    public void setCatchSize(int LruCacheSize, int softRefSize) {
        this.mCache = new BitMapCache(mcontext, LruCacheSize, softRefSize);
    }

    /***********
     * 设置线程池大小
     */
    public void setThreadPoolSize(int size) {
        this.threadpool = new ExcutePoolExecutor(size);
    }

    /***********
     * 是否移除ImageView 的时候关闭线程
     *
     * @return
     */
    public boolean isCancelDownloadingReuse() {
        return cancelDownloadingReuse;
    }

    /*************
     * 是否移除ImageView 的时候关闭线程
     *
     * @param cancelDownloadingReuse
     */
    public void setCancelDownloadingReuse(boolean cancelDownloadingReuse) {
        this.cancelDownloadingReuse = cancelDownloadingReuse;
    }

    /*********
     * 获取动画
     *
     * @return 动画
     */

    public AnimationBuilder getAnimationBuilder() {
        return builder;
    }

    /*******
     * 设置动画
     *
     * @param builder 动画构建
     */
    public void setAnimationBuilder(AnimationBuilder builder) {
        this.builder = builder;
    }

    /*********
     * 获取默认的保存图片地址
     *
     * @param context 上下文
     * @return
     */
    public static String getDefaultDirPath(Context context) {
        String cachePath = null;
        try {
            if (context.getExternalCacheDir() != null) {
                cachePath = context.getExternalCacheDir().getPath() + "/imagecache/";
            } else if (context.getCacheDir() != null) {
                cachePath = context.getCacheDir().getPath() + "/imagecache/";
            }
        } catch (Exception e) {
            cachePath = "";
        }
        return cachePath == null ? "" : cachePath;
    }

    /************
     * 单例模式获取默认的imagegetter
     *
     * @param appContext 上下文
     * @return
     */
    public static LXImageGetter getInstance(Context appContext) {
        if (instance == null) {
            synchronized (LXImageGetter.class) {
                if (instance == null) {
                    Context context = appContext.getApplicationContext();
                    instance = new LXImageGetter(context);
                }
            }
        }
        return instance;
    }


    /***************
     * 初始化
     *
     * @param appContext 上下文
     */
    public static void init(Context appContext) {
        if (instance == null) {
            synchronized (LXImageGetter.class) {
                if (instance == null) {
                    Context context = appContext.getApplicationContext();
                    instance = new LXImageGetter(context);
                }
            }
        }
    }

    /************
     * 初始化
     *
     * @param appContext   上下文
     * @param poolSize     线程池
     * @param dirPath      缓存路径
     * @param LruCacheSize 硬缓存大小
     * @param softRefSize  软缓存大小
     */
    public static void init(Context appContext,
                            int poolSize,
                            String dirPath,
                            int LruCacheSize,
                            int softRefSize) {
        if (instance == null) {
            synchronized (LXImageGetter.class) {
                if (instance == null) {
                    Context context = appContext.getApplicationContext();
                    instance = new LXImageGetter(context, poolSize, dirPath, LruCacheSize, softRefSize);
                }
            }
        }
    }


    /**************
     * 直接获取单例，需要init
     *
     * @return
     */
    public static LXImageGetter getInstance() {
        if (instance == null) {
            throw new RuntimeException("LXImageGetter did not init");
        }
        return instance;
    }


    /******************
     * 获取缓存中的bitmap
     *
     * @param url             地址
     * @param imageName       用户自由设置的保存的文件名
     * @param imageReadOption 读取的大小设置
     * @return
     */
    public Bitmap getCachedUrlBitmap(String url, String imageName, LXImageReadOption imageReadOption) {
        // 取得这个被保存的文件的key值
        Bitmap cached = mCache.getBitmapFromCache(NameTool.getImageAbsoluteKey(mDirpath, url, imageName, imageReadOption));
        //返回缓存
        return cached;
    }

    /*******************
     * 获取SD卡中已经缓存的图片
     *
     * @param url             地址
     * @param imageName       用户自由设置的保存的文件名
     * @param ImageReadedSize 读取图片的大小
     * @return
     */
    public Bitmap getSDedUrlBitmap(String url, String imageName, LXImageReadOption ImageReadedSize) throws Exception {
        // 读取图片
        bitmap = ImageReadTool.readFileBitmap(NameTool.getImageAbsolutePath(mDirpath, url, imageName), ImageReadedSize);
        //返回bitmap
        return bitmap;
    }


    /**************
     * 通过网络获取图片
     *
     * @param image 需要填充的ImageView
     * @param URl   网络下载地址
     */
    public void getImageWithUrl(final ImageView image,
                                final String URl) {
        getImageWithUrl(image,
                URl,
                null,
                null,
                null,
                null,
                null);
    }


    /**************
     * 通过网络获取图片
     *
     * @param image       需要填充的ImageView
     * @param URl         下载地址
     * @param placeHolder placeholder
     */
    public void getImageWithUrl(final ImageView image,
                                final String URl,
                                Bitmap placeHolder) {
        getImageWithUrl(image,
                URl,
                placeHolder,
                null,
                null,
                null,
                null);
    }


    public void getImageWithUrl(final ImageView image,
                                final String URl,
                                Bitmap placeHolder,
                                LXDownloadCallback callback) {
        getImageWithUrl(image,
                URl,
                placeHolder,
                callback,
                null,
                null,
                null);
    }

    /******************
     * 通过网络获取图片
     *
     * @param image           需要填充的ImageView
     * @param URl             下载地址
     * @param placeHolder     展示的Image
     * @param callback        下载过程的回调
     * @param ImageReadedSize daxiao
     */
    public void getImageWithUrl(final ImageView image,
                                final String URl,
                                Bitmap placeHolder,
                                LXDownloadCallback callback,
                                LXImageReadOption ImageReadedSize) {
        getImageWithUrl(image,
                URl,
                placeHolder,
                callback,
                ImageReadedSize,
                null,
                null);
    }

    public void getImageWithUrl(final ImageView image,
                                final String URl,
                                Bitmap placeHolder,
                                LXDownloadCallback callback,
                                LXImageReadOption ImageReadedSize,
                                ImageCacheOption option) {
        getImageWithUrl(image,
                URl,
                placeHolder,
                callback,
                ImageReadedSize,
                option,
                null);
    }

    /**********************
     * 通过网络获取图片
     *
     * @param image         需要填充的ImageView
     * @param URl           下载地址
     * @param placeHolder   展示的Image
     * @param callback      下载过程的回调
     * @param imageReadSize 图片读取过程中的设置
     * @param option        图片读取过程中的设置
     * @param imageName     图片保存的名称
     */
    public void getImageWithUrl(final ImageView image,
                                final String URl,
                                Bitmap placeHolder,
                                LXDownloadCallback callback,
                                LXImageReadOption imageReadSize,
                                ImageCacheOption option,
                                String imageName) {

        // 阻止已经运行的线程对此图片进行任何的操作
        removeImageLoad(image);

        /* 首先清除掉图片的动画 */
        if (image != null) {
            //清除图片动画
            image.clearAnimation();
            //判断占位是否为空
            if (placeHolder == null) {
                image.setImageBitmap(bitmap);
            } else {
                image.setImageBitmap(placeHolder);
            }
        }

        // 取得被保存的文件的绝对路径
        String absouluteSDPath = NameTool.getImageAbsolutePath(mDirpath, URl, imageName);
        // 取得被保存的文件的绝对键值
        String absouluteKeyName = NameTool.getImageAbsoluteKey(mDirpath, URl, imageName, imageReadSize);

        // 假如需要刷新这个数据
        if (option != null && option == ImageCacheOption.REFRESH_CACHE) {
            // 删除本地文件
            if (ImageReadTool.isFileExsitsAntNotDic(absouluteSDPath)) {
                // 从缓存中同样也移除当前的图片的缓存
                mCache.removeBitmapFromCache(absouluteKeyName);
                // 删除这个文件
                File file = new File(absouluteSDPath);
                //删除
                file.delete();
            }
        }


        // 如果SD卡中已经存在了这个文件
        if (ImageReadTool.isFileExsitsAntNotDic(absouluteSDPath)) {
            Bitmap cached = mCache.getBitmapFromCache(absouluteKeyName);
            // 如果缓存不为空
            if (cached != null) {
                // 设置到Image中去
                if (image != null) {
                    image.setImageBitmap(cached);
                }
                // 直接回调完成的方法
                if (callback != null) {
                    callback.downLoadReady(URl, absouluteSDPath, cached);
                }
                // 开始动画
                if (builder != null && image != null) {
                    Animation animation = builder.buildAnimation(image, ImageSourceType.FROM_CACHE);
                    if (animation != null) {
                        image.startAnimation(animation);
                    }
                }
                // 结束
                return;
            }
        }


        // 开启下载的线程
        ImageDownloadThread thread = new ImageDownloadThread(image, mcontext, mCache, URl, mDirpath, imageName, imageReadSize, callback);
        //设置cookie
        thread.setCookieHolder(cookieHolder);
        // 设置下载完成显示的动画
        thread.setAnimationBuilder(builder);
        // 开始执行了
        threadpool.execute(thread);

    }

    /****************
     * 从本地读取图片并显示
     *
     * @param image           需要显示的View
     * @param path            路径
     * @param ImageReadedSize 图片显示设置
     * @param callback        回调
     */
    public void getImageSdcard(final ImageView image,
                               final String path,
                               LXImageReadOption ImageReadedSize,
                               LXReadSDCardCallback callback) {
        getImageSdcard(image, path, null, ImageReadedSize, callback);
    }


    /****************
     * 从本地读取图片并显示
     *
     * @param image           需要显示的View
     * @param path            本地路径
     * @param ImageReadedSize 图片显示大小设置
     */
    public void getImageSdcard(final ImageView image,
                               final String path,
                               LXImageReadOption ImageReadedSize) {

        getImageSdcard(image, path, null, ImageReadedSize, null);
    }

    /****************
     * 从本地读取图片并显示
     *
     * @param image           需要显示的View
     * @param path            本地路径
     * @param ImageReadedSize 图片显示大小设置
     * @param callback        回调
     * @param placeHolder     默认显示
     */
    public void getImageSdcard(final ImageView image,
                               final String path,
                               Bitmap placeHolder,
                               LXImageReadOption ImageReadedSize,
                               LXReadSDCardCallback callback) {
        //阻止已经运行的线程对此图片进行任何的操作
        removeImageLoad(image);
        /* 首先清除掉图片的动画 */
        if (image != null) {
            //清空动画
            image.clearAnimation();
            if (placeHolder == null) {
                //设置空白显示
                image.setImageBitmap(bitmap);
            } else {
                //设置占位
                image.setImageBitmap(placeHolder);
            }
        }
        //绝对键值
        String absoluteKey = NameTool.getImageAbsoluteKey(path, ImageReadedSize);
        //获取缓存中的数据
        Bitmap cached = mCache.getBitmapFromCache(absoluteKey);
        // 如果缓存不为空
        if (cached != null) {
            // 设置到Image中去
            if (image != null) {
                image.setImageBitmap(cached);
            }
            // 直接回调完成的方法
            if (callback != null) {
                callback.readDone(cached);
            }
            // 开始动画
            if (builder != null && image != null) {
                Animation animation = builder.buildAnimation(image, ImageSourceType.FROM_CACHE);
                if (animation != null) {
                    image.startAnimation(animation);
                }
            }
            // 结束
            return;
        }

        // 如果SD卡中已经存在了这个文件
        if (ImageReadTool.isFileExsitsAntNotDic(path)) {
            //开始读取线程
            ImageReadSDCardThread thread = new ImageReadSDCardThread(image, mcontext, mCache, path, ImageReadedSize, callback);
            // 设置下载完成显示的动画
            thread.setAnimationBuilder(builder);
            // 开始执行了
            threadpool.execute(thread);

        } else {
            // 返回通知文件不存在
            callback.readError(new LDirException("no file exsists or file is directory"));
        }
    }

    /**************
     * 获取当前SD卡中缓存的大小
     *
     * @return
     */
    public String getPathSDmemorySize() {
        return FileSizeTool.getAutoFileOrFilesSize(mDirpath);
    }

    /**************
     * 获取指定路径SD卡中缓存的大小
     *
     * @param path 路径
     * @return
     */
    public String getPathSDmemorySize(String path) {
        return FileSizeTool.getAutoFileOrFilesSize(path);
    }

    /*************
     * 清空当前path下的缓存文件
     *
     * @param callback 回调函数
     */
    public void clearPathSDmemory(final ClearCacheCallback callback) {
        ClearCacheThreadHandler handler = new ClearCacheThreadHandler(callback);
        ClearCacheThread thread = new ClearCacheThread(mDirpath, handler);
        thread.start();
    }

    /***********
     * 清空path下的缓存文件
     *
     * @param path     路径
     * @param callback 回调函数
     */
    public void clearPathSDmemory(String path, final ClearCacheCallback callback) {
        ClearCacheThreadHandler handler = new ClearCacheThreadHandler(callback);
        ClearCacheThread thread = new ClearCacheThread(path, handler);
        thread.start();
    }

    /***********************
     * 取消所有线程的下载操作
     */
    public void cancelAllDownloading() {
        List<Thread> threads = threadpool.getAllThread();
        for (int s = 0; s < threads.size(); s++) {
            Thread thread = threads.get(s);
            if (thread instanceof ImageDownloadThread) {
                ((ImageDownloadThread) thread).cancelLoadingImage();
                ((ImageDownloadThread) thread).cancelShowImage();
            }
        }
    }

    /*****************
     * 取消对某张图片的download
     *
     * @param url 下载地址
     */
    public void cancelDownLoadImage(String url) {

        List<Thread> threads = threadpool.getAllThread();
        for (int s = 0; s < threads.size(); s++) {
            Thread thread = threads.get(s);
            if (thread instanceof ImageDownloadThread) {
                if (((ImageDownloadThread) thread).getUrlStr().equals(url)) {
                    ((ImageDownloadThread) thread).cancelLoadingImage();
                    ((ImageDownloadThread) thread).cancelShowImage();
                }
            }
        }
    }

    /***********************
     * 清空正在运行的线程对ImageView的后续改变
     *
     * @param image 需要被清空的ImageView
     * @throws
     */
    private void removeImageLoad(ImageView image) {
        // 为空不做任何事
        if (image == null) {
            LogTool.d(TAG, "the image is null");
            return;
        }
        // 获取所有的线程
        List<Thread> threads = threadpool.getAllThread();
        for (int s = 0; s < threads.size(); s++) {
            Thread thread = threads.get(s);
            // 下载的线程
            if (thread instanceof ImageDownloadThread) {
                // 判断这个线程是不是正要或者即将对这个image进行操作
                if (((ImageDownloadThread) thread).isImageOnLoading(image)) {
                    if (cancelDownloadingReuse) {
                        ((ImageDownloadThread) thread).cancelLoadingImage();
                        ((ImageDownloadThread) thread).cancelShowImage();
                    } else {
                        ((ImageDownloadThread) thread).cancelShowImage();
                    }
                }
            }
            // 读取SD卡的线程
            if (thread instanceof ImageReadSDCardThread) {
                ((ImageReadSDCardThread) thread).cancelShowOnImageView(image);
            }
        }
    }

}
