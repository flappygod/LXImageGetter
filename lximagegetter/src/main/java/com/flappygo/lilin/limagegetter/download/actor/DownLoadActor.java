package com.flappygo.lilin.limagegetter.download.actor;

import android.os.Handler;
import android.os.Message;

import com.flappygo.lilin.limagegetter.CookieHolder;
import com.flappygo.lilin.limagegetter.tools.DirTool;
import com.flappygo.lilin.limagegetter.tools.FileSizeTool;
import com.flappygo.lilin.limagegetter.tools.LogTool;
import com.flappygo.lilin.limagegetter.tools.NameTool;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.net.HttpURLConnection;
import java.net.URL;


/***************************
 * 下载工具
 */
public class DownLoadActor {

    private String tag = "DownLoadActor";

    //当前的状态
    private int downLoadState = -1;

    //下载失败
    public final static int ERROR = 3;
    //下载完成
    public final static int DONE = 2;
    //下载取消
    public final static int CANCEL = 1;
    //正在下载
    public final static int DOWNLOADING = 0;

    // 下载进度
    private int progress = 0;
    // 终止下载线程的标志
    private boolean threadStopFlag = false;
    // 线程是否忙碌
    private boolean ThreadBusy = false;
    // 下载的地址
    private String urlPath;
    // 下载保存的路径
    private String dirpath;
    // 下载保存的文件名称
    private String fileName;
    //当前的绝对地址
    private String absolutePath;
    // 下载的监听
    private DownLoadListener listener;
    // cookie
    private CookieHolder holder;

    /***********
     * 构造器
     *
     * @param urlPath
     *            需要下载的地址
     * @param dirpath
     *            需要保存的地址
     */
    public DownLoadActor(String urlPath, String dirpath) {
        this.urlPath = urlPath;
        this.dirpath = dirpath;
        initFileNameAndPath();
    }

    /***********
     * 构造器
     *
     * @param urlPath
     *            需要下载的地址
     * @param dirpath
     *            需要保存的地址
     * @param dirpath
     *            需要保存的名称
     */
    public DownLoadActor(String urlPath, String dirpath, String fileName) {
        this.urlPath = urlPath;
        this.dirpath = dirpath;
        this.fileName = fileName;
        initFileNameAndPath();
    }

    /******
     * 初始化设置文件的名称
     */
    private void initFileNameAndPath() {
        //设置图片在SD卡中保存的名称
        fileName = NameTool.getImageName(urlPath, fileName);
        //取得它的绝对路径
        absolutePath = NameTool.getImageAbsolutePath(dirpath, urlPath, fileName);
    }

    /*******
     * 获取当前的下载状态
     * @return
     */
    final public int getDownLoadState() {
        return downLoadState;
    }

    /********
     * 获取保存在SD卡中的名称
     * @return
     */
    final public String getFileName() {
        return fileName;
    }

    /**********
     * 获取绝对地址
     * @return
     */
    final public String getAbsolutePath() {
        return absolutePath;
    }


    /***********
     * 获取下载的地址
     *
     * @return
     */
    final public String getDownLoadUrl() {
        return urlPath;
    }

    public CookieHolder getHolder() {
        return holder;
    }

    public void setHolder(CookieHolder holder) {
        this.holder = holder;
    }

    /*******
     * 设置下载的监听
     */
    public void setDownLoadListener(DownLoadListener li) {
        listener = li;
    }

    /************
     * 取消下载，是否取消成功需要在监听中获得回调
     */
    public void cancle() {
        threadStopFlag = true;
    }

    /*************
     * 获取progress
     *
     * @return
     */
    public int getProgress() {
        return progress;
    }


