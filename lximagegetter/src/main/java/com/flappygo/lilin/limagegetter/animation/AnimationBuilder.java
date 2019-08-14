package com.flappygo.lilin.limagegetter.animation;

import android.view.animation.Animation;
import android.widget.ImageView;

public interface AnimationBuilder {
	
	enum  ImageSourceType{
		//来自cache
		FROM_CACHE(0),
		//来自网络
		FROM_NET(1),
		//来自存储卡
		FROM_SDCARD(2);
		
		final int code;
		ImageSourceType(int t){
			code=t;
		}
	}

	/********
	 * 构建 Animation
	 * @return
	 */
	Animation buildAnimation(ImageView imageView, ImageSourceType souceType);
}
