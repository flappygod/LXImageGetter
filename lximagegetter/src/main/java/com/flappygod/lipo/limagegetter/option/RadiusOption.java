package com.flappygod.lipo.limagegetter.option;

/**
 * Created by Administrator on 2017/5/27.
 */

public class RadiusOption {

    //corner的类型
    private  ScaleType  scaleType;
    //截取的弧度
    private  float      radian;


    public   RadiusOption(float radian){
        this.radian=radian;
        this.scaleType=ScaleType.CENTER_CROP_RADIUS;
    }

    public   RadiusOption(float radian,ScaleType type){
        this.radian=radian;
        this.scaleType=type;
    }


    public enum ScaleType {
        //图片先进行切割，保证宽高一致
        CENTER_CROP_RADIUS (1),
        //不进行切割，直接设置radius
        NO_CROP_RADIUS(2);
        ScaleType(int ni) {
            nativeInt = ni;
        }
        final int nativeInt;
    }


    public ScaleType getScaleType() {
        return scaleType;
    }

    public void setScaleType(ScaleType scaleType) {
        this.scaleType = scaleType;
    }

    public float getRadian() {
        return radian;
    }

    public void setRadian(float radian) {
        this.radian = radian;
    }
}