    /**********
     * 使用同步的方式进行调用
     */
    public synchronized void excutesync() {
        //判断是否忙碌
        if (ThreadBusy) {
            return;
        } else {
            ThreadBusy = true;
        }


        //起始字节设置为零
        long start = 0;
        //文件的总大小
        long totalSize = 0;
        //断点续传的配置文件
        File conffile = null;
        //下载的数据文件
        File dataFile = null;
        //真实下载的文件
        File realfile = null;
        //文件
        RandomAccessFile rafileApk = null;
        //数据流
        InputStream inputStream = null;


        try {

            //文件夹必须存在
            DirTool.createDir(dirpath, true);

            //配置文件
            conffile = new File(absolutePath + ".cfg");
            //数据临时文件
            dataFile = new File(absolutePath + ".data");
            //真实下载的文件
            realfile = new File(absolutePath);


            //如果已经存在了这个文件 校验文件大小，初步保证完整性
            if (realfile.exists()) {
                //url开始连接
                URL url = new URL(urlPath);
                //打开链接
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                //设置RequestProperty
                conn.setRequestProperty("Accept-Encoding", "identity");
                //设置 User-Agent
                conn.setRequestProperty("User-Agent", "NetFox");
                //设置断点续传的开始位置
                conn.setRequestProperty("RANGE", "bytes=" + Long.toString(0) + "-");
                //获取总大小
                int  fileSize = conn.getContentLength();
                //对比下载的文件的总大小是否相等
                if (fileSize == FileSizeTool.getFileSize(realfile)) {
                    // 下载完成
                    if (listener != null) {
                        listener.downLoadSuccess(dirpath + fileName, fileName);
                    }
                    //返回不再继续了
                    return;
                } else {
                    //进行初始化
                    start = 0;
                }
            }
            //如果文件不存在，但是两个配置文件存在
            else if (conffile.exists() && dataFile.exists()) {
                //传入的数据
                DataInputStream confData = null;
                //log数据
                FileInputStream confin = null;
                try {
                    //获取到已经存储的长度数据
                    confin = new FileInputStream(conffile);
                    //获取到已经存储的长度数据
                    confData = new DataInputStream(confin);
                    // 读取到已经写入了多少
                    start = confData.readLong();
                    //不相等的情况下抛出
                    if (start != FileSizeTool.getFileSize(dataFile)) {
                        start = 0;
                    }
                } catch (Exception e) {
                    //进行初始化
                    start = 0;
                } finally {
                    //关闭流
                    if (confin != null) {
                        confin.close();
                    }
                    if (confData != null) {
                        confData.close();
                    }
                }
            }

            //如果开始是零，那么重新创建文件
            if (start == 0) {
                //创建
                conffile.createNewFile();
                //创建
                dataFile.createNewFile();
            }


            //取得apk文件的写入
            rafileApk = new RandomAccessFile(dataFile, "rw");
            //定位到开始的地方
            rafileApk.seek(start);
            //url开始连接
            URL url = new URL(urlPath);
            //打开链接
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            //初始化cookie
            setCookie(conn);
            //设置RequestProperty
            conn.setRequestProperty("Accept-Encoding", "identity");
            //设置 User-Agent
            conn.setRequestProperty("User-Agent", "NetFox");
            //设置断点续传的开始位置
            conn.setRequestProperty("RANGE", "bytes=" + Long.toString(start) + "-");
            //设置总大小
            totalSize = (int) (conn.getContentLength() + start);


            //获取返回值
            int responseCode = conn.getResponseCode();
            //失败抛异常
            if (!(responseCode == HttpURLConnection.HTTP_OK || responseCode == HttpURLConnection.HTTP_PARTIAL)) {
                throw new Exception("Connection error:" + responseCode + " url:" + url);
            }
            //成功继续执行
            else {
                //保存cookie等数据
                generateSession(conn);
                //获取到input
                inputStream = conn.getInputStream();
                //缓存大小
                byte[] buffer = new byte[1024];
                //长度
                int len = 0;
                //循环读取
                while ((len = inputStream.read(buffer)) != -1) {
                    //线程被取消了，不再读了，如果说线程取消了，就不在写入了
                    if (threadStopFlag) {
                        break;
                    }
                    //写入数据
                    rafileApk.write(buffer, 0, len);
                    //新写入了len个字节
                    start = start + len;
                    //下载中
                    if (listener != null) {
                        listener.downLoading((int) (start * 100 / totalSize));
                    }
                }
                //如果说已经全部下载完了
                if (len == -1) {
                    //重新进行命名
                    dataFile.renameTo(realfile);
                    // 下载完成
                    if (listener != null) {
                        listener.downLoadSuccess(dirpath + fileName, fileName);
                    }
                    return;
                }
                //如果还没有下载完就代表是取消的
                else {
                    // 下载完成
                    if (listener != null) {
                        listener.downloadCancled();
                    }
                }
            }
        } catch (Exception e) {
            if (listener != null) {
                listener.downloadError((Exception) e);
            }
        } finally {
            //保存当前的下载进度
            try {
                if(conffile!=null) {
                    saveConfig(conffile, start);
                }
            } catch (Exception ex) {
                LogTool.e(tag, ex.getMessage());
            }
            //关闭RandomAccessFile
            try {
                if(rafileApk!=null) {
                    rafileApk.close();
                }
            } catch (Exception ex) {
                LogTool.e(tag, ex.getMessage());
            }
            //关闭inputStream
            try {
                if(inputStream!=null) {
                    inputStream.close();
                }
            } catch (Exception ex) {
                LogTool.e(tag, ex.getMessage());
            }
            ThreadBusy = false;
        }

    }





