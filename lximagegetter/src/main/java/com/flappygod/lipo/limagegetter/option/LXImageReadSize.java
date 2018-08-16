package com.flappygod.lipo.limagegetter.option;

import android.graphics.Bitmap;


/************
 * 
 * Package Name:com.flappygod.lipo.limagegetter.model <br/> 
 * ClassName: LXImageReadSize <br/>
 * Function: 读取图片时候的图片地区参数设置   <br/> 
 * date: 2016-3-9 下午2:04:21 <br/> 
 * 
 * @author lijunlin
 */
public class LXImageReadSize {

	//图片读取时读取的最大宽度
	private int  maxWidth;
	//图片读取时读取的最大高度
	private int  maxHeight;
	//读取图片时是否拉伸填充满设置的宽高
	private boolean scaleFill;
    //弧度的设置
	private RadiusOption radiusOption;
	//读取图片的
	private Bitmap.Config inPreferredConfig;

	
	public LXImageReadSize(int maxWidth, int maxHeight, boolean scaleFill) {
		super();
		this.maxWidth = maxWidth;
		this.maxHeight = maxHeight;
		this.scaleFill = scaleFill;
	}
	
	public LXImageReadSize(int maxWidth, int maxHeight, boolean scaleFill, Bitmap.Config config) {
		super();
		this.maxWidth = maxWidth;
		this.maxHeight = maxHeight;
		this.scaleFill = scaleFill;
		this.inPreferredConfig=config;
	}

	public LXImageReadSize(int maxWidth, int maxHeight, boolean scaleFill, Bitmap.Config config,RadiusOption radiusOption) {
		super();
		this.maxWidth = maxWidth;
		this.maxHeight = maxHeight;
		this.scaleFill = scaleFill;
		this.inPreferredConfig=config;
		this.radiusOption=radiusOption;
	}
	
	
	public int getMaxWidth() {
		return maxWidth;
	}
	public void setMaxWidth(int maxWidth) {
		this.maxWidth = maxWidth;
	}
	public int getMaxHeight() {
		return maxHeight;
	}
	public void setMaxHeight(int maxHeight) {
		this.maxHeight = maxHeight;
	}
	public boolean isScaleFill() {
		return scaleFill;
	}
	public void setScaleFill(boolean scaleFill) {
		this.scaleFill = scaleFill;
	}

	public Bitmap.Config getInPreferredConfig() {
		return inPreferredConfig;
	}

	public void setInPreferredConfig(Bitmap.Config inPreferredConfig) {
		this.inPreferredConfig = inPreferredConfig;
	}


	public RadiusOption getRadiusOption() {
		return radiusOption;
	}

	public void setRadiusOption(RadiusOption radiusOption) {
		this.radiusOption = radiusOption;
	}


	//获取附加字符串
	public String getSizeStrAdditional(){
		String str="";
		//拼接宽高设置
		if(maxHeight>0||maxWidth>0) {
			//获取size的附加字符串
			 str =str + getMaxWidth() + "*" + getMaxHeight();
		}
		if(inPreferredConfig!=null){
			str=str+"$"+inPreferredConfig;
		}
		if(radiusOption!=null){
			str=str+"#"+radiusOption.getRadian()+"|"+radiusOption.getScaleType();
		}
		return str;
	}

}
