package com.flappygod.lipo.limagegetter.tools;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.widget.ImageView;

import com.flappygod.lipo.limagegetter.option.RadiusOption;

/**
 * Created by Administrator on 2017/5/27.
 */

public class BitmapRadiusTool {


    public static Bitmap toRoundCorner(Bitmap bitmap, RadiusOption option) {
        if (bitmap == null) {
            return null;
        }
        final int color = 0xff424242;
        //源头的rect
        Rect srcRect = new Rect(0, 0, bitmap.getWidth(), bitmap.getHeight());
        //目标的rect
        Rect destRect = new Rect(0, 0, bitmap.getWidth(), bitmap.getHeight());
        //截取的话
        if (option.getScaleType()== RadiusOption.ScaleType.CENTER_CROP_RADIUS) {
            //宽高比为1.0,进行切割
            float whscale =  1.0f;
            float whbScale = (float) bitmap.getWidth() / (float) bitmap.getHeight();
            //如果view的长宽比
            if (whscale > whbScale) {
                //计算src需要被写入的部分
                int top = (int) ((bitmap.getHeight() - bitmap.getWidth() / whscale) / 2);
                srcRect = new Rect(0, top, bitmap.getWidth(), bitmap.getHeight() - top);
                destRect = new Rect(0, 0, bitmap.getWidth(), bitmap.getHeight() - 2 * top);

            } else {
                int left = (int) ((bitmap.getWidth() - bitmap.getHeight() * whscale) / 2);
                srcRect = new Rect(left, 0, bitmap.getWidth() - left, bitmap.getHeight());
                destRect = new Rect(0, 0, bitmap.getWidth() - 2 * left, bitmap.getHeight());
            }
        }
        Bitmap output = Bitmap.createBitmap(destRect.width(),
                destRect.height(), Bitmap.Config.ARGB_8888);
        //创建canvas
        Canvas canvas = new Canvas(output);
        canvas.drawARGB(0, 0, 0, 0);
        //corner
        float roundPx = option.getRadian() * Math.min(bitmap.getHeight(), bitmap.getWidth());
        Paint paint = new Paint();
        paint.setAntiAlias(true);
        paint.setColor(color);
        RectF rectF = new RectF(destRect);
        canvas.drawRoundRect(rectF, roundPx, roundPx, paint);
        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
        canvas.drawBitmap(bitmap, srcRect, destRect, paint);
        return output;
    }

}
