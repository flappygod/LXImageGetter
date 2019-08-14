package com.flappygo.lilin.limagegetter.tools;

import android.app.ActivityManager;
import android.content.Context;
import android.graphics.Bitmap;

import androidx.collection.LruCache;

import java.lang.ref.SoftReference;
import java.util.LinkedHashMap;

/***************
 * Package Name:com.flappygo.lipo.limagegetter.tools <br/>
 * ClassName: BitMapCache <br/>
 * Function: 缓存类 <br/>
 * date: 2016-3-9 上午11:04:35 <br/>
 *
 * @author lijunlin
 */
public class BitMapCache {

    // 若引用缓存的数量大小
    private int SOFT_CACHE_SIZE = 25;
    /* 硬引用缓存 */
    private LruCache<String, Bitmap> mLruCache;
    /* 弱引用缓存 */
    private LinkedHashMap<String, SoftReference<Bitmap>> mSoftCache;
    /* 最大的缓存限制 M为单位 */
    private int maxLruSize = 8;
    /* 最小的缓存限制 M为单位 */
    private int minLruSize =1;

    /******
     * 构造器
     *
     * @param context
     */
    public BitMapCache(Context context) {
        init(context);
    }

    public BitMapCache(Context context, int strongSize, int softRefSize) {
        maxLruSize = strongSize;
        SOFT_CACHE_SIZE = softRefSize;
        init(context);
    }

    public BitMapCache(Context context, int strongSize) {
        maxLruSize = strongSize;
        init(context);
    }

    /***********
     * 初始化
     */
    private void init(Context context) {
        //大小为零
        long UseMemorySize = 0;

        // 获取当前空闲的内存大小，以M为单位
        ActivityManager activityManager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        // 当前应用被允许的最大内存
        long maxAppSize = activityManager.getMemoryClass();
        //java虚拟机（这个进程）能构从操作系统那里挖到的最大的内存
        long maxSize = ((int) Runtime.getRuntime().maxMemory()) / 1024 / 1024;
        //java虚拟机现在已经从操作系统那里挖过来的内存大小
        long total = ((int) Runtime.getRuntime().totalMemory()) / 1024 / 1024;
        //java 虚拟机挖过来但是没有使用的内存大小
        long free = ((int) Runtime.getRuntime().freeMemory()) / 1024 / 1024;

        //能够被允许的最大内存
        UseMemorySize = Math.min(maxAppSize, maxSize);
        //当前可以使用的内存大小
        UseMemorySize = UseMemorySize - total + free;
        //使用最大可用的四分之一，不多
        UseMemorySize = (UseMemorySize / 4) > maxLruSize ? maxLruSize : (UseMemorySize / 4);
        // 修改部分情况下拿到的可用内存为零
        if(UseMemorySize<=0){
            UseMemorySize=minLruSize;
        }
		/* 硬引用缓存容量，用KB表示 */
        long cacheSize = 1024 * UseMemorySize;

        // 创建 mLruCache
        mLruCache = new LruCache<String, Bitmap>((int) cacheSize) {
            @Override
            protected int sizeOf(String key, Bitmap value) {
                if (value != null)
                    return value.getByteCount() / 1024;
                else
                    return 0;
            }

            @Override
            protected void entryRemoved(boolean evicted, String key,
                                        Bitmap oldValue, Bitmap newValue) {
                if (oldValue != null)
                /* 硬引用缓存容量满的时候，会根据LRU算法把最近没有被使用的图片转入此软引用缓存 */ {
                    //图片小一些的时候才加入到缓存之中
                    if (oldValue.getRowBytes() * oldValue.getHeight() < 1024 * 1024) {
                        mSoftCache.put(key, new SoftReference<Bitmap>(oldValue));
                    }
                }
            }
        };

        // 使用弱引用用，保证这部分被回收掉
        mSoftCache = new LinkedHashMap<String, SoftReference<Bitmap>>(
                SOFT_CACHE_SIZE, 0.75f, true) {
            private static final long serialVersionUID = 6040103833179403725L;

            @Override
            protected boolean removeEldestEntry(java.util.Map.Entry<String, SoftReference<Bitmap>> eldest) {
                // 大于的时候开始remove
                if (size() > SOFT_CACHE_SIZE) {
                    //移除成功
                    return true;
                }
                return false;
            }
        };
    }


