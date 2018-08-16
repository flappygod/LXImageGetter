package com.flappygod.lipo.limagegetter.tools;

import android.graphics.Bitmap;

public class BitmapTool {

	/**********
	 * 获取一个透明的bitmap
	 * 
	 * @param picw
	 *            宽度
	 * @param pich
	 *            高度
	 * @return
	 */
	public static Bitmap getAlphaBitMap(int picw, int pich) {
		Bitmap btmp = Bitmap.createBitmap(picw, pich, Bitmap.Config.ARGB_8888);
		int[] pix = new int[picw * pich];

		for (int y = 0; y < pich; y++)
			for (int x = 0; x < picw; x++) {
				int index = y * picw + x;
				int r = ((pix[index] >> 16) & 0xff) | 0xff;
				int g = ((pix[index] >> 8) & 0xff) | 0xff;
				int b = (pix[index] & 0xff) | 0xff;
				pix[index] = 0x00000000 | (r << 16) | (g << 8) | b;
			}
		btmp.setPixels(pix, 0, picw, 0, 0, picw, pich);
		return btmp;
	}
}
