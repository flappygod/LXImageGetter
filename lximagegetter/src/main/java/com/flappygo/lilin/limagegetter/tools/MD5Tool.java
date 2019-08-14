package com.flappygo.lilin.limagegetter.tools;

import java.security.MessageDigest;


/********************
 *
 * Package Name:com.flappygo.lipo.limagegetter.tools <br/>
 * ClassName: MD5Tool <br/>
 * Function:  MD5加密类    <br/> 
 * date: 2016-3-9 上午10:01:50 <br/> 
 * 
 * @author lijunlin
 */


public class MD5Tool {
	
	
	private final static String[] hexDigits = { "0", "1", "2", "3", "4", "5",
			"6", "7", "8", "9", "a", "b", "c", "d", "e", "f" };

	/**
	 * 转换字节数组为16进制字串
	 * 
	 * @param b 字节数组
	 * @return 16进制字串
	 */
	public static String byteArrayToHexString(byte[] b) {
		StringBuffer resultSb = new StringBuffer();
		for (int i = 0; i < b.length; i++) {
			resultSb.append(byteToHexString(b[i]));
		}
		return resultSb.toString();
	}

	/****************
	 * 比特转换hex
	 * @param b  比特
	 * @return
	 */
	private static String byteToHexString(byte b) {
		int n = b;
		if (n < 0)
			n = 256 + n;
		int d1 = n / 16;
		int d2 = n % 16;
		return hexDigits[d1] + hexDigits[d2];
	}

	/*****************
	 * MD5加密 
	 * @param origin  需要加密的字符串
	 * @return
	 */
	public static String MD5Encode(String origin) {
		String resultString = null;
		try {
			resultString = new String(origin);
			MessageDigest md = MessageDigest.getInstance("MD5Tool");
			resultString = byteArrayToHexString(md.digest(resultString
					.getBytes()));
		} catch (Exception ex) {

		}
		return resultString;
	}
}