    //移除某张图片
    public boolean removeBitmapFromCache(String key) {
        //假如key是空的，就返回
        if (key == null || key.equals(""))
            return false;
        //是否被移除
        boolean isRemoved = false;
        //加锁,保证原子操作
        synchronized (this) {
            //图片数据
            /* 先从硬引用缓存中获取 */
            Bitmap bitmap = mLruCache.get(key);
            if (bitmap != null) {
                /* 如果找到的话，移除*/
                mLruCache.remove(key);
                isRemoved = true;
            }
            /* 如果硬引用缓存中找不到，到软引用缓存中找 */
            SoftReference<Bitmap> bitmapReference = mSoftCache.get(key);
            if (bitmapReference != null) {
                mSoftCache.remove(key);
                isRemoved = true;
            }
        }
        return isRemoved;
    }


    /*****************************
     * 从缓存中获取图片
     *****************************/
    public Bitmap getBitmapFromCache(String key) {
        //假如key是空的，就返回
        if (key == null || key.equals(""))
            return null;

        synchronized (this) {
            /* 先从硬引用缓存中获取 */
            Bitmap bitmap = mLruCache.get(key);
            if (bitmap != null) {
                /* 如果找到的话，把元素移到LinkedHashMap的最前面，从而保证在LRU算法中是最后被删除 */
                mLruCache.remove(key);
                mLruCache.put(key, bitmap);
                return bitmap;
            }
		    /* 如果硬引用缓存中找不到，到软引用缓存中找 */
            SoftReference<Bitmap> bitmapReference = mSoftCache.get(key);
            if (bitmapReference != null) {
                bitmap = bitmapReference.get();
                if (bitmap != null) {
					/* 将图片移回硬缓存 */
                    mLruCache.put(key, bitmap);
                    //软引用缓存中移除
                    mSoftCache.remove(key);
                    //返回图片
                    return bitmap;
                } else {
                    //bitmap已经被回收，移除这个key，返回找不到
                    mSoftCache.remove(key);
                }
            }
        }
        return null;
    }

    /******************************
     * 添加图片到缓存
     */
    public boolean addBitmapToCache(String key, Bitmap newBitMap) {
        //如果传入的参数为空不执行
        if (key == null || key.equals("") || newBitMap == null) {
            return false;
        }
        //先判断mSoftCache中是否存在
        synchronized (this) {

            Bitmap bitmap = mLruCache.get(key);
            if (bitmap != null) {
				/* 如果找到的话，把元素移到LinkedHashMap的最前面，从而保证在LRU算法中是最后被删除 */
                mLruCache.remove(key);
                //放到最前面
                mLruCache.put(key, newBitMap);
                //已经存在了，把它移动到最前面
                return false;
            }

            /* 如果硬引用缓存中找不到，到软引用缓存中找 */
            SoftReference<Bitmap> bitmapReference = mSoftCache.get(key);
            if (bitmapReference != null) {
                bitmap = bitmapReference.get();
                if (bitmap != null) {
					/* 将图片移回硬缓存 */
                    mLruCache.put(key, newBitMap);
                    //软引用缓存中移除
                    mSoftCache.remove(key);
                    //返回图片
                    return false;
                } else {
                    //bitmap已经被回收，添加到硬引用缓存
                    mLruCache.put(key, newBitMap);
                    return true;
                }
            } else {
                //bitmap已经被回收，添加到硬引用缓存
                mLruCache.put(key, newBitMap);
                return true;
            }
        }
    }

    /*********************
     * 清空缓存的数据
     */
    public void clearCache() {
        //清理
        synchronized (this) {
            mLruCache.evictAll();
            mSoftCache.clear();
        }
    }
}
