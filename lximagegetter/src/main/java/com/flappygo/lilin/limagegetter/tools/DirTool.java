package com.flappygo.lilin.limagegetter.tools;

import java.io.File;

import com.flappygo.lilin.limagegetter.exception.LDirException;

import android.os.Environment;

/**********
 *
 * Package Name:com.flappygo.lipo.limagegetter.tools <br/>
 * ClassName: LDir <br/>
 * Function: 文件夹创建等 <br/>
 * date: 2016-3-9 上午10:14:47 <br/>
 * 
 * @author lijunlin
 */
public class DirTool {

	/**************
	 * 判断SD卡是否插入
	 * @return
	 */
	public static boolean isSDCardMounted() {
		String status = Environment.getExternalStorageState();
		if (status.equals(Environment.MEDIA_MOUNTED)) {
			return true;
		} else {
			return false;
		}
	}
	
	

	/************
	 * 通过dirPath 创建文件夹
	 * 
	 * @param dirPath
	 *            文件夹目录
	 * @param noMedia
	 *            是否同时创建noMedia文件
	 * @throws Exception
	 *             异常
	 */
	public static void createDir(String dirPath, boolean noMedia)
			throws Exception {
		// 如果没有文件夹插入进来
		if (!isSDCardMounted()) {
			// 抛出未能挂载的异常
			throw new LDirException(dirPath, "sdcard no mounted");
		}

		// 创建file
		File file = new File(dirPath);
		// 创建操作进行加锁
		synchronized (DirTool.class) {
			// 如果file不存在
			if (!file.exists()) {
				// 创建文件夹
				if (!file.mkdirs()) {
					throw new LDirException(dirPath, "can't  create mkdirs");
				}
			}
		}

		// 如果设置了noMedia
		if (noMedia) {
			File nomidia = new File(dirPath + ".nomedia");
			if (!nomidia.exists()) {
				if (!nomidia.createNewFile()) {
					throw new LDirException(dirPath,
							"can't  create nomedia file");
				}
			}
		}
	}

	/************************
	 * 删除文件夹下面的文件
	 * 
	 * @param file
	 *            文件夹路径
	 * @param deleteDir
	 *            是否同时删除文件夹
	 * @return
	 */
	public static void deleteDirFiles(File file, boolean deleteDir)
			throws Exception {

		// 如果是
		if (file.isDirectory()) {
			// 列出文件
			File[] childFiles = file.listFiles();
			// 没有文件直接返回
			if (childFiles == null || childFiles.length == 0) {
				return;
			}
			for (int i = 0; i < childFiles.length; i++) {
				deleteDirFiles(childFiles[i], deleteDir);
			}
			// 如果确定连文件夹一起删除
			if (deleteDir) {
				// 无法删除文件夹
				if (!file.delete()) {
					throw new LDirException(file.getAbsolutePath(),
							"can't  delete dir");
				}
			}
		} else {
			// 如果删除失败
			if (!file.delete()) {
				throw new LDirException(file.getAbsolutePath(),
						"can't  delete file");
			}
			file = null;
		}
	}

	/*********
	 * 删除文件夹下内容
	 * 
	 * @param dirPath
	 *            文件夹路径
	 * @param deleteDir
	 *            是否连带文件夹一起删除
	 * @throws Exception
	 *             异常
	 */
	public static void deleteDirFiles(String dirPath, boolean deleteDir)
			throws Exception {
		// 如果没有文件夹插入进来
		if (!isSDCardMounted()) {
			// 抛出未能挂载的异常
			throw new LDirException(dirPath, "sdcard no mounted");
		}
		deleteDirFiles(new File(dirPath), deleteDir);
	}
	
	
	
	
	

}
