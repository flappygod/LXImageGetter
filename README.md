# Lximagegetter

图片加载框架，主要解决：
1.大图加载问题。
2.图片过多内存溢出问题。
3.图片显示动画。
4.列表图片加载错位问题。

引入：
allprojects {
		repositories {
			...
			maven { url 'https://jitpack.io' }
		}
}
dependencies {
	        implementation 'com.github.flappygod:Lximagegetter:1.1'
}


初始化：
//初始化图片加载框架
LXImageGetter.init(getApplicationContext(), 8, BaseConfig.imagePath, 10, 10);



使用:
LXImageGetter.getInstance().getImageWithUrl(ImageView,url);
