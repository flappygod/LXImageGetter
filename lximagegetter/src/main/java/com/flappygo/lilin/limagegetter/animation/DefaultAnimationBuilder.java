package com.flappygo.lilin.limagegetter.animation;

import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.ImageView;

/********
 *
 * Package Name:com.flappygo.lipo.limagegetter.animation <br/>
 * ClassName: DefaultAnimationBuilder <br/>
 * Function: 默认的构造器 <br/>
 * date: 2016-3-10 下午3:41:33 <br/>
 *
 * @author lijunlin
 */
public class DefaultAnimationBuilder implements AnimationBuilder {

    @Override
    public Animation buildAnimation(ImageView imageview,
                                    ImageSourceType souceType) {
        //默认的加载动画
        if (souceType == ImageSourceType.FROM_CACHE) {
            Animation animation = new AlphaAnimation(0f, 1.0f);
            animation.setDuration(0);
            return animation;
        } else if (souceType == ImageSourceType.FROM_NET) {
            //默认的加载动画
            Animation animation = new AlphaAnimation(0f, 1.0f);
            animation.setDuration(300);
            return animation;
        } else if (souceType == ImageSourceType.FROM_SDCARD) {
            //默认的加载动画
            Animation animation = new AlphaAnimation(0f, 1.0f);
            animation.setDuration(300);
            return animation;
        }
        return null;
    }
}