    /************
     * 使用异步的方式进行调用
     */
    public synchronized void excute() {

        //判断是否忙碌
        if (ThreadBusy) {
            return;
        } else {
            ThreadBusy = true;
        }

        //handler
        final Handler proHanlder = new Handler() {
            public void handleMessage(Message msg) {
                //正在下载
                if (msg.what == DOWNLOADING && msg.arg1 != progress) {
                    progress = msg.arg1;
                    if (listener != null) {
                        listener.downLoading(progress);
                    }
                    downLoadState = DOWNLOADING;
                } else if (msg.what == CANCEL) {
                    // 下载取消
                    if (listener != null) {
                        listener.downloadCancled();
                    }
                    downLoadState = CANCEL;
                } else if (msg.what == DONE) {
                    // 下载完成
                    if (listener != null) {
                        listener.downLoadSuccess(dirpath + fileName, fileName);
                    }
                    downLoadState = DONE;
                } else if (msg.what == ERROR) {
                    // 下载失败
                    if (listener != null) {
                        listener.downloadError((Exception) msg.obj);
                    }
                    downLoadState = ERROR;
                }
            }
        };

        new Thread() {
            public void run() {




                //起始字节设置为零
                long start = 0;
                //文件的总大小
                long totalSize = 0;
                //断点续传的配置文件
                File conffile = null;
                //下载的数据文件
                File dataFile = null;
                //真实下载的文件
                File realfile = null;
                //文件
                RandomAccessFile rafileApk = null;
                //数据流
                InputStream inputStream = null;


                try {

                    //文件夹必须存在
                    DirTool.createDir(dirpath, true);

                    //配置文件
                    conffile = new File(absolutePath + ".cfg");
                    //数据临时文件
                    dataFile = new File(absolutePath + ".data");
                    //真实下载的文件
                    realfile = new File(absolutePath);


                    //如果已经存在了这个文件 校验文件大小，初步保证完整性
                    if (realfile.exists()) {
                        //url开始连接
                        URL url = new URL(urlPath);
                        //打开链接
                        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                        //设置RequestProperty
                        conn.setRequestProperty("Accept-Encoding", "identity");
                        //设置 User-Agent
                        conn.setRequestProperty("User-Agent", "NetFox");
                        //设置断点续传的开始位置
                        conn.setRequestProperty("RANGE", "bytes=" + Long.toString(0) + "-");
                        //获取总大小
                        int  fileSize = conn.getContentLength();
                        //对比下载的文件的总大小是否相等
                        if (fileSize == FileSizeTool.getFileSize(realfile)) {
                            //下载完成了
                            Message m = new Message();
                            //成功
                            m.what = DONE;
                            //发送消息
                            proHanlder.sendMessage(m);
                            //返回不再继续了
                            return;
                        } else {
                            //进行初始化
                            start = 0;
                        }
                    }
                    //如果文件不存在，但是两个配置文件存在
                    else if (conffile.exists() && dataFile.exists()) {
                        //传入的数据
                        DataInputStream confData = null;
                        //log数据
                        FileInputStream confin = null;
                        try {
                            //获取到已经存储的长度数据
                            confin = new FileInputStream(conffile);
                            //获取到已经存储的长度数据
                            confData = new DataInputStream(confin);
                            // 读取到已经写入了多少
                            start = confData.readLong();
                            //不相等的情况下抛出
                            if (start != FileSizeTool.getFileSize(dataFile)) {
                                start = 0;
                            }
                        } catch (Exception e) {
                            //进行初始化
                            start = 0;
                        } finally {
                            //关闭流
                            if (confin != null) {
                                confin.close();
                            }
                            if (confData != null) {
                                confData.close();
                            }
                        }
                    }


                    //如果开始是零，那么重新创建文件
                    if (start == 0) {
                        //创建
                        conffile.createNewFile();
                        //创建
                        dataFile.createNewFile();
                    }


                    //取得apk文件的写入
                    rafileApk = new RandomAccessFile(dataFile, "rw");
                    //定位到开始的地方
                    rafileApk.seek(start);
                    //url开始连接
                    URL url = new URL(urlPath);
                    //打开链接
                    HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                    //初始化cookie
                    setCookie(conn);
                    //设置RequestProperty
                    conn.setRequestProperty("Accept-Encoding", "identity");
                    //设置 User-Agent
                    conn.setRequestProperty("User-Agent", "NetFox");
                    //设置断点续传的开始位置
                    conn.setRequestProperty("RANGE", "bytes=" + Long.toString(start) + "-");
                    //设置总大小
                    totalSize = (int) (conn.getContentLength() + start);


                    //获取返回值
                    int responseCode = conn.getResponseCode();
                    //失败抛异常
                    if (!(responseCode == HttpURLConnection.HTTP_OK || responseCode == HttpURLConnection.HTTP_PARTIAL)) {
                        throw new Exception("Connection error:" + responseCode + " url:" + url);
                    }
                    //成功继续执行
                    else {
                        //保存cookie等数据
                        generateSession(conn);
                        //获取到input
                        inputStream = conn.getInputStream();
                        //缓存大小
                        byte[] buffer = new byte[1024];
                        //长度
                        int len = 0;
                        //循环读取
                        while ((len = inputStream.read(buffer)) != -1) {
                            //线程被取消了，不再读了，如果说线程取消了，就不在写入了
                            if (threadStopFlag) {
                                break;
                            }
                            //写入数据
                            rafileApk.write(buffer, 0, len);
                            //新写入了len个字节
                            start = start + len;
                            //发送下载中的消息
                            Message m = new Message();
                            //下载中
                            m.what = DOWNLOADING;
                            //下载进度
                            m.arg1 = (int) (start * 100 / totalSize);
                            //移除之前的
                            proHanlder.removeMessages(0);
                            //发送消息
                            proHanlder.sendMessage(m);
                        }
                        //如果说已经全部下载完了
                        if (len == -1) {
                            //重新进行命名
                            dataFile.renameTo(realfile);
                            // 下载完成
                            Message m = new Message();
                            //下载完成
                            m.what = DONE;
                            //发送消息
                            proHanlder.sendMessage(m);
                            //不再继续
                            return;
                        }
                        //如果还没有下载完就代表是取消的
                        else {
                            // 下载完成
                            Message m = new Message();
                            m.what = CANCEL;
                            proHanlder.sendMessage(m);
                        }
                    }
                } catch (Exception e) {
                    //下载出错咯
                    Message m = proHanlder.obtainMessage(ERROR, e);
                    //发送错误消息
                    proHanlder.sendMessage(m);
                } finally {
                    //保存当前的下载进度
                    try {
                        if(conffile!=null) {
                            saveConfig(conffile, start);
                        }
                    } catch (Exception ex) {
                        LogTool.e(tag, ex.getMessage());
                    }
                    //关闭RandomAccessFile
                    try {
                        if(rafileApk!=null) {
                            rafileApk.close();
                        }
                    } catch (Exception ex) {
                        LogTool.e(tag, ex.getMessage());
                    }
                    //关闭inputStream
                    try {
                        if(inputStream!=null) {
                            inputStream.close();
                        }
                    } catch (Exception ex) {
                        LogTool.e(tag, ex.getMessage());
                    }
                    ThreadBusy = false;
                }

            }
        }.start();
    }


