package com.flappygod.lipo.limagegetter.tools;

import com.flappygod.lipo.limagegetter.option.LXImageReadOption;

/************
 *
 * Package Name:com.flappygod.lipo.limagegetter.tools <br/>
 * ClassName: NameTool <br/>
 * Function: 根据url地址获取该文件名称 <br/>
 * date: 2016-3-9 下午2:41:00 <br/>
 *
 * @author lijunlin
 */
public class NameTool {

    public final static String tag = "NameTool";

    /**************
     * 通过url获取默认的保存文件名称
     *
     * @param url
     * @return
     */
    public static String getDefaultByUrl(String url) {
        String ret = url;
        // 没有url返回空的字符串
        if (ret == null) {
            return "";
        }
        // 直接拼接字符串
        try {
            // 去掉前面的http
            if (ret.startsWith("http://")) {
                ret = ret.replace("http://", "");
            }
            // 去掉之前的https
            if (ret.startsWith("https://")) {
                ret = ret.replace("https://", "");
            }
            String[] out = ret.split("/");
            ret = "";
            for (int w = 0; w < out.length; w++) {
                ret += out[w];
            }
        } catch (Exception e) {
            LogTool.e(tag, e.getMessage());
        }
        //太大了就干掉这个名称吧
        if (ret.getBytes().length > 255) {
            // 返回通过MD5加密的
            return MD5Tool.MD5Encode(ret);
        } else {
            return ret;
        }
    }

    /***********
     * 获取文件被保存的名称
     * @param url         文件的地址
     * @param userSetName 用户设置的文件名称
     * @return 文件的名称
     */
    public static String getImageName(String url, String userSetName) {
        if (userSetName != null) {
            return userSetName;
        } else {
            return getDefaultByUrl(url);
        }
    }

    /**************
     * 获取保存文件的整个绝对路径
     * @param dirPath      保存文件的文件夹路径
     * @param url          网络图片的地址
     * @param userSetName  用户设置的图片下载名称，为空是取默认名称
     * @return
     */
    public static String getImageAbsolutePath(String dirPath, String url, String userSetName) {
        String retStr = dirPath + getImageName(url, userSetName);
        return retStr;
    }


    /***************
     * 获取文件在内存hashMap中的键名称
     * @param dirPath   目录地址
     * @param url       下载地址
     * @param userSetName  用户设置名称
     * @param readOption  读取参数
     * @return
     */
    public static String getImageAbsoluteKey(String dirPath, String url, String userSetName, LXImageReadOption readOption) {
        if (readOption != null) {
            String retStr = getImageAbsolutePath(dirPath, url, userSetName) + readOption.getOptionAdditional();
            return retStr;
        } else {
            String retStr = getImageAbsolutePath(dirPath, url, userSetName);
            return retStr;
        }
    }

    /***************
     * 获取文件在内存hashMap中的键名称
     * @param absolutePath   地址
     * @param readOption     读取参数
     * @return
     */
    public static String getImageAbsoluteKey(String absolutePath, LXImageReadOption readOption) {
        if (readOption != null) {
            String retStr = absolutePath + readOption.getOptionAdditional();
            return retStr;
        } else {
            String retStr = absolutePath;
            return retStr;
        }
    }

}