    /************
     * 保存配置文件
     * @param configFile   文件
     * @param start        长度
     * @throws Exception   错误
     */
    private void saveConfig(File configFile, long start) throws Exception {
        //下载完成时候的进度配置保存
        {
            //配置数据
            FileOutputStream confout = null;
            //配置
            DataOutputStream confdata = null;
            try {
                //文件
                confout = new FileOutputStream(configFile);
                //数据
                confdata = new DataOutputStream(confout);
                //写入数据
                confdata.writeLong(start);
                //关闭
                confdata.close();
            } catch (Exception e) {
                //错误信息
                throw e;
            } finally {
                //关闭
                if (confdata != null) {
                    try {
                        confdata.close();
                    } catch (IOException ex) {
                        LogTool.e(tag, ex.getMessage());
                    }
                }
                //关闭
                if (confout != null) {
                    try {
                        confout.close();
                    } catch (IOException ex) {
                        LogTool.e(tag, ex.getMessage());
                    }
                }
            }
        }
    }


    /************
     * 设置已经被加入的cookie
     * @param conn
     */
    private void setCookie(HttpURLConnection conn) {
        if (holder != null && holder.getCookie() != null) {
            conn.addRequestProperty("Cookie", holder.getCookie());
        }
    }


    /************
     * 保存当前的cookie
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
